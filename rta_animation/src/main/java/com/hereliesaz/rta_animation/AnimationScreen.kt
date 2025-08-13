package com.hereliesaz.rta_animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Minimal scene-graph + keyframe system. Define elements, feed keyframes,
 * the engine does the rest.
 */

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
            prev.color!!.lerp(next.color!!, f) else next.color ?: prev.color
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
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(round, round),
                alpha = a
            )
        }
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

/**
 * Example scene:
 * - Pulsing background circle
 * - Sliding card
 * - Rotating accent triangle
 * - Title + subtitle fade/slide
 *
 * Replace these keyframes to match your video’s exact motion/tempo/colors.
 */
private fun sampleScene(): Scene {
    val bg = CircleNode(
        name = "bgPulse",
        kf = listOf(
            KF(0, x = .5f, y = .5f, w = 1.6f, alpha = 0f, color = Color(0xFF0E0F13)),
            KF(200, x = .5f, y = .5f, w = 1.6f, alpha = 1f),
            KF(1200, x = .5f, y = .5f, w = 1.8f, alpha = 1f),
            KF(1600, x = .5f, y = .5f, w = 1.6f, alpha = 1f),
        )
    )
    val card = RectNode(
        name = "card",
        round = 24f,
        kf = listOf(
            KF(
                0,
                x = .5f,
                y = 1.2f,
                w = .86f,
                h = .32f,
                r = 0f,
                alpha = 0f,
                color = Color(0xFF161923)
            ),
            KF(350, x = .5f, y = .58f, w = .86f, h = .32f, r = 0f, alpha = 1f),
            KF(900, x = .5f, y = .56f, w = .86f, h = .32f, r = 0f, alpha = 1f),
            KF(1200, x = .5f, y = .58f, w = .86f, h = .32f, r = 0f, alpha = 1f),
        )
    )
    val accent = TriangleNode(
        name = "accent",
        kf = listOf(
            KF(
                200,
                x = .82f,
                y = .28f,
                w = .10f,
                h = .12f,
                r = 0f,
                alpha = 0f,
                color = Color(0xFF4FC3F7)
            ),
            KF(450, x = .82f, y = .28f, w = .10f, h = .12f, r = 0f, alpha = 1f),
            KF(1400, x = .82f, y = .28f, w = .10f, h = .12f, r = 360f, alpha = 1f),
        )
    )
    val pill = RectNode(
        name = "pill",
        round = 999f,
        kf = listOf(
            KF(400, x = .5f, y = .42f, w = .46f, h = .06f, alpha = 0f, color = Color(0xFF263043)),
            KF(700, x = .5f, y = .42f, w = .56f, h = .06f, alpha = 1f),
            KF(1100, x = .5f, y = .42f, w = .50f, h = .06f, alpha = 1f),
        )
    )
    val underline = RectNode(
        name = "underline",
        round = 12f,
        kf = listOf(
            KF(650, x = .5f, y = .68f, w = .00f, h = .008f, alpha = 0f, color = Color(0xFF4FC3F7)),
            KF(900, x = .5f, y = .68f, w = .60f, h = .008f, alpha = 1f),
            KF(1200, x = .5f, y = .68f, w = .52f, h = .008f, alpha = 1f),
        )
    )
    return Scene(
        durationMs = 1800,
        nodes = listOf(bg, card, accent, pill, underline)
    )
}

@Composable
fun ReconstructionScreen() {
    val scene = remember { sampleScene() }
    val playhead = remember { Animatable(0f) }
    var loopCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        // tiny lead-in
        delay(50)
        while (true) {
            playhead.snapTo(0f)
            playhead.animateTo(
                1f,
                animationSpec = tween(durationMillis = scene.durationMs.toInt())
            )
            loopCount++
        }
    }

    Box(Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            val t = (playhead.value * scene.durationMs).toLong()
            // draw order = z-order
            scene.nodes.forEach { node ->
                val prog = node.kf.sample(t)
                node.draw(this, prog)
            }
        }

        // Titles overlay (text is separate for crispness)
        TitlesOverlay(playhead = playhead.value)
    }
}

@Composable
private fun TitlesOverlay(playhead: Float) {
    // Text keyframes (simple)
    val tMs = (playhead * 1800).toLong()
    val titleAlpha = when {
        tMs < 420 -> 0f
        tMs < 700 -> ((tMs - 420).toFloat() / 280f).coerceIn(0f, 1f)
        else -> 1f
    }
    val subAlpha = when {
        tMs < 650 -> 0f
        tMs < 950 -> ((tMs - 650).toFloat() / 300f).coerceIn(0f, 1f)
        else -> 1f
    }
    val slideY = if (tMs < 700) 12 else 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 88.dp)
    ) {
        Spacer(modifier = Modifier.height((24 + slideY).dp))
        Text(
            "Your Title",
            color = Color(0xFFE7ECF4).copy(alpha = titleAlpha),
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Subtitle or Tagline",
            color = Color(0xFFA9B4C7).copy(alpha = subAlpha),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
