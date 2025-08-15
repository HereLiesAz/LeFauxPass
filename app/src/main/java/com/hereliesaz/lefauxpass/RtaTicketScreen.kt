package com.hereliesaz.lefauxpass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import com.hereliesaz.lefauxpass.ui.theme.LeFauxPassTheme
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RtaTicketScreen() {
    val isInPreview = LocalInspectionMode.current
    val context = LocalContext.current
    var expirationTime by remember { mutableStateOf<ZonedDateTime?>(null) }

    LaunchedEffect(Unit) {
        if (isInPreview) {
            expirationTime = ZonedDateTime.now().plusHours(1).plusMinutes(56)
        } else {
            var storedExpiration = ExpirationManager.getExpirationTime(context)
            if (storedExpiration == null || storedExpiration.isBefore(ZonedDateTime.now())) {
                storedExpiration = ZonedDateTime.now().plusHours(1).plusMinutes(56)
                ExpirationManager.setExpirationTime(context, storedExpiration)
            }
            expirationTime = storedExpiration
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { /* Do nothing, it's a picture */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Also does nothing */ }) {
                        Icon(Icons.Outlined.Info, contentDescription = "Information")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGray
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            val (rtaText, rtaLogo, liveClock, ticketCard) = createRefs()
            val topGuideline = createGuidelineFromTop(0.5f)

            Text(
                text = "RTA",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.constrainAs(rtaText) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            AsyncImage(
                model = R.raw.animationwebp,
                contentDescription = "Animation",
                modifier = Modifier.constrainAs(rtaLogo) {
                    top.linkTo(rtaText.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            LiveClock(
                modifier = Modifier.constrainAs(liveClock) {
                    top.linkTo(rtaLogo.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            TicketInfoCard(
                expirationTime = expirationTime,
                modifier = Modifier.constrainAs(ticketCard) {
                    top.linkTo(topGuideline)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )
        }
    }
}

@Composable
fun LiveClock(modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }

    val formatter = remember { DateTimeFormatter.ofPattern("hh:mm:ss a") }

    Text(
        text = currentTime.format(formatter),
        fontSize = 64.sp,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth(0.6f)
    )
}

@Composable
fun TicketInfoCard(expirationTime: ZonedDateTime?, modifier: Modifier = Modifier) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy, h:mm a") }
    val expirationText = if (expirationTime != null) {
        "Expires ${expirationTime.format(formatter)}"
    } else {
        "Loading..."
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Adult Single Ride, Bus & Streetcar",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "New Orleans, LA",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = expirationText,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun RtaTicketScreenPreview() {
    LeFauxPassTheme {
        RtaTicketScreen()
    }
}