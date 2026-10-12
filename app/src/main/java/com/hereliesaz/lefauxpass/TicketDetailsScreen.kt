package com.hereliesaz.lefauxpass

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.hereliesaz.lefauxpass.ui.theme.LeFauxPassTheme
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// Dark palette sampled from the reference screenshots.
private val DetailsBackground = Color(0xFF1B1C20)
private val DetailsTopBar = Color(0xFF222327)
private val SheetBackground = Color(0xFF232529)
private val Accent = Color(0xFFD05CE0)
private val OnAccent = Color(0xFF2A1030)
private val SecondaryText = Color(0xFF9A9BA0)
private val PrimaryText = Color(0xFFE4E4E8)

/**
 * Pre-activation "Ticket Details" screen. "Activate ticket" opens a
 * confirmation bottom sheet; confirming calls [onActivate].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailsScreen(onActivate: () -> Unit) {
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

    // Shows the stored expiration if still valid, else the one activation would create.
    val expiration = remember {
        ExpirationManager.getExpirationTime(context)
            ?.takeIf { it.isAfter(ZonedDateTime.now()) }
            ?: ZonedDateTime.now().plusHours(1).plusMinutes(56)
    }
    val expirationText = remember(expiration) {
        expiration.format(DateTimeFormatter.ofPattern("M/d/yyyy, h:mm a"))
    }

    var showSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ticket Details", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SheetBackground)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                AccentButton("Activate ticket") { showSheet = true }
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
            Spacer(Modifier.height(36.dp))
            LePassCard(modifier = Modifier.padding(horizontal = 6.dp))
            Spacer(Modifier.height(30.dp))
            Text("Expiration date", color = SecondaryText, fontSize = 13.sp)
            Text(expirationText, color = PrimaryText, fontSize = 15.sp)
            Spacer(Modifier.height(24.dp))
            Text("Having trouble?", color = PrimaryText, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            LinkRow(Icons.Filled.PhoneInTalk, "Call customer support")
            LinkRow(Icons.Filled.Email, "Send email")
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ticket info", color = PrimaryText, fontSize = 15.sp)
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = PrimaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = SheetBackground,
            shape = RoundedCornerShape(0.dp),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TicketGlyph(Modifier.size(width = 80.dp, height = 70.dp))
                Spacer(Modifier.height(28.dp))
                Text(
                    "Activate this ticket?",
                    color = PrimaryText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "It’s best to activate just before boarding, as you can’t undo it.",
                    color = SecondaryText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                AccentButton("Activate") {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showSheet = false
                        onActivate()
                    }
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                }) {
                    Text("Activate later", color = Accent, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun AccentButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = OnAccent)
    ) {
        Text(text, fontSize = 16.sp, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun LinkRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        modifier = Modifier
            .clickable {}
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = Accent, fontSize = 14.sp)
    }
}

/** Lavender gradient pass card with the "Le Pass / RTA" watermark. */
@Composable
private fun LePassCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.66f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFE6E7F3), Color(0xFFB4B8E4), Color(0xFF8C93D2)),
                    start = Offset(0f, Float.POSITIVE_INFINITY),
                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                )
            )
    ) {
        val watermark = Color.White.copy(alpha = 0.35f)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                "Le Pass",
                color = watermark,
                fontSize = 64.sp,
                fontFamily = FontFamily.Cursive,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 4.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 40.dp)
            ) {
                Text(
                    "RTA",
                    color = watermark,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                )
                Spacer(Modifier.width(8.dp))
                Chevrons(listOf(watermark, watermark), Modifier.size(48.dp))
            }
        }
        Column(Modifier.padding(16.dp)) {
            Text(
                "Adult Single Ride, Bus & Streetcar",
                color = Color(0xFF1E1E24),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text("$1.25", color = Color(0xFF3A3A44), fontSize = 13.sp)
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Chevrons(listOf(Color(0xFF5B2C83), Color(0xFF4E8A2E)), Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text("New Orleans RTA", color = Color(0xFF1E1E24), fontSize = 13.sp)
        }
    }
}

/** Double chevron in the RTA mark style; one color per chevron. */
@Composable
private fun Chevrons(colors: List<Color>, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        fun chevron(x: Float) = Path().apply {
            moveTo(x, 0f)
            lineTo(x + w * 0.3f, 0f)
            lineTo(x + w * 0.6f, h / 2f)
            lineTo(x + w * 0.3f, h)
            lineTo(x, h)
            lineTo(x + w * 0.3f, h / 2f)
            close()
        }
        drawPath(chevron(0f), colors[0])
        drawPath(chevron(w * 0.4f), colors[1])
    }
}

/** Tilted blue ticket illustration for the activation sheet. */
@Composable
private fun TicketGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier.rotate(-30f)) {
        val body = Color(0xFF2F8BF0)
        val line = Color(0xFF1E6FD0)
        drawRoundRect(body, cornerRadius = CornerRadius(6.dp.toPx()))
        // Side notches.
        drawCircle(SheetBackground, radius = size.height * 0.12f, center = Offset(0f, size.height / 2))
        drawCircle(SheetBackground, radius = size.height * 0.12f, center = Offset(size.width, size.height / 2))
        val u = size.height / 10f
        drawRect(line, Offset(size.width * 0.14f, u * 2.4f), Size(u * 1.6f, u * 1.6f))
        drawRect(line, Offset(size.width * 0.32f, u * 2.4f), Size(size.width * 0.4f, u * 0.6f))
        drawRect(line, Offset(size.width * 0.32f, u * 3.6f), Size(size.width * 0.3f, u * 0.6f))
        drawRect(line, Offset(size.width * 0.14f, u * 5.2f), Size(size.width * 0.5f, u * 0.6f))
        drawRoundRect(
            line,
            Offset(size.width * 0.14f, u * 6.6f),
            Size(size.width * 0.72f, u * 1.4f),
            CornerRadius(u * 0.7f)
        )
    }
}

@Preview(widthDp = 360, heightDp = 740)
@Composable
fun TicketDetailsScreenPreview() {
    LeFauxPassTheme { TicketDetailsScreen(onActivate = {}) }
}
