package com.mattpatt.pocketpet

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

data class PuffletAnimationState(
    val breathing: Float,
    val blink: Float,
    val earTwitch: Float,
    val tailWag: Float,
    val lean: Float,
    val bounce: Float,
    val shake: Float
)

fun puffletAnimationState(
    visualState: VisualPuffletState,
    action: Action,
    progress: Float,
    idlePhase: Float,
    blink: Float
): PuffletAnimationState {
    val subdued = visualState == VisualPuffletState.SAD ||
        visualState == VisualPuffletState.SICK || visualState == VisualPuffletState.WEAK
    val breathing = sin(idlePhase * Math.PI * 2.0).toFloat() * if (subdued) 0.7f else 1.4f
    val tailWag = when (visualState) {
        VisualPuffletState.EXCITED -> sin(idlePhase * Math.PI * 8.0).toFloat() * 4.5f
        VisualPuffletState.HAPPY, VisualPuffletState.AFFECTIONATE -> sin(idlePhase * Math.PI * 4.0).toFloat() * 2.5f
        else -> sin(idlePhase * Math.PI * 1.5).toFloat() * 0.8f
    }
    return PuffletAnimationState(
        breathing = breathing,
        blink = if (visualState == VisualPuffletState.SLEEPY) 1f else blink,
        earTwitch = when (action) {
            Action.PET -> sin(progress * Math.PI * 3.0).toFloat() * 3f
            else -> sin(idlePhase * Math.PI * 2.0).toFloat() * 1.2f
        },
        tailWag = tailWag,
        lean = when (action) {
            Action.PET -> sin(progress * Math.PI).toFloat() * -3f
            Action.FEED -> sin(progress * Math.PI * 2.0).toFloat() * 2f
            else -> 0f
        },
        bounce = when (action) {
            Action.PLAY -> sin(progress * Math.PI).toFloat() * -10f
            Action.PET -> sin(progress * Math.PI).toFloat() * -3f
            else -> 0f
        },
        shake = if (action == Action.GROOM) sin(progress * Math.PI * 8.0).toFloat() * 2f else 0f
    )
}

/** Adds expressive feature layers to the intact concept artwork. */
fun drawPuffletFeatureLayers(
    canvas: DrawScope,
    cx: Float,
    cy: Float,
    visualState: VisualPuffletState,
    animation: PuffletAnimationState
) {
    val eyeWhite = Color.White.copy(alpha = 0.98f)
    val lid = Color(0xFFB88DDB)
    val eyeY = cy - 17f
    val eyeOffset = 14f
    val eyeRadius = 14.5f
    val blinkAmount = animation.blink.coerceIn(0f, 1f)

    if (blinkAmount > 0f) {
        listOf(cx - eyeOffset, cx + eyeOffset).forEach { eyeX ->
            canvas.drawOval(
                color = eyeWhite,
                topLeft = Offset(eyeX - eyeRadius, eyeY - eyeRadius),
                size = Size(eyeRadius * 2f, eyeRadius * 2f)
            )
            val openness = 1f - blinkAmount
            canvas.drawArc(
                color = lid,
                startAngle = 180f + openness * 18f,
                sweepAngle = 180f - openness * 36f,
                useCenter = false,
                topLeft = Offset(eyeX - eyeRadius, eyeY - eyeRadius * (0.55f + openness * 0.45f)),
                size = Size(eyeRadius * 2f, eyeRadius * (1.1f + openness * 0.9f)),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
        }
    }

    if (visualState == VisualPuffletState.AFFECTIONATE || visualState == VisualPuffletState.HAPPY) {
        canvas.drawCircle(Color(0xFFFF86B5).copy(alpha = 0.22f), 7f, Offset(cx - 27f, cy + 10f))
        canvas.drawCircle(Color(0xFFFF86B5).copy(alpha = 0.22f), 7f, Offset(cx + 27f, cy + 10f))
    }
}

/** Dispatches to a species-specific body drawer. Add a new `when` branch per new pet. */
fun drawPetBody(
    canvas: DrawScope,
    cx: Float,
    cy: Float,
    species: PetSpecies,
    visualState: VisualPuffletState,
    action: Action,
    progress: Float,
    fullness: Int,
    blink: Boolean,
    health: Int,
    cleanliness: Int,
    energy: Int
) {
    when (species.id) {
        else -> drawPufflet(canvas, cx, cy, species, visualState, action, progress, fullness, blink, health, cleanliness, energy)
    }
}

private fun drawPufflet(
    canvas: DrawScope,
    cx: Float,
    cy: Float,
    species: PetSpecies,
    visualState: VisualPuffletState,
    action: Action,
    progress: Float,
    fullness: Int,
    blink: Boolean,
    health: Int,
    cleanliness: Int,
    energy: Int
) {
    val fur = species.fur
    val furShadow = species.furShadow
    val earInner = species.earInner
    val eye = species.eyeColor
    val pink = species.accent

    // Tail behind the body.
    val tailMotion = when (visualState) {
        VisualPuffletState.EXCITED -> sin(progress * Math.PI * 4.0).toFloat() * 8f
        VisualPuffletState.HAPPY, VisualPuffletState.AFFECTIONATE -> sin(progress * Math.PI * 2.0).toFloat() * 5f
        VisualPuffletState.ANGRY -> sin(progress * Math.PI * 3.0).toFloat() * 6f
        else -> 0f
    }
    canvas.drawArc(
        color = furShadow,
        startAngle = -70f + tailMotion,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cx + 45f + tailMotion, cy - 18f),
        size = Size(70f, 95f),
        style = Stroke(width = 18f, cap = StrokeCap.Round)
    )

    // Ears (twitch slightly during PET / EXCITED moments for personality).
    val earDrop = when (visualState) {
        VisualPuffletState.SAD, VisualPuffletState.SLEEPY,
        VisualPuffletState.DIRTY, VisualPuffletState.SICK,
        VisualPuffletState.WEAK -> 8f
        VisualPuffletState.ANGRY -> -5f
        else -> 0f
    }
    val earTwitch = if (action == Action.PET || visualState == VisualPuffletState.EXCITED) {
        sin(progress * Math.PI * 3.0).toFloat() * 4f
    } else 0f
    val leftEar = Path().apply {
        moveTo(cx - 52f, cy - 52f)
        lineTo(cx - 65f + earTwitch, cy - 103f + earDrop)
        lineTo(cx - 20f, cy - 75f)
        close()
    }
    val rightEar = Path().apply {
        moveTo(cx + 52f, cy - 52f)
        lineTo(cx + 65f - earTwitch, cy - 103f + earDrop)
        lineTo(cx + 20f, cy - 75f)
        close()
    }
    canvas.drawPath(leftEar, fur)
    canvas.drawPath(rightEar, fur)

    val leftInner = Path().apply {
        moveTo(cx - 53f, cy - 65f)
        lineTo(cx - 59f + earTwitch, cy - 88f + earDrop)
        lineTo(cx - 35f, cy - 75f)
        close()
    }
    val rightInner = Path().apply {
        moveTo(cx + 53f, cy - 65f)
        lineTo(cx + 59f - earTwitch, cy - 88f + earDrop)
        lineTo(cx + 35f, cy - 75f)
        close()
    }
    canvas.drawPath(leftInner, earInner)
    canvas.drawPath(rightInner, earInner)

    // Body.
    canvas.drawCircle(color = fur, radius = 70f, center = Offset(cx, cy + 10f))
    canvas.drawCircle(color = fur, radius = 55f, center = Offset(cx, cy - 33f))

    // Fluffy cheeks.
    canvas.drawCircle(color = furShadow, radius = 17f, center = Offset(cx - 48f, cy + 8f))
    canvas.drawCircle(color = furShadow, radius = 17f, center = Offset(cx + 48f, cy + 8f))

    // Dirt smudges when grubby.
    if (cleanliness <= 30 || visualState == VisualPuffletState.DIRTY) {
        canvas.drawCircle(color = Color(0xFFA9895E).copy(alpha = 0.55f), radius = 6f, center = Offset(cx - 30f, cy + 28f))
        canvas.drawCircle(color = Color(0xFFA9895E).copy(alpha = 0.5f), radius = 5f, center = Offset(cx + 34f, cy + 40f))
    }

    // Eyes / expressions. `blink` briefly overrides everything except sleepy.
    if (blink && visualState != VisualPuffletState.SLEEPY) {
        listOf(cx - 24f, cx + 24f).forEach { eyeX ->
            canvas.drawCircle(color = Color.White, radius = 11f, center = Offset(eyeX, cy - 34f))
            canvas.drawArc(
                color = eye,
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(eyeX - 11f, cy - 42f),
                size = Size(22f, 16f),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
        }
    } else when (visualState) {
        VisualPuffletState.SLEEPY -> {
            canvas.drawArc(
                color = eye, startAngle = 15f, sweepAngle = 150f, useCenter = false,
                topLeft = Offset(cx - 34f, cy - 43f), size = Size(20f, 14f),
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
            canvas.drawArc(
                color = eye, startAngle = 15f, sweepAngle = 150f, useCenter = false,
                topLeft = Offset(cx + 14f, cy - 43f), size = Size(20f, 14f),
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
        }
        VisualPuffletState.SAD, VisualPuffletState.DIRTY, VisualPuffletState.SICK, VisualPuffletState.WEAK -> {
            canvas.drawCircle(color = eye, radius = 11f, center = Offset(cx - 24f, cy - 33f))
            canvas.drawCircle(color = eye, radius = 11f, center = Offset(cx + 24f, cy - 33f))
            canvas.drawCircle(color = Color.White, radius = 3.5f, center = Offset(cx - 20f, cy - 37f))
            canvas.drawCircle(color = Color.White, radius = 3.5f, center = Offset(cx + 28f, cy - 37f))
        }
        else -> {
            canvas.drawCircle(color = eye, radius = 12f, center = Offset(cx - 24f, cy - 34f))
            canvas.drawCircle(color = eye, radius = 12f, center = Offset(cx + 24f, cy - 34f))
            canvas.drawCircle(color = Color.White, radius = 4f, center = Offset(cx - 20f, cy - 39f))
            canvas.drawCircle(color = Color.White, radius = 4f, center = Offset(cx + 28f, cy - 39f))
        }
    }

    // Nose.
    canvas.drawCircle(color = pink, radius = 5f, center = Offset(cx, cy - 5f))

    // Mouth.
    when (visualState) {
        VisualPuffletState.HAPPY, VisualPuffletState.EXCITED, VisualPuffletState.AFFECTIONATE -> {
            canvas.drawArc(
                color = eye,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - 22f, cy + 2f),
                size = Size(44f, 24f),
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
        }
        VisualPuffletState.SAD -> {
            canvas.drawArc(
                color = eye,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - 18f, cy + 7f),
                size = Size(36f, 20f),
                style = Stroke(width = 4.5f, cap = StrokeCap.Round)
            )
        }
        VisualPuffletState.HUNGRY -> {
            canvas.drawCircle(color = eye, radius = 8f, center = Offset(cx, cy + 9f))
        }
        VisualPuffletState.DIRTY -> {
            canvas.drawLine(color = eye, start = Offset(cx - 8f, cy + 9f), end = Offset(cx + 8f, cy + 6f), strokeWidth = 4f, cap = StrokeCap.Round)
        }
        VisualPuffletState.ANGRY -> {
            canvas.drawLine(color = eye, start = Offset(cx - 12f, cy + 8f), end = Offset(cx + 12f, cy + 8f), strokeWidth = 5f, cap = StrokeCap.Round)
        }
        VisualPuffletState.SICK, VisualPuffletState.WEAK, VisualPuffletState.CRITICAL -> {
            canvas.drawLine(color = eye, start = Offset(cx - 8f, cy + 8f), end = Offset(cx + 8f, cy + 8f), strokeWidth = 4f, cap = StrokeCap.Round)
        }
        VisualPuffletState.SLEEPY, VisualPuffletState.NORMAL -> {
            canvas.drawLine(color = eye, start = Offset(cx - 9f, cy + 8f), end = Offset(cx + 9f, cy + 8f), strokeWidth = 4f, cap = StrokeCap.Round)
        }
    }

    if (visualState == VisualPuffletState.ANGRY) {
        canvas.drawLine(color = eye, start = Offset(cx - 38f, cy - 50f), end = Offset(cx - 10f, cy - 42f), strokeWidth = 6f, cap = StrokeCap.Round)
        canvas.drawLine(color = eye, start = Offset(cx + 10f, cy - 42f), end = Offset(cx + 38f, cy - 50f), strokeWidth = 6f, cap = StrokeCap.Round)
    }

    if (visualState == VisualPuffletState.SICK) {
        canvas.drawCircle(color = Color(0xFF8DE5FF).copy(alpha = 0.55f), radius = 5f, center = Offset(cx + 48f, cy - 4f))
        canvas.drawCircle(color = Color(0xFF8DE5FF).copy(alpha = 0.35f), radius = 3f, center = Offset(cx + 57f, cy - 15f))
    }

    if (visualState == VisualPuffletState.CRITICAL) {
        canvas.drawCircle(color = Color(0xFFFFA726), radius = 12f, center = Offset(cx + 68f, cy - 48f))
        canvas.drawLine(color = Color.White, start = Offset(cx + 68f, cy - 55f), end = Offset(cx + 68f, cy - 44f), strokeWidth = 4f, cap = StrokeCap.Round)
        canvas.drawCircle(color = Color.White, radius = 2.5f, center = Offset(cx + 68f, cy - 39f))
    }

    if (visualState == VisualPuffletState.WEAK || energy <= 12) {
        canvas.drawLine(color = Color.White.copy(alpha = 0.45f), start = Offset(cx - 76f, cy + 78f), end = Offset(cx - 50f, cy + 78f), strokeWidth = 3f, cap = StrokeCap.Round)
        canvas.drawLine(color = Color.White.copy(alpha = 0.3f), start = Offset(cx + 50f, cy + 78f), end = Offset(cx + 76f, cy + 78f), strokeWidth = 3f, cap = StrokeCap.Round)
    }

    // Tiny paws.
    canvas.drawCircle(color = furShadow, radius = 15f, center = Offset(cx - 42f, cy + 58f))
    canvas.drawCircle(color = furShadow, radius = 15f, center = Offset(cx + 42f, cy + 58f))

    if (action == Action.FEED) {
        val wiggle = sin(progress * Math.PI * 4.0).toFloat() * 3f
        canvas.drawCircle(color = furShadow, radius = 6f, center = Offset(cx, cy + 70f + wiggle))
    }

    if (fullness < 15) {
        canvas.drawCircle(color = Color(0xFFFFC857), radius = 4f, center = Offset(cx + 72f, cy + 30f))
    }
}

fun drawHeart(canvas: DrawScope, center: Offset, scale: Float, color: Color) {
    val p = Path()
    p.moveTo(center.x, center.y + 18f * scale)
    p.cubicTo(
        center.x - 35f * scale, center.y - 5f * scale,
        center.x - 17f * scale, center.y - 25f * scale,
        center.x, center.y - 8f * scale
    )
    p.cubicTo(
        center.x + 17f * scale, center.y - 25f * scale,
        center.x + 35f * scale, center.y - 5f * scale,
        center.x, center.y + 18f * scale
    )
    p.close()
    canvas.drawPath(p, color)
}

fun drawBubble(canvas: DrawScope, center: Offset, radius: Float, alpha: Float) {
    canvas.drawCircle(color = Color(0xFFBFEFFF).copy(alpha = alpha), radius = radius, center = center)
    canvas.drawCircle(
        color = Color.White.copy(alpha = alpha * 0.9f),
        radius = radius * 0.35f,
        center = center + Offset(-radius * 0.3f, -radius * 0.3f)
    )
}

/** Simple tombstone shown on the game-over screen. */
fun drawTombstone(canvas: DrawScope, cx: Float, cy: Float) {
    val stone = Color(0xFFB9C3D0)
    val stoneShadow = Color(0xFF8C97A6)
    val path = Path().apply {
        moveTo(cx - 42f, cy + 55f)
        lineTo(cx - 42f, cy - 10f)
        cubicTo(cx - 42f, cy - 55f, cx + 42f, cy - 55f, cx + 42f, cy - 10f)
        lineTo(cx + 42f, cy + 55f)
        close()
    }
    canvas.drawPath(path, stone)
    canvas.drawRect(
        color = stoneShadow,
        topLeft = Offset(cx - 42f, cy + 40f),
        size = Size(84f, 15f)
    )
    canvas.drawLine(color = Color(0xFF5B6472), start = Offset(cx, cy - 15f), end = Offset(cx, cy + 15f), strokeWidth = 6f, cap = StrokeCap.Round)
    canvas.drawLine(color = Color(0xFF5B6472), start = Offset(cx - 15f, cy), end = Offset(cx + 15f, cy), strokeWidth = 6f, cap = StrokeCap.Round)
}
