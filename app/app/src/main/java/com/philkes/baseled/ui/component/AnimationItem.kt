package com.philkes.baseled.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.philkes.baseled.R
import com.philkes.baseled.service.EspNowAction

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
