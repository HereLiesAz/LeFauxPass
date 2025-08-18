package com.hereliesaz.lefauxpass

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.hereliesaz.lefauxpass.ui.theme.LeFauxPassTheme
import com.hereliesaz.lefauxpass.ui.theme.MediumGrayTextColor
import com.hereliesaz.lefauxpass.ui.theme.RtaPurple
import com.hereliesaz.lefauxpass.ui.theme.TopBarColor
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LeFauxPassTheme {
                RtaTicketScreen()
            }
        }
    }
}

// region RtaTicketScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RtaTicketScreen() {
    val isInPreview = LocalInspectionMode.current
    val context = LocalContext.current
    var expirationTime by remember { mutableStateOf<ZonedDateTime?>(null) }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = TopBarColor.toArgb()
            window.navigationBarColor = TopBarColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

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
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Information",
                            tint = RtaPurple
                        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            Spacer(modifier = Modifier.height(10.dp))
            if (!isInPreview) {
                VideoPlayer(
                    videoRes = R.raw.animation,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .aspectRatio(1f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .aspectRatio(1f)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Video Preview", color = Color.White)
                }
            }
            LiveClock()
            Spacer(modifier = Modifier.height(24.dp))
            TicketInfoCard(expirationTime = expirationTime)
            Spacer(modifier = Modifier.height(10.dp))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayer(modifier: Modifier = Modifier, @RawRes videoRes: Int) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = RawResourceDataSource.buildRawResourceUri(videoRes)
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false // Hide controls
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
        modifier = modifier
    )
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

    val formatter = remember { DateTimeFormatter.ofPattern("h:mm:ss a") }

    Text(
        text = currentTime.format(formatter),
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth(0.75f),
        maxLines = 1
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
// endregion

// region AnimationScreen
private data class KF(
    val t: Long,                 // ms
    val x: Float? = null,        // 0..1 relative
    val y: Float? = null,        // 0..1 relative
    val w: Float? = null,        // width in relative space
    val h: Float? = null,        // height in relative space
    val r: Float? = null,        // rotation degrees
    val sx: Float? = null,       // scaleX
    val sy: Float? = null,       // scaleY
    val alpha: Float? = null,    // 0..1
    val color: Color? = null,
)

private fun lerp(a: Float, b: Float, f: Float) = a + (b - a) * f

private fun interp(prev: KF, next: KF, t: Long): KF {
    val span = (next.t - prev.t).coerceAtLeast(1L).toFloat()
    val f = ((t - prev.t).coerceIn(0, (next.t - prev.t)) / span)
    fun v(get: (KF) -> Float?): Float? {
        val a = get(prev);
        val b = get(next)
        return when {
            a != null && b != null -> lerp(a, b, f)
            t >= next.t -> b
            else -> a
        }
    }
    return KF(
        t = t,
        x = v { it.x },
        y = v { it.y },
        w = v { it.w },
        h = v { it.h },
        r = v { it.r },
        sx = v { it.sx } ?: 1f,
        sy = v { it.sy } ?: 1f,
        alpha = v { it.alpha } ?: 1f,
        color = if (prev.color != null && next.color != null)
            prev.color.lerp(next.color, f) else next.color ?: prev.color
    )
}

private fun List<KF>.sample(t: Long): KF {
    if (isEmpty()) return KF(t)
    if (t <= first().t) return first()
    if (t >= last().t) return last()
    val i = indexOfLast { it.t <= t }.coerceAtLeast(0)
    return interp(this[i], this[i + 1], t)
}

private fun Color.lerp(to: Color, f: Float): Color {
    return Color(
        red = lerp(this.red, to.red, f),
        green = lerp(this.green, to.green, f),
        blue = lerp(this.blue, to.blue, f),
        alpha = lerp(this.alpha, to.alpha, f),
    )
}

private sealed interface Node {
    val name: String
    val kf: List<KF>
    fun draw(scope: DrawScope, prog: KF)
}

private class CircleNode(
    override val name: String,
    override val kf: List<KF>,
) : Node {
    override fun draw(scope: DrawScope, prog: KF) = with(scope) {
        val a = (prog.alpha ?: 1f).coerceIn(0f, 1f)
        if (a <= 0f) return
        val px = (prog.x ?: 0.5f) * size.width
        val py = (prog.y ?: 0.5f) * size.height
        val w = (prog.w ?: 0.2f) * size.minDimension
        val scale = ((prog.sx ?: 1f) + (prog.sy ?: 1f)) / 2f
        val radius = (w * scale) / 2f
        drawCircle(
            color = prog.color ?: Color.White,
            radius = radius,
            center = Offset(px, py),
            alpha = a
        )
    }
}

private class RectNode(
    override val name: String,
    override val kf: List<KF>,
    private val round: Float = 0f,
) : Node {
    override fun draw(scope: DrawScope, prog: KF) = with(scope) {
        val a = (prog.alpha ?: 1f).coerceIn(0f, 1f)
        if (a <= 0f) return
        val px = (prog.x ?: 0.5f) * size.width
        val py = (prog.y ?: 0.5f) * size.height
        val w = (prog.w ?: 0.3f) * size.width
        val h = (prog.h ?: 0.1f) * size.height
        val cx = px - w / 2f
        val cy = py - h / 2f
        rotate(prog.r ?: 0f, pivot = Offset(px, py)) {
            drawRoundRect(
                color = prog.color ?: Color.White,
                topLeft = Offset(cx, cy),
                size = Size(w * (prog.sx ?: 1f), h * (prog.sy ?: 1f)),
                cornerRadius = CornerRadius(round, round),
                alpha = a
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        ReconstructionScreen()
    }
}

private class TriangleNode(
    override val name: String,
    override val kf: List<KF>,
) : Node {
    override fun draw(scope: DrawScope, prog: KF) = with(scope) {
        val a = (prog.alpha ?: 1f).coerceIn(0f, 1f)
        if (a <= 0f) return
        val px = (prog.x ?: 0.5f) * size.width
        val py = (prog.y ?: 0.5f) * size.height
        val w = (prog.w ?: 0.2f) * size.width
        val h = (prog.h ?: 0.2f) * size.height
        val halfW = w / 2f

        val path = Path().apply {
            moveTo(px, py - h / 2f)
            lineTo(px - halfW, py + h / 2f)
            lineTo(px + halfW, py + h / 2f)
            close()
        }
        rotate(prog.r ?: 0f, pivot = Offset(px, py)) {
            drawPath(path, color = prog.color ?: Color.White, alpha = a)
        }
    }
}

private data class Scene(val durationMs: Long, val nodes: List<Node>)

private fun rtaScene(): Scene {
    val outerCircle = CircleNode(
        name = "outerCircle",
        kf = listOf(
            KF(0, w = 0.45f, color = Color(0xFFFBC02D)),
            KF(1000, w = 0.5f),
            KF(2000, w = 0.45f),
        )
    )
    val innerCircle = CircleNode(
        name = "innerCircle",
        kf = listOf(
            KF(0, w = 0.4f, color = Color.White),
            KF(1000, w = 0.42f),
            KF(2000, w = 0.4f),
        )
    )

    return Scene(
        durationMs = 2000,
        nodes = listOf(outerCircle, innerCircle)
    )
}

@Composable
fun ReconstructionScreen() {
    val scene = remember { rtaScene() }
    val playhead = remember { Animatable(0f) }
    var loopCount by remember { mutableIntStateOf(0) }
    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("h:mm:ss a", Locale.US)
            currentTime = sdf.format(Date())
            delay(1000)
        }
    }

    LaunchedEffect(loopCount) {
        playhead.snapTo(0f)
        playhead.animateTo(
            1f,
            animationSpec = tween(durationMillis = scene.durationMs.toInt())
        )
        loopCount++
    }

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val t = (playhead.value * scene.durationMs).toLong()
                    scene.nodes.forEach { node ->
                        val prog = node.kf.sample(t)
                        node.draw(this, prog)
                    }
                }
                RtaLogoContent()
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = currentTime,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Adult Single Ride, Bus & Streetcar",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "New Orleans, LA",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Expires Jun 22, 2023, 1:03 PM",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


@Composable
private fun RtaLogoContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "RTA",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color(0xFF6A1B9A),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
// endregion