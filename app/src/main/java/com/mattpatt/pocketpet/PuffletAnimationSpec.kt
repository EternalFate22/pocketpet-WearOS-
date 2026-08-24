package com.mattpatt.pocketpet

import kotlin.math.PI
import kotlin.math.sin

enum class PuffletAnimation {
    IDLE_BREATH, BLINK, HAPPY, SAD, SLEEP, FEED, PLAY, PET, GROOM
}

data class PuffletFrame(val name: String, val durationMs: Int)

data class PuffletAnimationPlan(
    val frames: List<PuffletFrame>,
    val loop: Boolean = false
)

object PuffletAnimationPlans {
    val IDLE = PuffletAnimationPlan(
        listOf("idle_00", "idle_01", "idle_02", "idle_03", "idle_02", "idle_01")
            .map { PuffletFrame(it, 180) },
        loop = true
    )
    val BLINK = PuffletAnimationPlan(
        listOf(PuffletFrame("eyes_open", 70), PuffletFrame("blink_01", 55),
            PuffletFrame("blink_02", 70), PuffletFrame("eyes_open", 110))
    )
    val HAPPY = PuffletAnimationPlan(
        listOf("happy_00", "happy_01", "happy_02", "happy_03", "happy_02", "happy_01")
            .map { PuffletFrame(it, 110) }
    )
    val SAD = PuffletAnimationPlan(
        listOf("sad_00", "sad_01", "sad_02", "sad_03").map { PuffletFrame(it, 190) },
        loop = true
    )
    val SLEEP = PuffletAnimationPlan(
        listOf("sleep_00", "sleep_01", "sleep_02", "sleep_03", "sleep_02", "sleep_01")
            .map { PuffletFrame(it, 300) },
        loop = true
    )
    val FEED = PuffletAnimationPlan(
        listOf("feed_00", "feed_01", "feed_02", "feed_03", "feed_04", "feed_05")
            .map { PuffletFrame(it, 120) }
    )
    val PLAY = PuffletAnimationPlan(
        listOf("play_00", "play_01", "play_02", "play_03", "play_04", "play_05", "play_06", "play_07")
            .map { PuffletFrame(it, 110) }
    )
    val PET = PuffletAnimationPlan(
        listOf("pet_00", "pet_01", "pet_02", "pet_03", "pet_04", "pet_05")
            .map { PuffletFrame(it, 120) }
    )
    val GROOM = PuffletAnimationPlan(
        listOf("groom_00", "groom_01", "groom_02", "groom_03", "groom_04", "groom_05")
            .map { PuffletFrame(it, 120) }
    )
}

data class PuffletMotion(
    val scaleX: Float,
    val scaleY: Float,
    val translationX: Float,
    val translationY: Float,
    val rotationDegrees: Float
)

fun puffletIdleMotion(phase: Float, amplitude: Float = 1f): PuffletMotion {
    val t = phase * 2f * PI.toFloat()
    val breathe = sin(t) * amplitude
    val delayedHead = sin(t - 0.35f) * amplitude
    val tailWave = sin(t + 0.75f) * amplitude
    return PuffletMotion(
        scaleX = 1f - breathe * 0.012f,
        scaleY = 1f + breathe * 0.018f,
        translationX = tailWave * 0.35f,
        translationY = -breathe * 1.8f,
        rotationDegrees = delayedHead * 0.35f
    )
}

fun puffletAnimationFor(mood: Mood, action: Action, isBlinking: Boolean): PuffletAnimation = when {
    action == Action.SLEEP -> PuffletAnimation.SLEEP
    action == Action.FEED -> PuffletAnimation.FEED
    action == Action.PLAY -> PuffletAnimation.PLAY
    action == Action.PET -> PuffletAnimation.PET
    action == Action.GROOM -> PuffletAnimation.GROOM
    isBlinking -> PuffletAnimation.BLINK
    mood == Mood.LOVE || mood == Mood.EXCITED || mood == Mood.HAPPY -> PuffletAnimation.HAPPY
    mood == Mood.SAD || mood == Mood.HUNGRY || mood == Mood.WEAK -> PuffletAnimation.SAD
    mood == Mood.SLEEPY -> PuffletAnimation.SLEEP
    else -> PuffletAnimation.IDLE_BREATH
}