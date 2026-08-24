package com.mattpatt.pocketpet

import androidx.compose.ui.graphics.Color

/**
 * Moods drive both the face/animation the pet shows and the flavor text
 * it "says". Priority order for picking a mood lives in MainActivity.
 */
enum class Mood {
    HAPPY, NORMAL, HUNGRY, SAD, SLEEPY, DIRTY, EXCITED, LOVE,
    ANGRY, SICK, WEAK, CRITICAL
}

enum class VisualPuffletState {
    HAPPY, NORMAL, HUNGRY, SAD, SLEEPY, DIRTY, EXCITED, AFFECTIONATE,
    ANGRY, SICK, WEAK, CRITICAL
}

fun Mood.toVisualPuffletState(): VisualPuffletState = when (this) {
    Mood.HAPPY -> VisualPuffletState.HAPPY
    Mood.NORMAL -> VisualPuffletState.NORMAL
    Mood.HUNGRY -> VisualPuffletState.HUNGRY
    Mood.SAD -> VisualPuffletState.SAD
    Mood.SLEEPY -> VisualPuffletState.SLEEPY
    Mood.DIRTY -> VisualPuffletState.DIRTY
    Mood.EXCITED -> VisualPuffletState.EXCITED
    Mood.LOVE -> VisualPuffletState.AFFECTIONATE
    Mood.ANGRY -> VisualPuffletState.ANGRY
    Mood.SICK -> VisualPuffletState.SICK
    Mood.WEAK -> VisualPuffletState.WEAK
    Mood.CRITICAL -> VisualPuffletState.CRITICAL
}

/** A currently-playing action animation. IDLE means nothing special is happening. */
enum class Action {
    IDLE, FEED, PLAY, PET, GROOM, SLEEP
}

enum class Personality {
    PLAYFUL, CUDDLY, LAZY, MISCHIEVOUS
}

data class PetStats(
    val health: Int = 90,
    val happiness: Int = 80,
    val energy: Int = 85,
    val cleanliness: Int = 90,
    val fullness: Int = 75,
    val affection: Int = 50,
    val level: Int = 1,
    val xp: Int = 0,
    val personality: Personality = Personality.PLAYFUL
)

/**
 * Everything that makes one kind of pet look/feel different from another.
 * To add a new pet later: create another PetSpecies val (see PUFFLET below)
 * with its own palette + flavor lines, register it in PetCatalog.ALL, and
 * give it a body-shape branch in PetRenderer.drawPetBody(). Nothing else
 * in the app (stats, saving, decay, death timer, UI chrome) needs to change.
 */
data class PetSpecies(
    val id: String,
    val displayName: String,
    val fur: Color,
    val furShadow: Color,
    val earInner: Color,
    val eyeColor: Color,
    val accent: Color,
    val flavorLines: Map<Mood, List<String>>
) {
    fun lineFor(mood: Mood): List<String> = flavorLines[mood] ?: listOf("...")

    fun lineFor(mood: Mood, personality: Personality, action: Action = Action.IDLE): List<String> {
        val personalityLines = when (personality) {
            Personality.PLAYFUL -> when (mood) {
                Mood.NORMAL -> listOf("Let's play!", "Watch this!", "Hehe!")
                Mood.EXCITED -> listOf("Again! Again!", "Watch this!", "Hehe!")
                else -> emptyList()
            }
            Personality.CUDDLY -> when (mood) {
                Mood.NORMAL -> listOf("Stay with me?", "You're my favorite.")
                Mood.LOVE -> listOf("Pet me...", "I like when you do that.", "*snuggles into you*")
                else -> emptyList()
            }
            Personality.LAZY -> when (mood) {
                Mood.NORMAL -> listOf("Do I have to?", "Maybe after one more nap...")
                Mood.SLEEPY -> listOf("Five more minutes...", "Nap time...", "So sleepy...")
                else -> emptyList()
            }
            Personality.MISCHIEVOUS -> when (mood) {
                Mood.NORMAL -> listOf("Hehe... I did nothing.", "Who, me?", "Oops.")
                Mood.EXCITED -> listOf("Bet you didn't see that.", "Who, me?", "Oops.")
                else -> emptyList()
            }
        }
        val actionLines = when (personality to action) {
            Personality.PLAYFUL to Action.PLAY -> listOf("Again! Again!", "Let's play!")
            Personality.CUDDLY to Action.PET -> listOf("Pet me...", "I like when you do that.")
            Personality.CUDDLY to Action.PLAY -> listOf("Stay with me?")
            Personality.LAZY to Action.SLEEP -> listOf("Nap time...", "So sleepy...")
            Personality.LAZY to Action.PLAY -> listOf("Do I have to?")
            Personality.MISCHIEVOUS to Action.FEED -> listOf("Hehe... I did nothing.")
            Personality.MISCHIEVOUS to Action.PLAY -> listOf("Bet you didn't see that.")
            Personality.MISCHIEVOUS to Action.GROOM -> listOf("Oops.")
            else -> emptyList()
        }
        return lineFor(mood) + personalityLines + actionLines
    }
}

object PetCatalog {
    val PUFFLET = PetSpecies(
        id = "pufflet",
        displayName = "Pufflet",
        fur = Color(0xFFEAF8FF),
        furShadow = Color(0xFFB7D9FF),
        earInner = Color(0xFFB48CFF),
        eyeColor = Color(0xFF23304A),
        accent = Color(0xFFFF6FAE),
        flavorLines = mapOf(
            Mood.HAPPY to listOf(
                "Feeling great!", "Best day ever!", "Life is good, hooman."
            ),
            Mood.NORMAL to listOf(
                "I'm ready to play!", "What are we doing today?", "*happy little wiggle*"
            ),
            Mood.HUNGRY to listOf(
                "My tummy is rumbling...", "Got any snacks?", "Feed me, pleeease."
            ),
            Mood.SAD to listOf(
                "I need some love.", "Feeling a little blue...", "Can we hang out?"
            ),
            Mood.SLEEPY to listOf(
                "I could use a nap...", "So... sleepy...", "Five more minutes..."
            ),
            Mood.DIRTY to listOf(
                "I feel kinda grubby.", "Bath time soon?", "Sticky paws, help."
            ),
            Mood.EXCITED to listOf(
                "That was fun!!", "Again! Again!", "Whee!"
            ),
            Mood.LOVE to listOf(
                "I love you! ♥", "Best friend ever.", "*happy purring noises*",
                "I saved a happy wiggle for you!", "You make my day brighter.", "Stay with me a little longer?"
            ),
            Mood.ANGRY to listOf(
                "Hmph! I'm grumpy.", "Hey! Pay attention to me!", "I'm not happy right now."
            ),
            Mood.SICK to listOf(
                "I don't feel so good...", "My tummy feels funny.", "I think I need some care..."
            ),
            Mood.WEAK to listOf(
                "I'm feeling very weak...", "Everything feels wobbly.", "I need to rest..."
            ),
            Mood.CRITICAL to listOf(
                "Please help me!", "I really don't feel good...", "Something is very wrong!"
            )
        )
    )

    val GRIMMBLE = PetSpecies(
        id = "grimmble",
        displayName = "Grimmble",
        fur = Color(0xFFB05FD1),
        furShadow = Color(0xFF7A3C9E),
        earInner = Color(0xFFF5EAFB),
        eyeColor = Color(0xFF1B1740),
        accent = Color(0xFF6C63C7),
        flavorLines = mapOf(
            Mood.HAPPY to listOf(
                "Doom and gloom... but happy gloom.", "Best worst day ever.", "*creepy little wiggle*"
            ),
            Mood.NORMAL to listOf(
                "Bones. Always bones.", "What mischief today?", "I am watching you."
            ),
            Mood.HUNGRY to listOf(
                "Feed me... please.", "My bones are rattling.", "So hollow inside."
            ),
            Mood.SAD to listOf(
                "My heart is broken. Literally.", "Nobody understands me.", "Stay a while?"
            ),
            Mood.SLEEPY to listOf(
                "The shadows call me to rest.", "So... sleepy...", "Five more centuries..."
            ),
            Mood.DIRTY to listOf(
                "I collect grime. It's a look.", "Bath time? Ominous.", "Sticky bones, help."
            ),
            Mood.EXCITED to listOf(
                "Deliciously fun!!", "Again! Again!", "Mwehehe!"
            ),
            Mood.LOVE to listOf(
                "You have my whole broken heart.", "Best friend in this life or the next.", "*ominous purring*"
            ),
            Mood.ANGRY to listOf(
                "Grrr. I'm cranky.", "Hey! Look at me!", "Not in the mood, hooman."
            ),
            Mood.SICK to listOf(
                "I don't feel so undead...", "My bones ache.", "I need some care..."
            ),
            Mood.WEAK to listOf(
                "So very weak...", "Everything is fading.", "I need to rest..."
            ),
            Mood.CRITICAL to listOf(
                "Please help me!", "This is really bad...", "Something is very wrong!"
            )
        )
    )

    val ALL: List<PetSpecies> = listOf(PUFFLET, GRIMMBLE)
}
