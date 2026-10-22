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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val background = Color(0xFF0C1426)
private val cardColor = Color(0xFF192B47)
private val accent = Color(0xFF32D6A0)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                LifeQuestScreen()
            }
        }
    }
}

@Composable
fun LifeQuestScreen() {

    val quests = remember {
        listOf(
            Quest(1, "Study Kotlin", 50),
            Quest(2, "Solve DSA Problems", 100),
            Quest(3, "Workout", 30)
        )
    }

    var completedIds by rememberSaveable {
        mutableStateOf(listOf<Int>())
    }

    val state = completedIds.fold(
        QuestState(quests)
    ) { current, id ->
        completeQuest(current, id)
    }

    val level = state.totalXP / 100 + 1
    val levelProgress = (state.totalXP % 100) / 100f
    val completedCount = state.quests.count { it.completed }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Text(
                text = "LifeQuest ⚡",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "Turn your goals into victories.",
                color = Color.LightGray
            )

            Spacer(Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(20.dp)) {

                    Text(
                        text = "LEVEL $level",
                        color = accent
                    )

                    Text(
                        text = "${state.totalXP} XP",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { levelProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = accent
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Today's Quests ($completedCount/${quests.size})",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
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
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = quest.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = "+${quest.xp} XP",
                                    color = accent
                                )
                            }

                            Button(
                                enabled = !quest.completed,
                                onClick = {
                                    completedIds =
                                        completedIds + quest.id
                                }
                            ) {
                                Text(
                                    if (quest.completed) "Done"
                                    else "Finish"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
