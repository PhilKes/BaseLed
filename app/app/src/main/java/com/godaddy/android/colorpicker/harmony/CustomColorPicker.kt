package com.godaddy.android.colorpicker.harmony

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.toDegree
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

@Composable
fun CustomColorPicker(
    modifier: Modifier = Modifier,
    harmonyMode: ColorHarmonyMode,
    color: Color = Color.Red,
    onColorChanged: (HsvColor) -> Unit
) {
    BoxWithConstraints(modifier) {
        Column(
            Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            val hsvColor = remember { mutableStateOf(HsvColor.from(color)) }
            val updatedOnColorChanged by rememberUpdatedState(onColorChanged)

            HarmonyColorPickerWithMagnifiers(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f),
                hsvColor = hsvColor,
                onColorChanged = {
                    hsvColor.value = it
                    updatedOnColorChanged(it)
                },
                harmonyMode = harmonyMode
            )
            Spacer(modifier = Modifier.fillMaxHeight(0.1f))

        }
    }
}

@Composable
private fun HarmonyColorPickerWithMagnifiers(
    modifier: Modifier = Modifier,
    hsvColor: State<HsvColor>,
    onColorChanged: (HsvColor) -> Unit,
    harmonyMode: ColorHarmonyMode
) {
    BoxWithConstraints(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp)
            .wrapContentSize()
            .aspectRatio(1f, matchHeightConstraintsFirst = true)

    ) {
        val updatedOnColorChanged by rememberUpdatedState(onColorChanged)
        val diameterPx = remember(constraints.maxWidth) {
            mutableStateOf(constraints.maxWidth)
        }

        var animateChanges by remember {
            mutableStateOf(false)
        }
        var currentlyChangingInput by remember {
            mutableStateOf(false)
        }

        var dragCounter = 0;
        fun updateColorWheel(newPosition: Offset, animate: Boolean) {
            // Work out if the new position is inside the circle we are drawing, and has a
            // valid color associated to it. If not, keep the current position
            val newColor = colorForPosition(
                newPosition,
                IntSize(diameterPx.value, diameterPx.value),
                hsvColor.value.value
            )
            if (newColor != null) {
                animateChanges = animate
                // Only send color update every 4th color change while dragging
                if (currentlyChangingInput) {
                    dragCounter++;
                    if (dragCounter % 4 == 0) {
                        updatedOnColorChanged(newColor)
                    }
                } else {
                    updatedOnColorChanged(newColor)
                }
            }
        }

        val inputModifier = Modifier.pointerInput(diameterPx) {
            forEachGesture {
                awaitPointerEventScope {
                    val down = awaitFirstDown(false)
                    currentlyChangingInput = true
                    updateColorWheel(down.position, animate = true)
                    drag(down.id) { change ->
                        updateColorWheel(change.position, animate = false)
                        change.consumePositionChange()
                    }
                    currentlyChangingInput = false
                    dragCounter = 0;
                }
            }
        }

        Box(inputModifier.fillMaxSize()) {
            ColorWheel(hsvColor = hsvColor.value, diameter = diameterPx.value)
            HarmonyColorMagnifiers(
                diameterPx.value,
                hsvColor.value,
                animateChanges,
                currentlyChangingInput,
                harmonyMode
            )
        }
    }
}

private fun colorForPosition(position: Offset, size: IntSize, value: Float): HsvColor? {
    val centerX: Double = size.width / 2.0
    val centerY: Double = size.height / 2.0
    val radius: Double = min(centerX, centerY)
    val xOffset: Double = position.x - centerX
    val yOffset: Double = position.y - centerY
    val centerOffset = hypot(xOffset, yOffset)
    val rawAngle = atan2(yOffset, xOffset).toDegree()
    val centerAngle = (rawAngle + 360.0) % 360.0
    return if (centerOffset <= radius) {
        HsvColor(
            hue = centerAngle.toFloat(),
            saturation = (centerOffset / radius).toFloat(),
            value = value,
            alpha = 1.0f
        )
    } else {
        HsvColor(
            hue = centerAngle.toFloat(),
            saturation = 1.0f,
            value = value,
            alpha = 1.0f
        )
    }
}
