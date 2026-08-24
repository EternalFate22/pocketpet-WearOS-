package com.mattpatt.pocketpet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
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
        R.drawable.pufflet_idle_01, R.drawable.pufflet_idle_02,
        R.drawable.pufflet_idle_03, R.drawable.pufflet_idle_04,
        R.drawable.pufflet_idle_05, R.drawable.pufflet_idle_06,
        R.drawable.pufflet_idle_07, R.drawable.pufflet_idle_08,
        R.drawable.pufflet_idle_09
    ).map { resourceId -> ImageBitmap.imageResource(resourceId) }
    val walkFrames = listOf(
        R.drawable.pufflet_walk_01, R.drawable.pufflet_walk_02,
        R.drawable.pufflet_walk_03, R.drawable.pufflet_walk_04,
        R.drawable.pufflet_walk_05, R.drawable.pufflet_walk_06,
        R.drawable.pufflet_walk_07
    ).map { resourceId -> ImageBitmap.imageResource(resourceId) }
    val eatFrames = listOf(
        R.drawable.pufflet_eat_01, R.drawable.pufflet_eat_02,
        R.drawable.pufflet_eat_03, R.drawable.pufflet_eat_04,
        R.drawable.pufflet_eat_05, R.drawable.pufflet_eat_06,
        R.drawable.pufflet_eat_07, R.drawable.pufflet_eat_08,
        R.drawable.pufflet_eat_09
    ).map { resourceId -> ImageBitmap.imageResource(resourceId) }
    val sleepFrames = listOf(
        R.drawable.pufflet_sleep_01, R.drawable.pufflet_sleep_02,
        R.drawable.pufflet_sleep_03, R.drawable.pufflet_sleep_04,
        R.drawable.pufflet_sleep_05, R.drawable.pufflet_sleep_06,
        R.drawable.pufflet_sleep_07
    ).map { resourceId -> ImageBitmap.imageResource(resourceId) }
    val playFrames = listOf(
        R.drawable.pufflet_play_01, R.drawable.pufflet_play_02,
        R.drawable.pufflet_play_03, R.drawable.pufflet_play_04,
        R.drawable.pufflet_play_05, R.drawable.pufflet_play_06,
        R.drawable.pufflet_play_07, R.drawable.pufflet_play_08
    ).map { resourceId -> ImageBitmap.imageResource(resourceId) }
    val sitFrames = listOf(
        R.drawable.pufflet_sit_01, R.drawable.pufflet_sit_02,
        R.drawable.pufflet_sit_03, R.drawable.pufflet_sit_04,
        R.drawable.pufflet_sit_05, R.drawable.pufflet_sit_06,
        R.drawable.pufflet_sit_07, R.drawable.pufflet_sit_08,
        R.drawable.pufflet_sit_09
    ).map { resourceId -> ImageBitmap.imageResource(resourceId) }
    val hitFrames = listOf(
        R.drawable.pufflet_hit_01, R.drawable.pufflet_hit_02,
        R.drawable.pufflet_hit_03, R.drawable.pufflet_hit_04,
        R.drawable.pufflet_hit_05, R.drawable.pufflet_hit_06,
        R.drawable.pufflet_hit_07, R.drawable.pufflet_hit_08
    ).map { resourceId -> ImageBitmap.imageResource(resourceId) }
    val happyFrames = listOf(
        R.drawable.pufflet_happy_01, R.drawable.pufflet_happy_02,
        R.drawable.pufflet_happy_03, R.drawable.pufflet_happy_04,
        R.drawable.pufflet_happy_05, R.drawable.pufflet_happy_06,
        R.drawable.pufflet_happy_07, R.drawable.pufflet_happy_08
    ).map { resourceId -> ImageBitmap.imageResource(resourceId) }
    val expressionFrames = listOf(
        R.drawable.pufflet_expressions_01, R.drawable.pufflet_expressions_02,
        R.drawable.pufflet_expressions_03, R.drawable.pufflet_expressions_04,
        R.drawable.pufflet_expressions_05, R.drawable.pufflet_expressions_06,
        R.drawable.pufflet_expressions_07, R.drawable.pufflet_expressions_08,
        R.drawable.pufflet_expressions_09
    ).map { resourceId -> ImageBitmap.imageResource(resourceId) }

    val frameSet = when {
        action == Action.FEED -> eatFrames
        action == Action.PLAY -> playFrames
        action == Action.PET -> happyFrames
        action == Action.GROOM -> sitFrames
        action == Action.SLEEP || visualState == VisualPuffletState.SLEEPY -> sleepFrames
        visualState == VisualPuffletState.HAPPY || visualState == VisualPuffletState.AFFECTIONATE -> happyFrames
        visualState == VisualPuffletState.EXCITED -> walkFrames
        visualState == VisualPuffletState.SICK || visualState == VisualPuffletState.WEAK ||
            visualState == VisualPuffletState.CRITICAL -> hitFrames
        visualState == VisualPuffletState.SAD -> sitFrames
        visualState == VisualPuffletState.HUNGRY || visualState == VisualPuffletState.DIRTY ||
            visualState == VisualPuffletState.ANGRY -> expressionFrames
        else -> idleFrames
    }

    val baseFrame = if (action == Action.IDLE) {
        frameSet[(idlePhase * frameSet.size).toInt() % frameSet.size]
    } else {
        frameSet[(actionProgress * (frameSet.size - 1)).toInt().coerceIn(0, frameSet.size - 1)]
    }

    Box(modifier = modifier) {
        Image(
            bitmap = baseFrame,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
