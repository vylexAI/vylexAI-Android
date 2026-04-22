package com.vylexai.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vylexai.app.ui.components.BrandMark
import com.vylexai.app.ui.components.GhostButton
import com.vylexai.app.ui.components.PrimaryButton
import com.vylexai.app.ui.components.VylexBackdrop
import com.vylexai.app.ui.theme.VylexPalette
import com.vylexai.app.ui.theme.VylexTheme
import kotlinx.coroutines.launch

private data class Slide(val title: String, val body: String)

private val slides = listOf(
    Slide(
        "A network, not a data center",
        "Millions of phones become nodes in one decentralized AI compute layer. No single company owns the intelligence."
    ),
    Slide(
        "Your device, your participation",
        "Contribute idle compute while charging on Wi-Fi. Earn BSAI for every verified task the network routes to your phone."
    ),
    Slide(
        "Train or serve — your call",
        "Run inference and fine-tune compact models on the same network that moves your data. Pay only for the work you trigger."
    )
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val s = slides[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                BrandMark(size = 88.dp)
                Spacer(Modifier.height(32.dp))
                Text(
                    text = s.title,
                    style = MaterialTheme.typography.displaySmall,
                    color = VylexPalette.Text100
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = s.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VylexPalette.Text300
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize().weight(0.08f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(slides.size) { i ->
                val active = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            if (active) VylexPalette.Cyan300 else VylexPalette.Ink500,
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        val isLast = pagerState.currentPage == slides.lastIndex
        PrimaryButton(
            text = if (isLast) "Choose your mode" else "Next",
            onClick = {
                if (isLast) onFinish()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            }
        )
        Spacer(Modifier.height(12.dp))
        GhostButton(text = "Skip", onClick = onFinish)
    }
}

@Preview
@Composable
private fun OnboardingPreview() {
    VylexTheme { VylexBackdrop { OnboardingScreen(onFinish = {}) } }
}
