package com.hereliesaz.lefauxpass

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.hereliesaz.lefauxpass.shared.resources.Res
import com.hereliesaz.lefauxpass.shared.resources.ticket_animation_logo
import org.jetbrains.compose.resources.imageResource
import kotlin.math.roundToInt

/** Colors for the ticket animation's circle layers, with a darker inset of the selected ring. */
internal data class TicketAnimationColors(
    val ring: Color = Color(0xFFFF9EC4),
    val paper: Color = Color.White,
    val background: Color = Color.White,
    val shadow: Color = Color.Black,
) {
    val ringInset: Color = Color(ring.red * 0.8f, ring.green * 0.8f, ring.blue * 0.8f)
}

/** Plays the reference WebP's measured circle poses at their original frame boundaries. */
@Composable
internal fun TicketAnimation(
    modifier: Modifier = Modifier,
    colors: TicketAnimationColors = TicketAnimationColors(),
) {
    val logo = imageResource(Res.drawable.ticket_animation_logo)
    val frameIndex = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                val elapsed = ((now - start) / 1_000_000L % frameEnds.last()).toInt()
                frameIndex.intValue = frameEnds.indexOfFirst { it > elapsed }
            }
        }
    }

    Canvas(modifier) {
        val frame = frames[frameIndex.intValue]
        val scale = size.minDimension / 434f
        if (scale <= 0f) return@Canvas
        val origin = Offset((size.width - 434f * scale) / 2, (size.height - 434f * scale) / 2)
        val center = origin + Offset(216.25f, 220.5f) * scale
        val paperCenter = origin + Offset(216.25f, 219.5f) * scale
        val paperRadius = 115f * scale
        val shadowCenter = paperCenter + Offset(0f, 2.5f * scale)

        drawRect(colors.background)
        if (frame.haloRadius > 0) {
            drawCircle(
                colors.shadow,
                radius = frame.haloRadius * scale,
                center = center,
                alpha = frame.haloAlpha,
            )
        }
        drawCircle(colors.ring, radius = frame.ringRadius * scale, center = center)
        if (frame.insetRadius > 0) {
            drawCircle(colors.ringInset, radius = frame.insetRadius * scale, center = center)
        }
        drawCircle(
            Brush.radialGradient(
                0.9f to colors.shadow.copy(alpha = 0.26f),
                1f to colors.shadow.copy(alpha = 0f),
                center = shadowCenter,
                radius = 123f * scale,
            ),
            radius = 123f * scale,
            center = shadowCenter,
        )
        drawCircle(colors.paper, radius = paperRadius, center = paperCenter)
        if (logo.width > 0 && logo.height > 0) {
            drawImage(
                image = logo,
                srcSize = IntSize(logo.width, logo.height),
                dstOffset = IntOffset(
                    (origin.x + 142f * scale).roundToInt(),
                    (origin.y + 194f * scale).roundToInt(),
                ),
                dstSize = IntSize(
                    (148f * scale).roundToInt().coerceAtLeast(1),
                    (54f * scale).roundToInt().coerceAtLeast(1),
                ),
            )
        }
    }
}

private data class Frame(
    val duration: Int,
    val ringRadius: Float,
    val insetRadius: Float,
    val haloRadius: Float,
    val haloAlpha: Float,
)

// One pose per decoded frame of LeFauxPassRN/assets/animation.webp.
// Columns: duration (ms), outer radius, inset radius, halo radius (all on the 434px canvas), halo opacity.
private val frames = arrayOf(
    Frame(33, 210.9f, 0.0f, 0.0f, 0.000f),
    Frame(33, 213.8f, 0.0f, 0.0f, 0.000f),
    Frame(33, 213.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 208.9f, 0.0f, 0.0f, 0.000f),
    Frame(33, 204.7f, 0.0f, 0.0f, 0.000f),
    Frame(33, 200.4f, 0.0f, 0.0f, 0.000f),
    Frame(33, 196.2f, 0.0f, 0.0f, 0.000f),
    Frame(33, 191.9f, 0.0f, 0.0f, 0.000f),
    Frame(33, 187.7f, 0.0f, 0.0f, 0.000f),
    Frame(33, 185.5f, 0.0f, 0.0f, 0.000f),
    Frame(33, 181.3f, 0.0f, 0.0f, 0.000f),
    Frame(33, 177.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 172.8f, 123.1f, 0.0f, 0.000f),
    Frame(33, 168.6f, 125.9f, 0.0f, 0.000f),
    Frame(33, 164.3f, 128.4f, 0.0f, 0.000f),
    Frame(33, 158.0f, 132.6f, 0.0f, 0.000f),
    Frame(33, 153.7f, 135.6f, 0.0f, 0.000f),
    Frame(33, 147.4f, 139.6f, 0.0f, 0.000f),
    Frame(33, 145.3f, 140.9f, 0.0f, 0.000f),
    Frame(33, 141.1f, 139.1f, 144.1f, 0.165f),
    Frame(33, 136.8f, 134.9f, 146.9f, 0.200f),
    Frame(33, 132.5f, 130.6f, 149.6f, 0.201f),
    Frame(33, 130.2f, 128.1f, 150.1f, 0.200f),
    Frame(33, 132.1f, 130.1f, 149.1f, 0.203f),
    Frame(33, 134.0f, 131.9f, 147.4f, 0.202f),
    Frame(33, 135.9f, 133.9f, 145.6f, 0.202f),
    Frame(33, 137.7f, 135.6f, 143.6f, 0.204f),
    Frame(33, 138.7f, 136.6f, 142.9f, 0.195f),
    Frame(33, 140.4f, 138.4f, 0.0f, 0.000f),
    Frame(33, 142.5f, 139.1f, 0.0f, 0.000f),
    Frame(33, 144.4f, 136.9f, 0.0f, 0.000f),
    Frame(33, 146.3f, 134.9f, 0.0f, 0.000f),
    Frame(33, 148.2f, 133.1f, 0.0f, 0.000f),
    Frame(33, 150.0f, 131.4f, 0.0f, 0.000f),
    Frame(33, 152.0f, 129.4f, 0.0f, 0.000f),
    Frame(33, 153.8f, 127.6f, 0.0f, 0.000f),
    Frame(33, 155.7f, 125.9f, 0.0f, 0.000f),
    Frame(33, 157.6f, 124.1f, 0.0f, 0.000f),
    Frame(33, 159.5f, 122.6f, 0.0f, 0.000f),
    Frame(33, 161.4f, 121.1f, 0.0f, 0.000f),
    Frame(33, 163.3f, 0.0f, 0.0f, 0.000f),
    Frame(33, 165.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 167.0f, 0.0f, 0.0f, 0.000f),
    Frame(33, 168.2f, 0.0f, 0.0f, 0.000f),
    Frame(33, 167.5f, 0.0f, 0.0f, 0.000f),
    Frame(33, 166.7f, 0.0f, 0.0f, 0.000f),
    Frame(33, 166.0f, 0.0f, 0.0f, 0.000f),
    Frame(33, 165.2f, 0.0f, 0.0f, 0.000f),
    Frame(33, 164.4f, 0.0f, 0.0f, 0.000f),
    Frame(33, 163.7f, 0.0f, 0.0f, 0.000f),
    Frame(33, 162.9f, 0.0f, 0.0f, 0.000f),
    Frame(33, 162.2f, 121.1f, 0.0f, 0.000f),
    Frame(33, 161.5f, 121.9f, 0.0f, 0.000f),
    Frame(33, 160.7f, 122.6f, 0.0f, 0.000f),
    Frame(33, 159.9f, 123.4f, 0.0f, 0.000f),
    Frame(33, 159.2f, 124.1f, 0.0f, 0.000f),
    Frame(33, 158.4f, 124.9f, 0.0f, 0.000f),
    Frame(33, 157.3f, 126.1f, 0.0f, 0.000f),
    Frame(33, 156.5f, 126.9f, 0.0f, 0.000f),
    Frame(33, 155.8f, 127.6f, 0.0f, 0.000f),
    Frame(33, 155.1f, 128.4f, 0.0f, 0.000f),
    Frame(33, 154.3f, 129.4f, 0.0f, 0.000f),
    Frame(33, 155.7f, 128.6f, 0.0f, 0.000f),
    Frame(33, 157.7f, 127.4f, 0.0f, 0.000f),
    Frame(33, 159.7f, 125.9f, 0.0f, 0.000f),
    Frame(33, 161.8f, 124.6f, 0.0f, 0.000f),
    Frame(33, 163.9f, 123.6f, 0.0f, 0.000f),
    Frame(33, 165.9f, 122.6f, 0.0f, 0.000f),
    Frame(33, 167.9f, 121.4f, 0.0f, 0.000f),
    Frame(33, 170.0f, 0.0f, 0.0f, 0.000f),
    Frame(33, 172.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 174.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 176.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 178.2f, 0.0f, 0.0f, 0.000f),
    Frame(33, 180.3f, 0.0f, 0.0f, 0.000f),
    Frame(33, 182.3f, 0.0f, 0.0f, 0.000f),
    Frame(33, 184.3f, 0.0f, 0.0f, 0.000f),
    Frame(33, 186.4f, 0.0f, 0.0f, 0.000f),
    Frame(33, 188.4f, 0.0f, 0.0f, 0.000f),
    Frame(33, 190.5f, 0.0f, 0.0f, 0.000f),
    Frame(33, 192.5f, 0.0f, 0.0f, 0.000f),
    Frame(33, 193.6f, 0.0f, 0.0f, 0.000f),
    Frame(33, 195.6f, 0.0f, 0.0f, 0.000f),
    Frame(33, 193.5f, 0.0f, 0.0f, 0.000f),
    Frame(33, 190.8f, 0.0f, 0.0f, 0.000f),
    Frame(33, 188.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 185.3f, 0.0f, 0.0f, 0.000f),
    Frame(33, 182.6f, 0.0f, 0.0f, 0.000f),
    Frame(33, 178.5f, 0.0f, 0.0f, 0.000f),
    Frame(33, 174.4f, 0.0f, 0.0f, 0.000f),
    Frame(33, 173.0f, 0.0f, 0.0f, 0.000f),
    Frame(33, 170.3f, 0.0f, 0.0f, 0.000f),
    Frame(33, 167.6f, 122.1f, 0.0f, 0.000f),
    Frame(33, 164.8f, 123.6f, 0.0f, 0.000f),
    Frame(33, 162.1f, 125.4f, 0.0f, 0.000f),
    Frame(33, 159.4f, 127.4f, 0.0f, 0.000f),
    Frame(33, 156.6f, 129.1f, 0.0f, 0.000f),
    Frame(33, 153.9f, 130.9f, 0.0f, 0.000f),
    Frame(33, 151.2f, 132.9f, 0.0f, 0.000f),
    Frame(33, 148.5f, 134.6f, 0.0f, 0.000f),
    Frame(33, 145.8f, 136.4f, 0.0f, 0.000f),
    Frame(33, 143.1f, 138.1f, 0.0f, 0.000f),
    Frame(33, 140.2f, 138.1f, 0.0f, 0.000f),
    Frame(33, 144.2f, 137.9f, 0.0f, 0.000f),
    Frame(33, 148.1f, 135.6f, 0.0f, 0.000f),
    Frame(33, 152.1f, 133.1f, 0.0f, 0.000f),
    Frame(66, 158.0f, 129.6f, 0.0f, 0.000f),
    Frame(33, 162.0f, 127.4f, 0.0f, 0.000f),
    Frame(33, 165.9f, 124.9f, 0.0f, 0.000f),
    Frame(33, 169.9f, 122.9f, 0.0f, 0.000f),
    Frame(33, 173.8f, 121.1f, 0.0f, 0.000f),
    Frame(33, 177.8f, 0.0f, 0.0f, 0.000f),
    Frame(33, 181.7f, 0.0f, 0.0f, 0.000f),
    Frame(33, 185.7f, 0.0f, 0.0f, 0.000f),
    Frame(33, 189.6f, 0.0f, 0.0f, 0.000f),
    Frame(33, 193.6f, 0.0f, 0.0f, 0.000f),
    Frame(33, 197.5f, 0.0f, 0.0f, 0.000f),
    Frame(33, 201.4f, 0.0f, 0.0f, 0.000f),
    Frame(33, 205.4f, 0.0f, 0.0f, 0.000f),
    Frame(33, 209.4f, 0.0f, 0.0f, 0.000f),
    Frame(33, 213.2f, 0.0f, 0.0f, 0.000f),
    Frame(33, 213.8f, 0.0f, 0.0f, 0.000f),
    Frame(33, 210.5f, 0.0f, 0.0f, 0.000f),
    Frame(33, 206.3f, 0.0f, 0.0f, 0.000f),
    Frame(33, 202.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 197.8f, 0.0f, 0.0f, 0.000f),
    Frame(33, 193.5f, 0.0f, 0.0f, 0.000f),
    Frame(33, 189.3f, 0.0f, 0.0f, 0.000f),
    Frame(33, 185.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 180.8f, 0.0f, 0.0f, 0.000f),
    Frame(33, 176.6f, 121.1f, 0.0f, 0.000f),
    Frame(33, 172.4f, 123.6f, 0.0f, 0.000f),
    Frame(33, 168.1f, 126.1f, 0.0f, 0.000f),
    Frame(33, 163.9f, 128.9f, 0.0f, 0.000f),
    Frame(33, 159.6f, 131.6f, 0.0f, 0.000f),
    Frame(33, 155.4f, 134.4f, 0.0f, 0.000f),
    Frame(33, 151.2f, 137.1f, 0.0f, 0.000f),
    Frame(33, 146.9f, 139.9f, 0.0f, 0.000f),
    Frame(33, 142.6f, 140.6f, 0.0f, 0.000f),
    Frame(33, 138.5f, 136.4f, 145.9f, 0.203f),
    Frame(33, 134.2f, 132.1f, 148.6f, 0.200f),
    Frame(33, 129.9f, 127.9f, 149.9f, 0.200f),
    Frame(33, 131.4f, 129.4f, 149.9f, 0.201f),
    Frame(33, 133.2f, 131.1f, 148.1f, 0.201f),
    Frame(33, 135.1f, 133.1f, 146.1f, 0.202f),
    Frame(33, 137.0f, 134.9f, 144.4f, 0.203f),
    Frame(33, 138.9f, 136.9f, 142.6f, 0.191f),
    Frame(33, 140.6f, 138.6f, 0.0f, 0.000f),
    Frame(33, 142.7f, 138.4f, 0.0f, 0.000f),
    Frame(33, 144.5f, 136.6f, 0.0f, 0.000f),
    Frame(33, 146.5f, 134.6f, 0.0f, 0.000f),
    Frame(33, 148.4f, 132.9f, 0.0f, 0.000f),
    Frame(33, 150.2f, 131.1f, 0.0f, 0.000f),
    Frame(33, 152.2f, 129.4f, 0.0f, 0.000f),
    Frame(33, 154.0f, 127.4f, 0.0f, 0.000f),
    Frame(33, 155.9f, 125.6f, 0.0f, 0.000f),
    Frame(33, 157.8f, 123.9f, 0.0f, 0.000f),
    Frame(33, 159.6f, 122.4f, 0.0f, 0.000f),
    Frame(33, 161.6f, 0.0f, 0.0f, 0.000f),
    Frame(33, 163.5f, 0.0f, 0.0f, 0.000f),
    Frame(33, 165.3f, 0.0f, 0.0f, 0.000f),
    Frame(33, 167.2f, 0.0f, 0.0f, 0.000f),
    Frame(33, 168.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 167.4f, 0.0f, 0.0f, 0.000f),
    Frame(33, 166.6f, 0.0f, 0.0f, 0.000f),
    Frame(33, 165.9f, 0.0f, 0.0f, 0.000f),
    Frame(33, 165.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 164.4f, 0.0f, 0.0f, 0.000f),
    Frame(33, 163.6f, 0.0f, 0.0f, 0.000f),
    Frame(33, 162.8f, 0.0f, 0.0f, 0.000f),
    Frame(33, 162.1f, 121.1f, 0.0f, 0.000f),
    Frame(33, 161.4f, 121.9f, 0.0f, 0.000f),
    Frame(33, 160.6f, 122.6f, 0.0f, 0.000f),
    Frame(33, 159.8f, 123.4f, 0.0f, 0.000f),
    Frame(33, 159.1f, 124.1f, 0.0f, 0.000f),
    Frame(33, 158.4f, 124.9f, 0.0f, 0.000f),
    Frame(33, 157.6f, 125.9f, 0.0f, 0.000f),
    Frame(33, 156.8f, 126.4f, 0.0f, 0.000f),
    Frame(33, 156.1f, 127.4f, 0.0f, 0.000f),
    Frame(33, 155.4f, 128.1f, 0.0f, 0.000f),
    Frame(33, 154.6f, 128.9f, 0.0f, 0.000f),
    Frame(33, 154.8f, 129.1f, 0.0f, 0.000f),
    Frame(33, 156.9f, 127.9f, 0.0f, 0.000f),
    Frame(33, 158.9f, 126.4f, 0.0f, 0.000f),
    Frame(33, 162.0f, 124.6f, 0.0f, 0.000f),
    Frame(33, 164.1f, 123.4f, 0.0f, 0.000f),
    Frame(33, 166.1f, 122.4f, 0.0f, 0.000f),
    Frame(33, 168.1f, 121.4f, 0.0f, 0.000f),
    Frame(33, 170.2f, 0.0f, 0.0f, 0.000f),
    Frame(33, 172.3f, 0.0f, 0.0f, 0.000f),
    Frame(33, 174.3f, 0.0f, 0.0f, 0.000f),
    Frame(33, 176.3f, 0.0f, 0.0f, 0.000f),
    Frame(33, 178.4f, 0.0f, 0.0f, 0.000f),
    Frame(33, 180.4f, 0.0f, 0.0f, 0.000f),
    Frame(33, 182.5f, 0.0f, 0.0f, 0.000f),
    Frame(33, 184.6f, 0.0f, 0.0f, 0.000f),
    Frame(33, 186.6f, 0.0f, 0.0f, 0.000f),
    Frame(33, 188.6f, 0.0f, 0.0f, 0.000f),
    Frame(33, 190.7f, 0.0f, 0.0f, 0.000f),
    Frame(33, 192.8f, 0.0f, 0.0f, 0.000f),
    Frame(33, 194.8f, 0.0f, 0.0f, 0.000f),
    Frame(33, 194.6f, 0.0f, 0.0f, 0.000f),
    Frame(33, 191.9f, 0.0f, 0.0f, 0.000f),
    Frame(33, 189.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 186.4f, 0.0f, 0.0f, 0.000f),
    Frame(33, 183.6f, 0.0f, 0.0f, 0.000f),
    Frame(33, 180.9f, 0.0f, 0.0f, 0.000f),
    Frame(33, 178.2f, 0.0f, 0.0f, 0.000f),
    Frame(33, 175.5f, 0.0f, 0.0f, 0.000f),
    Frame(33, 172.8f, 0.0f, 0.0f, 0.000f),
    Frame(33, 170.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 167.3f, 122.4f, 0.0f, 0.000f),
    Frame(33, 164.6f, 123.9f, 0.0f, 0.000f),
    Frame(33, 161.9f, 125.6f, 0.0f, 0.000f),
    Frame(33, 159.1f, 127.6f, 0.0f, 0.000f),
    Frame(33, 156.4f, 129.4f, 0.0f, 0.000f),
    Frame(33, 153.7f, 131.1f, 0.0f, 0.000f),
    Frame(33, 150.9f, 133.1f, 0.0f, 0.000f),
    Frame(33, 148.2f, 134.9f, 0.0f, 0.000f),
    Frame(33, 145.5f, 136.6f, 0.0f, 0.000f),
    Frame(33, 142.8f, 138.4f, 0.0f, 0.000f),
    Frame(33, 140.5f, 138.6f, 0.0f, 0.000f),
    Frame(33, 144.6f, 137.9f, 0.0f, 0.000f),
    Frame(33, 148.5f, 135.4f, 0.0f, 0.000f),
    Frame(33, 152.5f, 132.9f, 0.0f, 0.000f),
    Frame(33, 156.4f, 130.6f, 0.0f, 0.000f),
    Frame(33, 158.4f, 129.4f, 0.0f, 0.000f),
    Frame(33, 162.3f, 127.1f, 0.0f, 0.000f),
    Frame(33, 166.3f, 124.9f, 0.0f, 0.000f),
    Frame(33, 170.3f, 122.9f, 0.0f, 0.000f),
    Frame(33, 174.2f, 0.0f, 0.0f, 0.000f),
    Frame(33, 178.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 182.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 186.1f, 0.0f, 0.0f, 0.000f),
    Frame(33, 190.0f, 0.0f, 0.0f, 0.000f),
    Frame(33, 193.9f, 0.0f, 0.0f, 0.000f),
    Frame(33, 197.9f, 0.0f, 0.0f, 0.000f),
)

private val frameEnds = IntArray(frames.size).also { ends ->
    var end = 0
    frames.forEachIndexed { index, frame ->
        end += frame.duration
        ends[index] = end
    }
}
