package com.hereliesaz.lefauxpass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hereliesaz.lefauxpass.ui.theme.MediumGrayTextColor
import com.hereliesaz.lefauxpass.ui.theme.TopBarColor
import kotlinx.coroutines.delay

/** The activated ticket: live clock, expiration, and the looping animation. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RtaTicketScreen(onBack: () -> Unit = {}) {
    val platform = LocalPlatform.current
    var expirationTime by remember { mutableStateOf<Long?>(null) }
    var isVideoReady by remember { mutableStateOf(false) }

    platform.SystemBarColor(TopBarColor)

    LaunchedEffect(Unit) {
        var stored = platform.loadExpiration()
        if (stored == null || stored < nowMillis()) {
            stored = nowMillis() + TICKET_LIFETIME_MS
            platform.saveExpiration(stored)
        }
        expirationTime = stored
        platform.checkForUpdates?.invoke(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    platform.checkForUpdates?.let { check ->
                        IconButton(onClick = { check(true) }) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Information",
                                tint = Color(0xFFB865D0)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarColor,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .alpha(if (isVideoReady) 1f else 0f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "RTA",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )

                    Text(
                        text = "Show operator your ticket",
                        fontSize = 16.sp,
                        color = MediumGrayTextColor
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                platform.AnimationPlayer(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .aspectRatio(1f)
                        .background(Color.White),
                    onReady = { isVideoReady = true }
                )
                LiveClock()
                Spacer(modifier = Modifier.height(24.dp))
                TicketInfoCard(expirationTime = expirationTime)
                Spacer(modifier = Modifier.height(20.dp))
                Spacer(modifier = Modifier.weight(1f))
            }
            if (!isVideoReady) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun LiveClock(modifier: Modifier = Modifier) {
    var currentTime by remember { mutableLongStateOf(nowMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = nowMillis()
            delay(1000)
        }
    }

    Text(
        text = TimeFormat.clock(currentTime),
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = Color.Black,
        modifier = modifier.fillMaxWidth(0.75f),
        maxLines = 1
    )
}

@Composable
fun TicketInfoCard(expirationTime: Long?, modifier: Modifier = Modifier) {
    val expirationText = if (expirationTime != null) {
        "Expires ${TimeFormat.long(expirationTime)}"
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
                color = Color.Black
            )
            Text(
                text = "New Orleans, LA",
                color = Color.Black,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = expirationText,
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}
