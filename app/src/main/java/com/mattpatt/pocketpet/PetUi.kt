package com.mattpatt.pocketpet

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun StatsStrip(health: Int, happiness: Int, energy: Int, cleanliness: Int, fullness: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TinyStat("♥", health, Color(0xFFFF4F88), Modifier.weight(1f))
        TinyStat("☺", happiness, Color(0xFF5FE58A), Modifier.weight(1f))
        TinyStat("⚡", energy, Color(0xFFFFC857), Modifier.weight(1f))
        TinyStat("💧", cleanliness, Color(0xFF58C8FF), Modifier.weight(1f))
        TinyStat("🍽", fullness, Color(0xFFFF9D57), Modifier.weight(1f))
    }
}

@Composable
fun CompactStatusStrip(
    health: Int,
    happiness: Int,
    energy: Int,
    cleanliness: Int,
    fullness: Int,
    affection: Int,
    level: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            TinyStat("♥", health, Color(0xFFFF6688), Modifier.weight(1f))
            TinyStat("☺", happiness, Color(0xFF80E69A), Modifier.weight(1f))
            TinyStat("⚡", energy, Color(0xFFFFD166), Modifier.weight(1f))
            TinyStat("💧", cleanliness, Color(0xFF72D5FF), Modifier.weight(1f))
            TinyStat("🍴", fullness, Color(0xFFFFA15C), Modifier.weight(1f))
            TinyStat("♥", affection, Color(0xFFFF9BC7), Modifier.weight(1f))
        }
        Text(
            text = "Lv $level",
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PuffletBackdrop() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stars = listOf(
            Offset(size.width * 0.18f, size.height * 0.22f),
            Offset(size.width * 0.78f, size.height * 0.18f),
            Offset(size.width * 0.86f, size.height * 0.44f),
            Offset(size.width * 0.12f, size.height * 0.56f),
            Offset(size.width * 0.72f, size.height * 0.68f),
            Offset(size.width * 0.28f, size.height * 0.78f)
        )
        stars.forEachIndexed { index, center ->
            val radius = if (index % 2 == 0) 2.2f else 1.4f
            drawCircle(Color(0xFFFFD98A).copy(alpha = 0.42f), radius, center)
        }
        drawArc(
            color = Color(0xFFFFC978).copy(alpha = 0.16f),
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(size.width * 0.68f, size.height * 0.08f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.16f, size.height * 0.12f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )
    }
}

@Composable
private fun TinyStat(icon: String, value: Int, color: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, color = color, fontSize = 10.sp)
        Box(
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(value.coerceIn(0, 100) / 100f).height(4.dp)
                    .clip(CircleShape).background(color)
            )
        }
    }
}

@Composable
fun ActionBubble(label: String, emoji: String, color: Color, enabled: Boolean = true, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = if (enabled) 0.92f else 0.35f))
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = label }
            .padding(top = 9.dp)
    ) {
        Text(emoji, fontSize = 20.sp, color = Color.White)
    }
}

@Composable
fun AnimatedPet(
    species: PetSpecies,
    mood: Mood,
    action: Action,
    actionId: Int,
    idlePulse: Int,
    personality: Personality,
    onTap: () -> Unit,
    fullness: Int,
    health: Int,
    cleanliness: Int,
    energy: Int
) {
    val infinite = rememberInfiniteTransition(label = "petMotion")
    val bob by infinite.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.98f, targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val idlePhase by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing), RepeatMode.Restart),
        label = "idlePhase"
    )

    val actionProgress by animateFloatAsState(
        targetValue = if (action == Action.IDLE) 0f else 1f,
        animationSpec = tween(950),
        label = "actionProgress"
    )

    // A short four-step blink: open, half closed, closed, half closed, open.
    val blink by produceState(initialValue = 0f) {
        while (true) {
            delay((2800..4800).random().toLong())
            value = 0.45f
            delay(55)
            value = 1f
            delay(110)
            value = 0.45f
            delay(55)
            value = 0f
        }
    }

    val visualState = mood.toVisualPuffletState()
    val animation = puffletAnimationState(visualState, action, actionProgress, idlePhase, blink)
    val safeForPersonality = visualState == VisualPuffletState.NORMAL ||
        visualState == VisualPuffletState.HAPPY ||
        visualState == VisualPuffletState.AFFECTIONATE
    val personalityBob = if (action == Action.IDLE && safeForPersonality) {
        when (personality) {
            Personality.PLAYFUL -> bob * 1.35f
            Personality.CUDDLY -> bob * 1.1f
            Personality.LAZY -> bob * 0.65f
            Personality.MISCHIEVOUS -> bob * 1.15f
        }
    } else {
        bob
    }
    val stateLift = when (visualState) {
        VisualPuffletState.SAD, VisualPuffletState.SICK, VisualPuffletState.WEAK -> 4f
        VisualPuffletState.SLEEPY -> 2f
        else -> 0f
    }
    val stateScale = when (visualState) {
        VisualPuffletState.SAD, VisualPuffletState.SICK, VisualPuffletState.WEAK -> 0.96f
        VisualPuffletState.EXCITED -> 1.02f
        else -> 1f
    }
    val bounceOffset = animation.bounce
    val wobble = animation.shake + if (mood == Mood.SICK || mood == Mood.WEAK) {
        sin((idlePhase + 0.15f) * Math.PI * 2.0).toFloat() * 1.5f
    } else 0f
    val actionLean = animation.lean + if (action == Action.SLEEP) 4f * actionProgress else 0f
    val petScale = if (action == Action.PET) {
        1f + 0.035f * sin(actionProgress * Math.PI).toFloat()
    } else {
        pulse * stateScale
    }

    Box(
        modifier = Modifier
            .size(160.dp)
            .pointerInput(actionId) {
                detectTapGestures(onTap = { onTap() })
            }
            .graphicsLayer(
                scaleX = petScale,
                scaleY = petScale,
                translationY = personalityBob + animation.breathing + bounceOffset + stateLift + actionLean,
                rotationZ = wobble + animation.tailWag * 0.12f + animation.earTwitch * 0.08f +
                    if (action == Action.PET) -2f * actionProgress else 0f
            ),
        contentAlignment = Alignment.Center
    ) {
        // Species dispatch: each pet has its own frame-based sprite
        // renderer. Everything around this (breathing, bob, wobble,
        // lean, particle overlays below) is species-agnostic and stays
        // shared - only which sprite composable draws the body changes.
        if (species.id == PetCatalog.GRIMMBLE.id) {
            GrimmbleSprite(
                action = action,
                actionProgress = actionProgress,
                idlePhase = idlePhase,
                blink = blink,
                modifier = Modifier
                    .size(128.dp)
                    .align(Alignment.Center)
            )
        } else {
            PuffletSprite(
                visualState = visualState,
                action = action,
                actionProgress = actionProgress,
                idlePhase = idlePhase,
                blink = blink,
                modifier = Modifier
                    .size(128.dp)
                    .align(Alignment.Center)
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f + 8f

            if (action == Action.FEED) {
                val chomp = sin(actionProgress * Math.PI * 6.0).toFloat() * 2f
                drawCircle(
                    color = Color.White.copy(alpha = 0.32f),
                    radius = 5f + chomp,
                    center = Offset(cx + size.width * 0.18f, cy + size.height * 0.08f)
                )
            }

            if (action == Action.PLAY) {
                val ballX = cx - 68f + actionProgress * 136f
                val ballY = cy + 25f - sin(actionProgress * Math.PI).toFloat() * 45f
                drawCircle(Color(0xFFFF5C77), 11f, Offset(ballX, ballY))
                drawCircle(Color.White.copy(alpha = 0.85f), 3f, Offset(ballX - 3f, ballY - 3f))
            }

            if (action == Action.FEED) {
                val foodX = cx + 65f - actionProgress * 62f
                val foodY = cy + 20f
                drawCircle(Color(0xFFFF4757), 13f, Offset(foodX, foodY))
                drawCircle(Color(0xFF71B75B), 4f, Offset(foodX + 5f, foodY - 12f))
                drawArc(
                    color = Color.White.copy(alpha = 0.8f),
                    startAngle = 200f, sweepAngle = 50f, useCenter = false,
                    topLeft = Offset(foodX - 7f, foodY - 7f), size = Size(14f, 14f),
                    style = Stroke(width = 2.5f)
                )
            }
        }

        if (mood == Mood.CRITICAL) {
            Text("!", color = Color(0xFFFFA726), fontSize = 20.sp, fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 7.dp, end = 12.dp))
        }
    }
}

@Composable
fun GameOverScreen(petName: String, onNewPet: (PetSpecies) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF08111F)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            drawTombstone(this, size.width / 2f, size.height / 2f)
        }
        Text(
            "$petName didn't make it",
            color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp)
        )
        Text(
            "No food for 4 days...",
            color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp,
            textAlign = TextAlign.Center, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )
        Row {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF7C4DFF))
                    .clickable(onClick = { onNewPet(PetCatalog.PUFFLET) })
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("New Pufflet", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF7A3C9E))
                    .clickable(onClick = { onNewPet(PetCatalog.GRIMMBLE) })
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Text("New Grimmble", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PetSelectionScreen(
    selectedSpecies: PetSpecies,
    onSelect: (PetSpecies) -> Unit,
    onClose: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "petSelectionPreview")
    val idlePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing), RepeatMode.Restart),
        label = "selectionIdlePhase"
    )
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF08111F)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Choose your pet", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(
            "Your stats and progress stay with you",
            color = Color.White.copy(alpha = 0.65f), fontSize = 9.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PetSelectionCard(
                species = PetCatalog.PUFFLET,
                selected = selectedSpecies.id == PetCatalog.PUFFLET.id,
                idlePhase = idlePhase,
                onClick = { onSelect(PetCatalog.PUFFLET) },
                modifier = Modifier.weight(1f)
            )
            PetSelectionCard(
                species = PetCatalog.GRIMMBLE,
                selected = selectedSpecies.id == PetCatalog.GRIMMBLE.id,
                idlePhase = idlePhase,
                onClick = { onSelect(PetCatalog.GRIMMBLE) },
                modifier = Modifier.weight(1f)
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .clip(CircleShape)
                .background(Color(0xFF253550))
                .clickable(onClick = onClose)
                .padding(horizontal = 22.dp, vertical = 8.dp)
        ) {
            Text("BACK", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PetSelectionCard(
    species: PetSpecies,
    selected: Boolean,
    idlePhase: Float,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFF314B68) else Color(0xFF17263B))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(112.dp), contentAlignment = Alignment.Center) {
            if (species.id == PetCatalog.GRIMMBLE.id) {
                GrimmbleSprite(
                    action = Action.IDLE,
                    actionProgress = 0f,
                    idlePhase = idlePhase,
                    blink = 0f,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PuffletSprite(
                    visualState = VisualPuffletState.NORMAL,
                    action = Action.IDLE,
                    actionProgress = 0f,
                    idlePhase = idlePhase,
                    blink = 0f,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Text(species.displayName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(
            if (selected) "CURRENT" else "SELECT",
            color = if (selected) Color(0xFF8BE9A5) else Color.White.copy(alpha = 0.65f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun DevPetTestScreen(stats: PetStats, onSet: (String) -> Unit, onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF08111F)).padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("DEV PET TEST", color = Color(0xFFFFB347), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text("HP ${stats.health}  •  Happy ${stats.happiness}", color = Color.White.copy(alpha = .8f), fontSize = 9.sp)
        Text("Energy ${stats.energy}  •  Clean ${stats.cleanliness}  •  Food ${stats.fullness}", color = Color.White.copy(alpha = .8f), fontSize = 9.sp)
        Spacer(Modifier.height(5.dp))
        val rows = listOf(
            listOf("HAPPY", "HUNGRY", "DIRTY"),
            listOf("SAD", "SLEEPY", "ANGRY"),
            listOf("SICK", "WEAK", "CRITICAL"),
            listOf("RESET")
        )
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowItems.forEach { item ->
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF253550))
                            .clickable { onSet(item) }
                            .padding(horizontal = 9.dp, vertical = 7.dp)
                    ) {
                        Text(item, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier.clip(CircleShape).background(Color(0xFF7C4DFF)).clickable(onClick = onClose)
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Text("CLOSE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text("Long-press the name/Lv row to reopen", color = Color.White.copy(alpha = .45f), fontSize = 8.sp,
            textAlign = TextAlign.Center, modifier = Modifier.padding(top = 5.dp))
    }
}
