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
import com.godaddy.android.colorpicker.harmony.BrightnessBar
import com.philkes.baseled.R
import com.philkes.baseled.ui.theme.BaseLedTheme

data class Animation(val animationId: Int, val iconResource: Int);


@Composable
fun AnimationTab() {
    val hsvColor = remember { mutableStateOf(HsvColor.from(Color.Red)) }

    val animations = listOf(
        Animation(0, R.drawable.rgb_color_room),
        Animation(1, R.drawable.icon),
        Animation(2, R.drawable.icon),
        Animation(3, R.drawable.icon),
        Animation(4, R.drawable.icon),
        Animation(5, R.drawable.icon),
    )
    val activeAnimation: MutableState<Animation?> = remember {
        mutableStateOf(animations[0])
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
            items(animations) { animation ->
                AnimationItem(animation, animation == activeAnimation.value) {
                    activeAnimation.value = animation
                }
            }
        }
        Spacer(modifier = Modifier.fillMaxHeight(0.1f))
        BrightnessBar(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            onValueChange = { value ->
                hsvColor.value = hsvColor.value.copy(value = value)
//                    updatedBrightness(hsvColor.value.TODO Only brightness)
            },
            currentColor = hsvColor.value
        )

    }
}

@Composable
fun AnimationItem(animation: Animation, active: Boolean = false, onClickAction: () -> Unit) {
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
        modifier = Modifier
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