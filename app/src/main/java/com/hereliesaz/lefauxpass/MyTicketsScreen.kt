package com.hereliesaz.lefauxpass

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.LinearScale
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.hereliesaz.lefauxpass.ui.theme.LeFauxPassTheme
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val ActiveGreen = Color(0xFF1FC27A)
private val CardSurface = Color(0xFF232529)
private val PillBackground = Color(0xFF5A1470)

/**
 * "My tickets" hub, reached by backing out of the ticket screen.
 * Tapping the active (green) ride card calls [onViewTicket].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(onViewTicket: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DetailsTopBar.toArgb()
            window.navigationBarColor = DetailsTopBar.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val activeUntil = remember {
        (ExpirationManager.getExpirationTime(context) ?: ZonedDateTime.now().plusHours(1).plusMinutes(56))
            .format(DateTimeFormatter.ofPattern("M/d/yyyy, h:mma"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My tickets", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = {}) { Icon(Icons.Filled.Menu, contentDescription = "Menu") }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = "Account")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DetailsTopBar,
                    titleContentColor = PrimaryText,
                    navigationIconContentColor = PrimaryText,
                    actionIconContentColor = PrimaryText
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SheetBackground) {
                val colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Accent,
                    selectedTextColor = Accent,
                    unselectedIconColor = PrimaryText,
                    unselectedTextColor = PrimaryText,
                    indicatorColor = Color.Transparent
                )
                NavTab(Icons.Outlined.Directions, "Directions", false, colors)
                NavTab(Icons.Outlined.Place, "Map", false, colors)
                NavTab(Icons.Outlined.LinearScale, "Lines", false, colors)
                NavTab(Icons.Filled.ConfirmationNumber, "Tickets", true, colors)
            }
        },
        containerColor = DetailsBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("My rides")
            Spacer(Modifier.height(16.dp))
            Surface(
                onClick = onViewTicket,
                shape = RoundedCornerShape(14.dp),
                color = CardSurface,
                border = BorderStroke(2.dp, ActiveGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(22.dp).background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Chevrons(listOf(Color(0xFF5B2C83), Color(0xFF4E8A2E)), Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Adult Single Ride, Bus & Streetcar", color = PrimaryText, fontSize = 16.sp)
                            Text("Active until $activeUntil", color = SecondaryText, fontSize = 13.sp)
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(PillBackground)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.ConfirmationNumber,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("View ticket", color = Accent, fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
            SectionTitle("Pay for rides")
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSurface)
                    .clickable {}
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.ConfirmationNumber,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(18.dp))
                Column {
                    Text("Buy a ticket", color = PrimaryText, fontSize = 16.sp)
                    Text("Select the agency and purchase a ticket", color = SecondaryText, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(36.dp))
            Text(
                "Ride History",
                color = Accent,
                fontSize = 15.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {}
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = PrimaryText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NavTab(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    colors: androidx.compose.material3.NavigationBarItemColors
) {
    NavigationBarItem(
        selected = selected,
        onClick = {},
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        colors = colors
    )
}

@Preview(widthDp = 360, heightDp = 740)
@Composable
fun MyTicketsScreenPreview() {
    LeFauxPassTheme { MyTicketsScreen(onViewTicket = {}) }
}
