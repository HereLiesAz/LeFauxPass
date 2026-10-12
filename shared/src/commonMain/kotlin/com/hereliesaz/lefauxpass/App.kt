package com.hereliesaz.lefauxpass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.hereliesaz.lefauxpass.ui.theme.LeFauxPassTheme

private enum class Screen { Details, Ticket, MyTickets }

/** Root of both the Android app and the PWA. */
@Composable
fun App(hooks: PlatformHooks) {
    CompositionLocalProvider(LocalPlatform provides hooks) {
        LeFauxPassTheme {
            // Details -> (Activate) -> Ticket <-> My tickets (back / card tap).
            var screen by rememberSaveable { mutableStateOf(Screen.Details) }
            when (screen) {
                Screen.Details -> TicketDetailsScreen(onActivate = { screen = Screen.Ticket })
                Screen.Ticket -> {
                    hooks.BackHandler { screen = Screen.MyTickets }
                    RtaTicketScreen(onBack = { screen = Screen.MyTickets })
                }
                Screen.MyTickets -> MyTicketsScreen(onViewTicket = { screen = Screen.Ticket })
            }
        }
    }
}
