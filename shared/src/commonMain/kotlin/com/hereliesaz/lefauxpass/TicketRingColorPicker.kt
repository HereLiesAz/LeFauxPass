package com.hereliesaz.lefauxpass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val ColorRequestEmail = "hereliesaz@gmail.com"
private val WheelMarkerRadius = 8.dp

/** Edits a ring color locally until [onApply] commits the chosen sRGB color. */
@Composable
internal fun TicketRingColorPicker(
    currentColor: Color,
    onDismiss: () -> Unit,
    onApply: (Color) -> Unit,
) {
    var hsv by remember(currentColor) { mutableStateOf(WheelColor.from(currentColor)) }
    var hex by remember(currentColor) { mutableStateOf(currentColor.toRgbHex()) }
    var emailUnavailable by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val selectedColor = hex.toRingColorOrNull()
    val updateWheel: (WheelColor) -> Unit = { next ->
        hsv = next
        hex = next.toColor().toRgbHex()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ring color") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RingColorWheel(
                    color = hsv,
                    onPick = { hue, saturation -> updateWheel(hsv.copy(hue = hue, saturation = saturation)) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                TextButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        try {
                            uriHandler.openUri(
                                "mailto:$ColorRequestEmail?subject=LeFauxPass%20ring%20color%20request" +
                                    "&body=Requested%20ring%20color%3A%20",
                            )
                        } catch (_: IllegalArgumentException) {
                            emailUnavailable = true
                        }
                    },
                ) { Text("Need more?") }
                if (emailUnavailable) {
                    Text("Send your color suggestion to $ColorRequestEmail.")
                }
                Text("Brightness")
                Slider(
                    value = hsv.value,
                    onValueChange = { updateWheel(hsv.copy(value = it)) },
                    modifier = Modifier.semantics { contentDescription = "Brightness" },
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier.size(32.dp)
                            .background(selectedColor ?: hsv.toColor(), CircleShape)
                            .border(1.dp, Color.Gray, CircleShape),
                    )
                    OutlinedTextField(
                        value = hex,
                        onValueChange = { input ->
                            hex = input.trim().removePrefix("#").uppercase()
                            hex.toRingColorOrNull()?.let { hsv = WheelColor.from(it) }
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Hex color") },
                        prefix = { Text("#") },
                        singleLine = true,
                        isError = selectedColor == null,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedColor != null,
                onClick = { selectedColor?.let(onApply) },
            ) { Text("Apply") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** Draws hue clockwise from the right and saturation outward from the center. */
@Composable
private fun RingColorWheel(
    color: WheelColor,
    onPick: (hue: Float, saturation: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pick by rememberUpdatedState(onPick)
    val hues = remember { (0..6).map { Color.hsv(it % 6 * 60f, 1f, 1f) } }
    Canvas(
        modifier.widthIn(max = 220.dp).fillMaxWidth().aspectRatio(1f)
            .semantics { contentDescription = "Hue and saturation wheel" }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = center.x - WheelMarkerRadius.toPx()
                    if (radius <= 0f || (down.position - center).getDistance() > center.x) {
                        return@awaitEachGesture
                    }
                    down.consume()
                    fun pickAt(position: Offset) {
                        val delta = position - center
                        val hue = (atan2(delta.y, delta.x) * (180f / PI.toFloat()) + 360f) % 360f
                        pick(hue, (delta.getDistance() / radius).coerceIn(0f, 1f))
                    }
                    pickAt(down.position)
                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (change.isConsumed) break
                        if (change.position != change.previousPosition) pickAt(change.position)
                        change.consume()
                        if (!change.pressed) break
                    }
                }
            },
    ) {
        val radius = size.minDimension / 2f - WheelMarkerRadius.toPx()
        if (radius <= 0f) return@Canvas
        drawCircle(Brush.sweepGradient(hues, center), radius)
        drawCircle(Brush.radialGradient(listOf(Color.White, Color.White.copy(alpha = 0f)), center, radius), radius)
        drawCircle(Color.Black.copy(alpha = 1f - color.value), radius)
        val radians = color.hue * (PI.toFloat() / 180f)
        val marker = center + Offset(cos(radians), sin(radians)) * (radius * color.saturation)
        drawCircle(Color.Black, WheelMarkerRadius.toPx(), marker)
        drawCircle(Color.White, 6.dp.toPx(), marker)
        drawCircle(color.toColor(), 4.dp.toPx(), marker)
    }
}

/** Picker coordinates in HSV. [hue] is in degrees and the other channels are in 0..1. */
private data class WheelColor(val hue: Float, val saturation: Float, val value: Float) {
    fun toColor(): Color = Color.hsv(hue, saturation, value)

    companion object {
        /** Converts the current sRGB color into wheel and brightness coordinates. */
        fun from(color: Color): WheelColor {
            val (red, green, blue) = Triple(color.red, color.green, color.blue)
            val value = maxOf(red, green, blue)
            val chroma = value - minOf(red, green, blue)
            if (chroma == 0f) return WheelColor(0f, 0f, value)
            val sector = when (value) {
                red -> (green - blue) / chroma
                green -> (blue - red) / chroma + 2f
                else -> (red - green) / chroma + 4f
            }
            return WheelColor((sector * 60f + 360f) % 360f, chroma / value, value)
        }
    }
}

/** Formats the opaque sRGB color for the editable hex field. */
private fun Color.toRgbHex(): String =
    (toArgb() and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()

/** Accepts a complete six-digit sRGB hex value and supplies full opacity. */
private fun String.toRingColorOrNull(): Color? {
    if (length != 6 || any { it !in "0123456789ABCDEF" }) return null
    return toIntOrNull(16)?.let { Color(it or 0xFF000000.toInt()) }
}
