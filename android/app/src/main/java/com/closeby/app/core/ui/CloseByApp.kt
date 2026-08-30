package com.closeby.app.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.closeby.app.core.navigation.CloseByBottomBar
import com.closeby.app.core.navigation.CloseByNavHost
import com.closeby.app.core.ui.theme.CloseByTheme

/**
 * App root: theme + bottom navigation + nav host.
 * This is the base scaffold — no feature logic lives here.
 */
@Composable
fun CloseByApp() {
    CloseByTheme {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = { CloseByBottomBar(navController) }
        ) { padding ->
            CloseByNavHost(
                navController = navController,
                modifier = androidx.compose.ui.Modifier.padding(padding)
            )
        }
    }
}
