package com.philkes.baseled.ui.tab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.HsvColor
import com.philkes.baseled.R
import com.philkes.baseled.service.EspNowAction
import com.philkes.baseled.ui.theme.BaseLedTheme

data class Animation(val animationId: Int, val iconResource: Int);

val ANIMATIONS = listOf(
    Animation(EspNowAction.RGB_WHEEL.actionId, R.drawable.rgb_color_room),
    Animation(5, R.drawable.icon),
    Animation(6, R.drawable.icon),
    Animation(7, R.drawable.icon),
    Animation(8, R.drawable.icon),
    Animation(9, R.drawable.icon),
)

@Composable
fun AnimationTab() {
    val hsvColor = remember { mutableStateOf(HsvColor.from(Color.Red)) }


    val activeAnimation: MutableState<Animation?> = remember {
        mutableStateOf(ANIMATIONS[0])
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(start = 20.dp, end = 20.dp)

    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.25f))
/*        Box(
            modifier = Modifier
                .fillMaxWidth(1.0f)
                .fillMaxHeight(0.25f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Animations",
                fontSize = 46.sp,
                textAlign = TextAlign.Center,
            )
        }*/
        LazyVerticalGrid(
            modifier = Modifier,
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.Center
        ) {
            items(ANIMATIONS) { animation ->
                AnimationItem(animation, active = animation == activeAnimation.value) {
                    activeAnimation.value = animation
                }
            }
        }
        Spacer(modifier = Modifier.fillMaxHeight(0.1f))
/*        BrightnessBar(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            onValueChange = { value ->
                hsvColor.value = hsvColor.value.copy(value = value)
//                    updatedBrightness(hsvColor.value.TODO Only brightness)
            },
            currentColor = hsvColor.value
        )*/

    }
}

@Composable
fun AnimationItem(
    animation: Animation,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    onClickAction: () -> Unit
) {
    OutlinedButton(
        onClick = onClickAction,
        border = BorderStroke(
            if (!active) 2.dp else 6.dp,
            if (!active) MaterialTheme.colors.onSurface else MaterialTheme.colors.secondary
        ),
        shape = RoundedCornerShape(15),
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = if (!active) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.onSecondary
        ),
        modifier = modifier
            .padding(10.dp)
            .aspectRatio(1f)
    ) {
        Image(
            painter = painterResource(id = animation.iconResource),
            contentDescription = "icon",
            modifier = Modifier
                .fillMaxHeight(1f)
                .aspectRatio(1f)
        )
    }
}

@Preview
@Composable
fun AnimationTabPreview() {
    BaseLedTheme {
        AnimationTab()
    }
}