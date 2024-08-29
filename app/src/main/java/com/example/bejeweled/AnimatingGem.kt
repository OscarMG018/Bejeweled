package com.example.bejeweled

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.*
import com.example.bejeweled.Game.*
import kotlinx.coroutines.*
import androidx.compose.ui.unit.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.scale

data class AnimatingGem(
    val position: Position,
    val color: JewelColor,
    var alpha: Float = 1f
)

@Composable
fun AnimatedGem(
    position: Position,
    color: JewelColor?,
    animationState: AnimationState,
    pos1: Position,
    pos2: Position,
    breakingGems: List<Position>,
    onClick: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(animationState, pos1, pos2, breakingGems, color) {
        when (animationState) {
            AnimationState.SWAPPING -> {
                if (position == pos1 || position == pos2) {
                    val targetPosition = if (position == pos1) pos2 else pos1
                    launch {
                        offsetX.animateTo(
                            (targetPosition.y - position.y) * 40f,
                            animationSpec = tween(500, easing = LinearEasing)
                        )
                        offsetX.snapTo(0f)
                    }
                    launch {
                        offsetY.animateTo(
                            (targetPosition.x - position.x) * 40f,
                            animationSpec = tween(500, easing = LinearEasing)
                        )
                        offsetY.snapTo(0f)
                    }
                }
            }
            AnimationState.BREAKING -> {
                offsetX.snapTo(0f)
                offsetY.snapTo(0f)
                if (breakingGems.contains(position)) {
                    launch {
                        scale.animateTo(1.2f, animationSpec = tween(250))
                        scale.animateTo(0f, animationSpec = tween(250))
                    }
                    launch {
                        alpha.animateTo(0f, animationSpec = tween(500))
                    }
                } else if (color != null) {
                    // This is a new gem, animate it coming in
                    scale.snapTo(1f)
                    alpha.snapTo(1f)
                }
            }
            AnimationState.IDLE -> {
                offsetX.snapTo(0f)
                offsetY.snapTo(0f)
                scale.snapTo(1f)
                alpha.snapTo(1f)
            }
        }
    }

    val borderColor =  if ((position == pos1 || position == pos2) && animationState == AnimationState.IDLE) Color.White else Color.Transparent
    val borderWidth =  if ((position == pos1 || position == pos2) && animationState == AnimationState.IDLE) 4 else 0


    Box(
        modifier = Modifier
            .border(borderWidth.dp,borderColor)
            .offset(offsetX.value.dp, offsetY.value.dp)
            .scale(scale.value)
            .alpha(alpha.value)
            .size(40.dp)
            .background(brush = Brush.linearGradient(colors = getColorsForDisplay(color)))
            .clickable(onClick = onClick)
    )
}