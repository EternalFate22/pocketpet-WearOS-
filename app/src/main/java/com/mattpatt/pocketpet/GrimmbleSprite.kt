package com.mattpatt.pocketpet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource

/**
 * Renders Grimmble from the "Grimmble PocketPet Asset Pack v1" frame set -
 * same Gen5-style approach as PuffletSprite: real hand-authored frames on a
 * uniform 256x256 canvas, swapped by action/idle clock, no rig math.
 *
 * Unlike the Pufflet v2 pack, every frame here checked out clean (sampled
 * alpha on all 26 frames - 0% opaque-gray-background contamination) so
 * nothing had to be dropped. The pack also ships this pet's own blink
 * pair as a separate 2-frame set rather than baking blink into idle, so
 * blink is driven off the shared `blink` clock (the same signal the old
 * layered Pufflet renderer used to use) instead of idlePhase.
 *
 * This pack has no sleep or play frames. Sleepy mood and the Play action
 * fall back to the idle loop; Play still gets its ball-bounce particle
 * overlay from AnimatedPet's Canvas block, same as Pufflet.
 */
@Composable
fun GrimmbleSprite(
    action: Action,
    actionProgress: Float,
    idlePhase: Float,
    blink: Float,
    modifier: Modifier = Modifier
) {
    val idleFrames = listOf(
        ImageBitmap.imageResource(R.drawable.grimmble_idle_00),
        ImageBitmap.imageResource(R.drawable.grimmble_idle_01),
        ImageBitmap.imageResource(R.drawable.grimmble_idle_02),
        ImageBitmap.imageResource(R.drawable.grimmble_idle_03),
        ImageBitmap.imageResource(R.drawable.grimmble_idle_04),
        ImageBitmap.imageResource(R.drawable.grimmble_idle_05)
    )
    val blinkFrames = listOf(
        ImageBitmap.imageResource(R.drawable.grimmble_blink_00),
        ImageBitmap.imageResource(R.drawable.grimmble_blink_01)
    )
    val feedFrames = listOf(
        ImageBitmap.imageResource(R.drawable.grimmble_feed_00),
        ImageBitmap.imageResource(R.drawable.grimmble_feed_01),
        ImageBitmap.imageResource(R.drawable.grimmble_feed_02),
        ImageBitmap.imageResource(R.drawable.grimmble_feed_03),
        ImageBitmap.imageResource(R.drawable.grimmble_feed_04),
        ImageBitmap.imageResource(R.drawable.grimmble_feed_05)
    )
    val petFrames = listOf(
        ImageBitmap.imageResource(R.drawable.grimmble_pet_00),
        ImageBitmap.imageResource(R.drawable.grimmble_pet_01),
        ImageBitmap.imageResource(R.drawable.grimmble_pet_02),
        ImageBitmap.imageResource(R.drawable.grimmble_pet_03),
        ImageBitmap.imageResource(R.drawable.grimmble_pet_04),
        ImageBitmap.imageResource(R.drawable.grimmble_pet_05)
    )
    val groomFrames = listOf(
        ImageBitmap.imageResource(R.drawable.grimmble_groom_00),
        ImageBitmap.imageResource(R.drawable.grimmble_groom_01),
        ImageBitmap.imageResource(R.drawable.grimmble_groom_02),
        ImageBitmap.imageResource(R.drawable.grimmble_groom_03),
        ImageBitmap.imageResource(R.drawable.grimmble_groom_04),
        ImageBitmap.imageResource(R.drawable.grimmble_groom_05)
    )

    val frame = when {
        action == Action.FEED -> {
            val i = (actionProgress * (feedFrames.size - 1)).toInt().coerceIn(0, feedFrames.size - 1)
            feedFrames[i]
        }
        action == Action.PET -> {
            val i = (actionProgress * (petFrames.size - 1)).toInt().coerceIn(0, petFrames.size - 1)
            petFrames[i]
        }
        action == Action.GROOM -> {
            val i = (actionProgress * (groomFrames.size - 1)).toInt().coerceIn(0, groomFrames.size - 1)
            groomFrames[i]
        }
        blink > 0.75f -> blinkFrames[1]
        blink > 0.15f -> blinkFrames[0]
        else -> {
            val i = (idlePhase * idleFrames.size).toInt().coerceIn(0, idleFrames.size - 1)
            idleFrames[i]
        }
    }

    Image(
        bitmap = frame,
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}
