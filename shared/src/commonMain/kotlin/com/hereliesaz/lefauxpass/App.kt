package com.hereliesaz.lefauxpass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.hereliesaz.lefauxpass.ui.theme.LeFauxPassTheme

private enum class Screen { Details, Ticket, MyTickets }

/** Stored names from the former preset picker, mapped to their original ring colors. */
private val legacyRingColors = mapOf(
    "Pink" to Color(0xFFEC2A90),
    "Red" to Color(0xFFE53945),
    "Coral" to Color(0xFFFF7167),
    "Orange" to Color(0xFFF78124),
    "Gold" to Color(0xFFE7A824),
    "Yellow" to Color(0xFFF4D32A),
    "Lime" to Color(0xFF9DD231),
    "Green" to Color(0xFF00A650),
    "Mint" to Color(0xFF57D9A6),
    "Teal" to Color(0xFF00A59F),
    "Turquoise" to Color(0xFF23CFD8),
    "LightBlue" to Color(0xFF65B9F6),
    "RoyalBlue" to Color(0xFF4169E1),
    "Periwinkle" to Color(0xFF929BFF),
    "Purple" to Color(0xFF8D48CC),
    "Lavender" to Color(0xFFC7A1F1),
)

/** Root of both the Android app and the PWA. */
@Composable
fun App(hooks: PlatformHooks) {
    CompositionLocalProvider(LocalPlatform provides hooks) {
        LeFauxPassTheme {
            // Details -> (Activate) -> Ticket <-> My tickets (back / card tap).
            var screen by rememberSaveable { mutableStateOf(Screen.Details) }
            var ringColor by remember(hooks) {
                val preference = hooks.loadRingColor()
                val storedColor = preference?.toIntOrNull()?.takeIf { it ushr 24 == 0xFF }
                mutableStateOf(storedColor?.let { Color(it) } ?: preference?.let(legacyRingColors::get) ?: TicketAnimationColors().ring)
            }
            when (screen) {
                Screen.Details -> TicketDetailsScreen(onActivate = { screen = Screen.Ticket })
                Screen.Ticket -> {
                    hooks.BackHandler { screen = Screen.MyTickets }
                    RtaTicketScreen(
                        onBack = { screen = Screen.MyTickets },
                        ringColors = TicketAnimationColors(ring = ringColor),
                    )
                }
                Screen.MyTickets -> MyTicketsScreen(
                    onViewTicket = { screen = Screen.Ticket },
                    ringColor = ringColor,
                    onRingColorSelected = { choice ->
                        hooks.saveRingColor(choice.toArgb().toString())
                        ringColor = choice
                    },
                )
            }
        }
    }
}
