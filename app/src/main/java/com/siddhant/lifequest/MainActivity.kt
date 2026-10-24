
package com.siddhant.lifequest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val backgroundColor = Color(0xFF0B1220)
private val cardColor = Color(0xFF172338)
private val accentColor = Color(0xFF37E5B1)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = accentColor,
                    background = backgroundColor,
                    surface = cardColor
                )
            ) {
                LifeQuestApp()
            }
        }
    }
}

@Composable
@Preview(showSystemUi=true)
fun LifeQuestApp() {

    var onboardingDone by rememberSaveable {
        mutableStateOf(false)
    }

    if (onboardingDone) {
        LifeQuestScreen()
    } else {
        OnboardingScreen(
            onGetStarted = {
                onboardingDone = true
            }
        )
    }
}

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "⚡",
                fontSize = 72.sp
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Welcome to LifeQuest",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Turn daily goals into quests.\n" +
                        "Complete tasks, earn XP and level up!",
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get Started")
            }
        }
    }
}

@Composable
fun LifeQuestScreen() {

    val quests = remember {
        listOf(
            Quest(1, "Study Kotlin", 50, Difficulty.EASY),
            Quest(2, "Solve DSA Problems", 50, Difficulty.MEDIUM),
            Quest(3, "Workout", 30, Difficulty.HARD)
        )
    }

    var completedIds by rememberSaveable {
        mutableStateOf(listOf<Int>())
    }

    val state = completedIds.fold(QuestState(quests)) { current, id ->
        when (val result = completeQuest(current, id)) {
            is QuestResult.Success -> result.state
            QuestResult.AlreadyCompleted -> current
            QuestResult.NotFound -> current
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp)
        ) {

            Text(
                text = "LifeQuest ⚡",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {

                    Text(
                        text = "LEVEL ${state.level}",
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "${state.totalXP} TOTAL XP",
                        color = Color.White
                    )

                    Spacer(Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = {
                            (state.totalXP % 100) / 100f
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = accentColor
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Today's Quests",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.quests, key = { it.id }) { quest ->

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = cardColor
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {

                            Text(
                                text = quest.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(6.dp))

                            val reward =
                                quest.xp * quest.difficulty.multiplier

                            Text(
                                text = "${quest.difficulty.name} • +$reward XP",
                                color = accentColor
                            )

                            Spacer(Modifier.height(12.dp))

                            Button(
                                enabled = !quest.completed,
                                onClick = {
                                    completedIds =
                                        completedIds + quest.id
                                }
                            ) {
                                Text(
                                    if (quest.completed) "Completed ✓"
                                    else "Finish Quest"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
