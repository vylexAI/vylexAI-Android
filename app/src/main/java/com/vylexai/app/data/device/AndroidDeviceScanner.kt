package com.vylexai.app.data.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.vylexai.app.domain.device.BatteryInfo
import com.vylexai.app.domain.device.CpuInfo
import com.vylexai.app.domain.device.DeviceReport
import com.vylexai.app.domain.device.DeviceScanner
import com.vylexai.app.domain.device.NetworkInfo
import com.vylexai.app.domain.device.NetworkType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production device scanner — reads real sensors and capabilities.
 *
 * Nothing leaves the device: the report is returned in-memory and the caller
 * decides what (if anything) to upload to the coordinator.
 */
@Singleton
class AndroidDeviceScanner @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceScanner {

    override suspend fun scan(): DeviceReport {
        val cpu = readCpu()
        val ramGb = readRamGb()
        val freeGb = readFreeStorageGb()
        val battery = readBattery()
        val network = readNetwork()
        val pm = context.packageManager
        val vulkan = pm.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL)
        val nnapi = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

        val performance = computeScore(cpu, ramGb, nnapi, vulkan, battery.temperatureC)
        val (lo, hi) = estimateBsai(performance)
        return DeviceReport(
            model = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
            androidSdk = Build.VERSION.SDK_INT,
            cpu = cpu,
            // Android doesn't expose GPU name without an active GL context — deferred.
            gpu = null,
            ramGb = ramGb,
            freeGb = freeGb,
            battery = battery,
            network = network,
            nnapi = nnapi,
            vulkan = vulkan,
            performanceScore = performance,
            estimateMonthlyBsai = lo to hi,
            recommendedTasks = recommend(performance)
        )
    }

    private fun readCpu(): CpuInfo {
        val cores = Runtime.getRuntime().availableProcessors()
        val socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL.takeIf { it != Build.UNKNOWN && it.isNotBlank() }
        } else {
            null
        }
        val model = socModel ?: Build.HARDWARE
        val maxHz = runCatching {
            File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
                .readText()
                .trim()
                .toInt() / KHZ_PER_MHZ
        }.getOrNull()
        return CpuInfo(cores = cores, model = model, maxFrequencyMhz = maxHz)
    }

    private fun readRamGb(): Int {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return ((info.totalMem + GB_MINUS_ONE) / GIGABYTE).toInt()
    }

    private fun readFreeStorageGb(): Int {
        val stat = StatFs(Environment.getDataDirectory().path)
        val bytes = stat.availableBytes
        return (bytes / GIGABYTE).toInt()
    }

    private fun readBattery(): BatteryInfo {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent: Intent? = context.registerReceiver(null, filter)
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) (level * PERCENT_100 / scale) else 0
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val tempC = if (tempTenths >= 0) tempTenths / TENTHS_PER_UNIT else 0
        return BatteryInfo(percent = pct, temperatureC = tempC, isCharging = isCharging)
    }

    private fun readNetwork(): NetworkInfo {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val active = cm.activeNetwork
        val caps = active?.let { cm.getNetworkCapabilities(it) }
        if (caps == null) return NetworkInfo(NetworkType.NONE, null)
        val downMbps = (caps.linkDownstreamBandwidthKbps / KBPS_PER_MBPS).takeIf { it > 0 }
        val type = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            else -> NetworkType.OTHER
        }
        return NetworkInfo(type = type, downlinkMbps = downMbps)
    }

    private fun computeScore(
        cpu: CpuInfo,
        ramGb: Int,
        nnapi: Boolean,
        vulkan: Boolean,
        tempC: Int
    ): Int {
        // Heuristic, not a benchmark — good enough for task routing hints.
        // Range: 0..SCORE_MAX. Coordinator can replace with a real benchmark later.
        val coreScore = cpu.cores * SCORE_PER_CORE
        val freqScore = ((cpu.maxFrequencyMhz ?: DEFAULT_FREQ_MHZ) / FREQ_SCORE_DIVISOR)
            .coerceAtMost(FREQ_SCORE_CAP)
        val ramScore = ramGb * SCORE_PER_GB_RAM
        val nnapiBonus = if (nnapi) SCORE_NNAPI_BONUS else 0
        val vulkanBonus = if (vulkan) SCORE_VULKAN_BONUS else 0
        val thermalPenalty = if (tempC > MAX_SAFE_TEMP_C) {
            (tempC - MAX_SAFE_TEMP_C) * SCORE_PER_DEGREE_OVER
        } else {
            0
        }
        return (coreScore + freqScore + ramScore + nnapiBonus + vulkanBonus - thermalPenalty)
            .coerceIn(0, SCORE_MAX)
    }

    private fun estimateBsai(score: Int): Pair<Int, Int> {
        val base = score.coerceAtLeast(1) / SCORE_MAX.toDouble()
        return (base * EST_LO_BSAI).toInt() to (base * EST_HI_BSAI).toInt()
    }

    private fun recommend(score: Int): List<String> = when {
        score >= REC_HIGH_THRESHOLD -> listOf(
            "Image classification",
            "Object detection",
            "OCR",
            "NLP inference",
            "Lightweight fine-tuning"
        )
        score >= REC_MID_THRESHOLD -> listOf("Image classification", "OCR", "NLP inference")
        else -> listOf("Image classification", "Content moderation")
    }

    private companion object {
        const val GIGABYTE = 1024L * 1024 * 1024
        const val GB_MINUS_ONE = GIGABYTE - 1
        const val MAX_SAFE_TEMP_C = 40

        const val KHZ_PER_MHZ = 1000
        const val KBPS_PER_MBPS = 1000
        const val PERCENT_100 = 100
        const val TENTHS_PER_UNIT = 10

        // Device-score tuning — adjust to change how task routing weights hardware.
        const val SCORE_MAX = 1000
        const val SCORE_PER_CORE = 40
        const val SCORE_PER_GB_RAM = 30
        const val SCORE_PER_DEGREE_OVER = 10
        const val FREQ_SCORE_DIVISOR = 10
        const val FREQ_SCORE_CAP = 300
        const val DEFAULT_FREQ_MHZ = 2000
        const val SCORE_NNAPI_BONUS = 120
        const val SCORE_VULKAN_BONUS = 80

        // Recommendation tiers + monthly earning estimate range.
        const val REC_HIGH_THRESHOLD = 700
        const val REC_MID_THRESHOLD = 400
        const val EST_LO_BSAI = 10
        const val EST_HI_BSAI = 50
    }
}
