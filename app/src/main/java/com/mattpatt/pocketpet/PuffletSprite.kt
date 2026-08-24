package com.mattpatt.pocketpet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import kotlin.math.sin

/**
 * Renders Pufflet from "Pufflet Animation Assets v2" - real hand-authored
 * frame sequences (256x256, uniform canvas) rather than a layered rig or a
 * skeleton. This is deliberately Gen5-Pokemon-style: a handful of frames
 * swapped on a timer/action, tail and all baked into each frame by the
 * artist, not computed.
 *
 * Only verified-clean frames from that pack are used here. It shipped with
 * a real defect - about half of "sleep" and most of "cleaning" have an
 * opaque gray rectangle baked into the pixels instead of transparency
 * (confirmed by sampling alpha values directly, not by eye). Those frames
 * are left out entirely rather than papering over them:
 *   - idle: all 6 frames clean -> used as the persistent base loop.
 *   - affection (Pet): frames 1-7 clean, frame 8 broken -> use 1-7.
 *   - sleep: only frames 2 and 6 clean -> used as a slow 2-frame
 *     breathing alternation instead of the full 6-frame loop.
 *   - cleaning: only frames 1 and 8 clean (2 of 8) - not enough to
 *     animate, so it's dropped entirely. cleaning_overlay (the
 *     bubbles/sparkles-only effect layer) is fully clean, and the pack's
 *     own manifest already recommends compositing it over the idle base
 *     rather than using the "cleaning" body frames - which is exactly
 *     what happens here for the Groom action.
 *
 * This pack has no Feed or Play frames, and no frames for moods other than
 * sleepy/affectionate (hungry, sad, sick, etc. all still show the idle
 * loop). Feed/Play keep working via the particle overlays already drawn
 * in AnimatedPet's Canvas block. Mood-specific expression for the other
 * states is a real gap - it would need matching frames in this same style.
 */
@Composable
fun PuffletSprite(
    visualState: VisualPuffletState,
    action: Action,
    actionProgress: Float,
    idlePhase: Float,
    blink: Float,
    modifier: Modifier = Modifier
) {
    val idleFrames = listOf(
        ImageBitmap.imageResource(R.drawable.sprite_idle_01),
        ImageBitmap.imageResource(R.drawable.sprite_idle_02),
        ImageBitmap.imageResource(R.drawable.sprite_idle_03),
        ImageBitmap.imageResource(R.drawable.sprite_idle_04),
        ImageBitmap.imageResource(R.drawable.sprite_idle_05),
        ImageBitmap.imageResource(R.drawable.sprite_idle_06)
    )
    val affectionFrames = listOf(
        ImageBitmap.imageResource(R.drawable.sprite_affection_01),
        ImageBitmap.imageResource(R.drawable.sprite_affection_02),
        ImageBitmap.imageResource(R.drawable.sprite_affection_03),
        ImageBitmap.imageResource(R.drawable.sprite_affection_04),
        ImageBitmap.imageResource(R.drawable.sprite_affection_05),
        ImageBitmap.imageResource(R.drawable.sprite_affection_06),
        ImageBitmap.imageResource(R.drawable.sprite_affection_07)
    )
    val sleepFrames = listOf(
        ImageBitmap.imageResource(R.drawable.sprite_sleep_a),
        ImageBitmap.imageResource(R.drawable.sprite_sleep_b)
    )
    val groomOverlayFrames = listOf(
        ImageBitmap.imageResource(R.drawable.sprite_groom_overlay_01),
        ImageBitmap.imageResource(R.drawable.sprite_groom_overlay_02),
        ImageBitmap.imageResource(R.drawable.sprite_groom_overlay_03),
        ImageBitmap.imageResource(R.drawable.sprite_groom_overlay_04),
        ImageBitmap.imageResource(R.drawable.sprite_groom_overlay_05),
        ImageBitmap.imageResource(R.drawable.sprite_groom_overlay_06),
        ImageBitmap.imageResource(R.drawable.sprite_groom_overlay_07),
        ImageBitmap.imageResource(R.drawable.sprite_groom_overlay_08)
    )

    val sleepy = visualState == VisualPuffletState.SLEEPY || action == Action.SLEEP

    val baseFrame = when {
        // One-shot: plays across the Pet action's own ~950ms actionProgress
        // window, same clock the rest of the app already uses for actions.
        action == Action.PET -> {
            val index = (actionProgress * (affectionFrames.size - 1))
                .toInt().coerceIn(0, affectionFrames.size - 1)
            affectionFrames[index]
        }
        // Slow alternation between the two clean sleep frames, timed off
        // the same shared idlePhase clock as everything else's idle motion.
        sleepy -> {
            val index = if (sin(idlePhase * Math.PI * 2.0) >= 0.0) 0 else 1
            sleepFrames[index]
        }
        else -> {
            val index = (idlePhase * idleFrames.size).toInt().coerceIn(0, idleFrames.size - 1)
            idleFrames[index]
        }
    }

    Box(modifier = modifier) {
        Image(
            bitmap = baseFrame,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        if (action == Action.GROOM) {
            val index = (actionProgress * (groomOverlayFrames.size - 1))
                .toInt().coerceIn(0, groomOverlayFrames.size - 1)
            Image(
                bitmap = groomOverlayFrames[index],
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}
