package com.mattpatt.pocketpet

import android.content.Context
import kotlin.math.max

/**
 * Tamagotchi-style pets need to keep decaying even when the watch app isn't
 * open. Instead of only ticking stats down while the Composable is alive
 * (which is what v0.4 did), we timestamp every save and, on load, replay
 * however much real-world time has passed. The 4-day "no feeding = death"
 * rule is checked against the actual clock, not app-open time, so it works
 * the way the user described it.
 */
class PetStore(context: Context) {
    private val prefs = context.getSharedPreferences("pocket_pet", Context.MODE_PRIVATE)

    companion object {
        const val DEATH_HOURS = 96.0 // 4 days without a single feeding

        // Decay rates, expressed as "points lost per minute" at full pace.
        private const val FULLNESS_PER_MIN = 100.0 / (16.7 * 60.0)
        private const val ENERGY_PER_MIN = 100.0 / (20.0 * 60.0)
        private const val CLEAN_PER_MIN = 100.0 / (25.0 * 60.0)
        private const val HAPPY_BASELINE_PER_MIN = 100.0 / (50.0 * 60.0)
        private const val HAPPY_CRITICAL_PER_MIN = 100.0 / (12.0 * 60.0)
        private const val STARVE_HEALTH_PER_MIN = 100.0 / (33.0 * 60.0)
    }

    fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)
    fun getLong(key: String, default: Long): Long = prefs.getLong(key, default)
    fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)
    fun getString(key: String, default: String): String = prefs.getString(key, default) ?: default

    fun save(
        stats: PetStats,
        lastFedAt: Long,
        lastTickAt: Long,
        isDead: Boolean,
        petName: String,
        speciesId: String
    ) {
        prefs.edit()
            .putInt("health", stats.health)
            .putInt("happiness", stats.happiness)
            .putInt("energy", stats.energy)
            .putInt("cleanliness", stats.cleanliness)
            .putInt("fullness", stats.fullness)
            .putInt("affection", stats.affection)
            .putInt("level", stats.level)
            .putInt("xp", stats.xp)
            .putString("personality", stats.personality.name)
            .putLong("lastFedAt", lastFedAt)
            .putLong("lastTickAt", lastTickAt)
            .putBoolean("isDead", isDead)
            .putString("petName", petName)
            .putString("speciesId", speciesId)
            .apply()
    }

    data class LoadResult(
        val stats: PetStats,
        val lastFedAt: Long,
        val lastTickAt: Long,
        val isDead: Boolean,
        val petName: String,
        val speciesId: String,
        val justDied: Boolean
    )

    /**
     * Loads saved state and fast-forwards decay for any time the app was
     * closed. Call this once, on launch.
     */
    fun loadAndCatchUp(now: Long): LoadResult {
        val wasAlreadyDead = getBoolean("isDead", false)
        var stats = PetStats(
            health = getInt("health", 90),
            happiness = getInt("happiness", 80),
            energy = getInt("energy", 85),
            cleanliness = getInt("cleanliness", 90),
            fullness = getInt("fullness", 75),
            affection = getInt("affection", 50).coerceIn(0, 100),
            level = getInt("level", 1),
            xp = getInt("xp", 0),
            personality = getString("personality", "").let { saved ->
                runCatching { Personality.valueOf(saved) }
                    .getOrElse { enumValues<Personality>().random() }
            }
        )
        val lastFedAt = getLong("lastFedAt", now)
        val lastTickAt = getLong("lastTickAt", now)
        val petName = getString("petName", "Pufflet")
        val speciesId = getString("speciesId", PetCatalog.PUFFLET.id)

        if (wasAlreadyDead) {
            save(stats, lastFedAt, lastTickAt, isDead = true, petName = petName, speciesId = speciesId)
            return LoadResult(stats, lastFedAt, lastTickAt, true, petName, speciesId, justDied = false)
        }

        val hoursSinceFed = (now - lastFedAt) / 3_600_000.0
        if (hoursSinceFed >= DEATH_HOURS) {
            val deadStats = stats.copy(health = 0)
            save(deadStats, lastFedAt, now, isDead = true, petName = petName, speciesId = speciesId)
            return LoadResult(deadStats, lastFedAt, now, true, petName, speciesId, justDied = true)
        }

        stats = applyDecay(stats, minutesElapsed = max(0.0, (now - lastTickAt) / 60_000.0))
        save(stats, lastFedAt, now, isDead = false, petName = petName, speciesId = speciesId)
        return LoadResult(stats, lastFedAt, now, false, petName, speciesId, justDied = false)
    }

    /** Applies proportional decay for `minutesElapsed` minutes of real time. */
    fun applyDecay(stats: PetStats, minutesElapsed: Double): PetStats {
        if (minutesElapsed <= 0.0) return stats
        val newFullness = (stats.fullness - FULLNESS_PER_MIN * minutesElapsed).coerceIn(0.0, 100.0)
        val newEnergy = (stats.energy - ENERGY_PER_MIN * minutesElapsed).coerceIn(0.0, 100.0)
        val newClean = (stats.cleanliness - CLEAN_PER_MIN * minutesElapsed).coerceIn(0.0, 100.0)

        val critical = newFullness < 20 || newEnergy < 20 || newClean < 20
        val happyRate = if (critical) HAPPY_CRITICAL_PER_MIN else HAPPY_BASELINE_PER_MIN
        val newHappiness = (stats.happiness - happyRate * minutesElapsed).coerceIn(0.0, 100.0)

        val healthLossPerMin = when {
            newFullness <= 0.0 && newClean <= 0.0 -> STARVE_HEALTH_PER_MIN * 1.35
            newFullness <= 0.0 -> STARVE_HEALTH_PER_MIN
            newClean <= 0.0 -> STARVE_HEALTH_PER_MIN * 0.35
            else -> 0.0
        }
        val newHealth = (stats.health - healthLossPerMin * minutesElapsed).coerceIn(0.0, 100.0)

        return stats.copy(
            fullness = newFullness.toInt(),
            energy = newEnergy.toInt(),
            cleanliness = newClean.toInt(),
            happiness = newHappiness.toInt(),
            health = newHealth.toInt()
        )
    }

    /** Wipes everything and starts a brand new pet. */
    fun resetForNewPet(now: Long, petName: String, speciesId: String): LoadResult {
        val fresh = PetStats(personality = enumValues<Personality>().random())
        save(fresh, lastFedAt = now, lastTickAt = now, isDead = false, petName = petName, speciesId = speciesId)
        return LoadResult(fresh, now, now, false, petName, speciesId, justDied = false)
    }
}
