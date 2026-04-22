package com.vylexai.app.ui

import androidx.compose.runtime.Composable
import com.vylexai.app.ui.components.VylexBackdrop
import com.vylexai.app.ui.navigation.VylexNavHost

@Composable
fun VylexApp() {
    VylexBackdrop {
        VylexNavHost()
    }
}
