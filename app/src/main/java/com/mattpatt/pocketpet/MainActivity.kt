package com.mattpatt.pocketpet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PocketPetApp() }
    }
}

@Composable
private fun PocketPetApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { PetStore(context) }

    val loaded = remember { store.loadAndCatchUp(System.currentTimeMillis()) }
    // Was `remember(loaded.speciesId)` off a one-shot `loaded` value, so it
    // never actually updated after starting a new pet of a different
    // species - made it real mutable state so choosing Grimmble at the
    // GameOverScreen actually sticks.
    var species by remember {
        mutableStateOf(PetCatalog.ALL.firstOrNull { it.id == loaded.speciesId } ?: PetCatalog.PUFFLET)
    }

    var stats by remember { mutableStateOf(loaded.stats) }
    var lastFedAt by remember { mutableStateOf(loaded.lastFedAt) }
    var lastTickAt by remember { mutableStateOf(loaded.lastTickAt) }
    var isDead by remember { mutableStateOf(loaded.isDead) }
    var petName by remember { mutableStateOf(loaded.petName) }
    var action by remember { mutableStateOf(Action.IDLE) }
    var actionId by remember { mutableStateOf(0) }
    var idleTicks by remember { mutableStateOf(0) }
    var idlePulse by remember { mutableStateOf(0) }
    var devOpen by remember { mutableStateOf(false) }
    var petSelectorOpen by remember { mutableStateOf(false) }

    val mood = when {
        isDead -> Mood.CRITICAL
        action == Action.FEED -> Mood.HAPPY
        action == Action.PLAY -> Mood.EXCITED
        action == Action.PET -> Mood.LOVE
        action == Action.GROOM -> Mood.HAPPY
        action == Action.SLEEP -> Mood.SLEEPY
        stats.health <= 20 -> Mood.CRITICAL
        stats.health <= 42 -> Mood.SICK
        stats.energy <= 12 -> Mood.WEAK
        stats.happiness <= 12 -> Mood.ANGRY
        stats.energy <= 22 -> Mood.SLEEPY
        stats.fullness <= 22 -> Mood.HUNGRY
        stats.cleanliness <= 22 -> Mood.DIRTY
        stats.happiness <= 25 -> Mood.SAD
        stats.affection >= 80 -> Mood.LOVE
        stats.happiness >= 90 -> Mood.HAPPY
        else -> Mood.NORMAL
    }

    fun persist() {
        store.save(stats, lastFedAt, lastTickAt, isDead, petName, species.id)
    }

    fun addXp(amount: Int) {
        var xp = stats.xp + amount
        var level = stats.level
        var happiness = stats.happiness
        while (xp >= 100) {
            xp -= 100
            level += 1
            happiness = (happiness + 8).coerceAtMost(100)
        }
        stats = stats.copy(xp = xp, level = level, happiness = happiness)
    }

    fun doFeed() {
        if (action != Action.IDLE || isDead) return
        idleTicks = 0
        action = Action.FEED
        actionId++
        val now = System.currentTimeMillis()
        lastFedAt = now
        stats = stats.copy(
            fullness = (stats.fullness + 25).coerceAtMost(100),
            happiness = (stats.happiness + 7).coerceAtMost(100),
            health = (stats.health + if (stats.health < 80) 2 else 0).coerceAtMost(100),
            energy = (stats.energy - 2).coerceAtLeast(0),
            affection = (stats.affection + 2).coerceIn(0, 100)
        )
        addXp(12)
        persist()
    }

    fun doPlay() {
        if (action != Action.IDLE || isDead) return
        if (stats.energy < 8) return
        idleTicks = 0
        action = Action.PLAY
        actionId++
        stats = stats.copy(
            happiness = (stats.happiness + 20).coerceAtMost(100),
            energy = (stats.energy - 10).coerceAtLeast(0),
            fullness = (stats.fullness - 5).coerceAtLeast(0),
            affection = (stats.affection + 5).coerceIn(0, 100)
        )
        addXp(18)
        persist()
    }

    fun doPet() {
        if (action != Action.IDLE || isDead) return
        idleTicks = 0
        action = Action.PET
        actionId++
        stats = stats.copy(
            happiness = (stats.happiness + 12).coerceAtMost(100),
            affection = (stats.affection + 6).coerceIn(0, 100)
        )
        addXp(5)
        persist()
    }

    fun doGroom() {
        if (action != Action.IDLE || isDead) return
        idleTicks = 0
        action = Action.GROOM
        actionId++
        stats = stats.copy(
            cleanliness = (stats.cleanliness + 35).coerceAtMost(100),
            happiness = (stats.happiness + 6).coerceAtMost(100),
            health = (stats.health + if (stats.cleanliness < 30) 2 else 0).coerceAtMost(100),
            affection = (stats.affection + 2).coerceIn(0, 100)
        )
        addXp(10)
        persist()
    }

    fun doSleep() {
        if (action != Action.IDLE || isDead) return
        if (stats.energy >= 92) return
        idleTicks = 0
        action = Action.SLEEP
        actionId++
        stats = stats.copy(
            energy = (stats.energy + 30).coerceAtMost(100),
            health = (stats.health + 5).coerceAtMost(100),
            happiness = (stats.happiness + 4).coerceAtMost(100)
        )
        addXp(4)
        persist()
    }

    fun startNewPet(newSpecies: PetSpecies) {
        val now = System.currentTimeMillis()
        val fresh = store.resetForNewPet(now, petName = newSpecies.displayName, speciesId = newSpecies.id)
        stats = fresh.stats
        lastFedAt = fresh.lastFedAt
        lastTickAt = fresh.lastTickAt
        isDead = false
        petName = fresh.petName
        species = newSpecies
        action = Action.IDLE
    }

    fun switchSpecies(newSpecies: PetSpecies) {
        if (newSpecies.id == species.id) {
            petSelectorOpen = false
            return
        }
        species = newSpecies
        action = Action.IDLE
        actionId++
        persist()
        petSelectorOpen = false
    }

    fun devSet(name: String) {
        val updated = when (name) {
            "HAPPY" -> stats.copy(health = 95, happiness = 95, energy = 90, cleanliness = 95, fullness = 90)
            "HUNGRY" -> stats.copy(fullness = 12)
            "DIRTY" -> stats.copy(cleanliness = 12)
            "SAD" -> stats.copy(happiness = 20)
            "ANGRY" -> stats.copy(happiness = 8)
            "SLEEPY" -> stats.copy(energy = 12)
            "SICK" -> stats.copy(health = 40)
            "WEAK" -> stats.copy(health = 65, energy = 8)
            "CRITICAL" -> stats.copy(health = 15, fullness = 8, energy = 8)
            "RESET" -> PetStats()
            else -> stats
        }
        stats = updated
        if (name == "RESET") {
            val now = System.currentTimeMillis()
            lastFedAt = now
            lastTickAt = now
            isDead = false
            action = Action.IDLE
        }
        persist()
    }

    LaunchedEffect(actionId) {
        if (action != Action.IDLE) {
            delay(if (action == Action.SLEEP) 2200 else 1300)
            action = Action.IDLE
            persist()
        }
    }

    LaunchedEffect(isDead) {
        if (isDead) return@LaunchedEffect
        while (true) {
            delay(60_000)
            val now = System.currentTimeMillis()
            val hoursSinceFed = (now - lastFedAt) / 3_600_000.0
            if (hoursSinceFed >= PetStore.DEATH_HOURS) {
                stats = stats.copy(health = 0)
                lastTickAt = now
                isDead = true
                persist()
                return@LaunchedEffect
            }
            stats = store.applyDecay(stats, minutesElapsed = 1.0)
            lastTickAt = now
            if (action == Action.IDLE) {
                idleTicks++
                if (idleTicks >= 2) {
                    idlePulse++
                    idleTicks = 0
                }
            }
            persist()
        }
    }

    if (isDead) {
        GameOverScreen(petName = petName, onNewPet = ::startNewPet)
        return
    }
    if (petSelectorOpen) {
        PetSelectionScreen(
            selectedSpecies = species,
            onSelect = ::switchSpecies,
            onClose = { petSelectorOpen = false }
        )
        return
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF263550), Color(0xFF0A1020)),
                    radius = 620f
                )
            )
    ) {
        PuffletBackdrop()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        CompactStatusStrip(
            health = stats.health,
            happiness = stats.happiness,
            energy = stats.energy,
            cleanliness = stats.cleanliness,
            fullness = stats.fullness,
            affection = stats.affection,
            level = stats.level
        )
        Box(
            modifier = Modifier
                .padding(top = 2.dp, bottom = 2.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .clickable { petSelectorOpen = true }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            androidx.compose.material3.Text(
                text = "Change Pet: ${species.displayName}",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedPet(
                species = species,
                mood = mood,
                action = action,
                actionId = actionId,
                idlePulse = idlePulse,
                personality = stats.personality,
                onTap = ::doPet,
                fullness = stats.fullness,
                health = stats.health,
                cleanliness = stats.cleanliness,
                energy = stats.energy
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionBubble(label = "Feed", emoji = "🍎", color = Color(0xFFFF7043), onClick = ::doFeed)
            ActionBubble(label = "Play", emoji = "🎾", color = Color(0xFF2196F3), enabled = stats.energy >= 8, onClick = ::doPlay)
            ActionBubble(label = "Groom", emoji = "🧼", color = Color(0xFF26BFA5), onClick = ::doGroom)
            ActionBubble(label = "Sleep", emoji = "🌙", color = Color(0xFF635BEB), onClick = ::doSleep)
        }
        }
    }
}