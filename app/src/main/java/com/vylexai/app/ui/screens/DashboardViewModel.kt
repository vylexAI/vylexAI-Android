package com.vylexai.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vylexai.app.data.auth.AuthTokenStore
import com.vylexai.app.data.net.VylexException
import com.vylexai.app.data.wallet.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the metric tiles on Provider + Client dashboards.
 *
 * Same pull-or-demo pattern as [WalletViewModel]: live numbers when a JWT
 * exists and the coordinator answers; curated demo numbers otherwise so the
 * dashboards read well even on a brand-new install.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val wallet: WalletRepository,
    private val tokenStore: AuthTokenStore
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState(kind = DashboardKind.Loading))
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (!tokenStore.isAuthenticated()) {
            _state.value = DEMO_STATE
            return
        }
        _state.update { it.copy(kind = DashboardKind.Loading) }
        viewModelScope.launch {
            runCatching {
                val bal = wallet.balance()
                val stats = wallet.stats()
                bal to stats
            }.onSuccess { (bal, stats) ->
                _state.value = DashboardUiState(
                    kind = DashboardKind.Live,
                    bsaiEarned = BigDecimal(stats.bsaiEarned),
                    bsaiSpent = BigDecimal(stats.bsaiSpent),
                    balanceBsai = BigDecimal(bal.balanceBsai),
                    tasksCompleted = stats.tasksCompleted,
                    contributionScore = stats.contributionScore
                )
            }.onFailure { t ->
                _state.value = when (t) {
                    is VylexException.AuthExpired -> DEMO_STATE
                    else -> DashboardUiState(kind = DashboardKind.Error, message = t.message)
                }
            }
        }
    }

    private companion object {
        val DEMO_STATE = DashboardUiState(
            kind = DashboardKind.Demo,
            bsaiEarned = BigDecimal("24.38"),
            bsaiSpent = BigDecimal("8.14"),
            balanceBsai = BigDecimal("24.38"),
            tasksCompleted = DEMO_TASKS,
            contributionScore = DEMO_CONTRIB
        )

        const val DEMO_TASKS = 1284
        const val DEMO_CONTRIB = 97
    }
}

enum class DashboardKind { Loading, Live, Demo, Error }

data class DashboardUiState(
    val kind: DashboardKind,
    val bsaiEarned: BigDecimal = BigDecimal.ZERO,
    val bsaiSpent: BigDecimal = BigDecimal.ZERO,
    val balanceBsai: BigDecimal = BigDecimal.ZERO,
    val tasksCompleted: Int = 0,
    val contributionScore: Int = 0,
    val message: String? = null
)
