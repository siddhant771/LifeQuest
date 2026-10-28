
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private val backgroundColor = Color(0xFF0B1220)
private val cardColor = Color(0xFF172338)
private val accentColor = Color(0xFF37E5B1)

private object LifeQuestRoutes {
    const val DASHBOARD = "dashboard"
    const val DETAIL = "detail/{questId}"
    const val PROFILE = "profile"

    fun detail(questId: Int) = "detail/$questId"
}

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
                AsyncQuestDemoScreen()
            }
        }
    }
}

@Composable

fun LifeQuestApp() {
    var onboardingDone by rememberSaveable {
        mutableStateOf(false)
    }

    if (onboardingDone) {
        LifeQuestNavigation()
    } else {
        OnboardingScreen(
            onGetStarted = { onboardingDone = true }
        )
    }
}

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
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
            Text(text = "⚡", fontSize = 72.sp)

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

/*
 * Shared state is owned here so that quests and XP remain
 * consistent while navigating between screens.
 */
@Composable
fun LifeQuestNavigation() {
    val navController = rememberNavController()

    var quests by rememberSaveable {
        mutableStateOf(
            listOf(
                Quest(1, "Study Kotlin", 50, Difficulty.EASY),
                Quest(2, "Solve DSA Problems", 50, Difficulty.MEDIUM),
                Quest(3, "Workout", 30, Difficulty.HARD)
            )
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

    fun completeQuestById(id: Int) {
        if (quests.any { it.id == id } && id !in completedIds) {
            completedIds = completedIds + id
        }
    }

    fun createQuest(title: String, xp: Int, difficulty: Difficulty) {
        val nextId = (quests.maxOfOrNull { it.id } ?: 0) + 1

        quests = quests + Quest(
            id = nextId,
            title = title,
            xp = xp,
            difficulty = difficulty
        )
    }

    NavHost(
        navController = navController,
        startDestination = LifeQuestRoutes.DASHBOARD
    ) {
        composable(LifeQuestRoutes.DASHBOARD) {
            DashboardScreen(
                state = state,
                onCreateQuest = ::createQuest,
                onQuestClick = { questId ->
                    navController.navigate(
                        LifeQuestRoutes.detail(questId)
                    )
                },
                onProfileClick = {
                    navController.navigate(LifeQuestRoutes.PROFILE)
                },
                onCompleteQuest = ::completeQuestById
            )
        }

        composable(
            route = LifeQuestRoutes.DETAIL,
            arguments = listOf(
                navArgument("questId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val questId = backStackEntry.arguments?.getInt("questId")
            val quest = state.quests.find { it.id == questId }

            if (quest != null) {
                QuestDetailScreen(
                    quest = quest,
                    onBack = { navController.popBackStack() },
                    onComplete = { completeQuestById(quest.id) }
                )
            } else {
                MissingQuestScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(LifeQuestRoutes.PROFILE) {
            ProfileScreen(
                state = state,
                completedCount = completedIds.size,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun DashboardScreen(
    state: QuestState,
    onCreateQuest: (String, Int, Difficulty) -> Unit,
    onQuestClick: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onCompleteQuest: (Int) -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LifeQuest ⚡",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(onClick = onProfileClick) {
                    Text("Profile")
                }
            }

            Spacer(Modifier.height(16.dp))

            XPWidget(
                totalXP = state.totalXP,
                level = state.level
            )

            Spacer(Modifier.height(16.dp))

            CreateQuestForm(onCreateQuest = onCreateQuest)

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Today's Quests",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.quests, key = { it.id }) { quest ->
                    Column {
                        QuestCard(
                            quest = quest,
                            onComplete = {
                                onCompleteQuest(quest.id)
                            }
                        )

                        TextButton(
                            onClick = { onQuestClick(quest.id) }
                        ) {
                            Text("View Quest Details →")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestDetailScreen(
    quest: Quest,
    onBack: () -> Unit,
    onComplete: () -> Unit
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
            verticalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = onBack) {
                Text("← Back to Dashboard")
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Quest Details",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = quest.title,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Difficulty: ${quest.difficulty.name}",
                        color = Color.LightGray
                    )

                    Text(
                        text = "Base XP: ${quest.xp}",
                        color = Color.LightGray
                    )

                    Text(
                        text = "Reward: ${quest.xp * quest.difficulty.multiplier} XP",
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = onComplete,
                        enabled = !quest.completed,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (quest.completed) {
                                "Completed"
                            } else {
                                "Complete Quest"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    state: QuestState,
    completedCount: Int,
    onBack: () -> Unit
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
            verticalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = onBack) {
                Text("← Back to Dashboard")
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Your Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(24.dp))

            XPWidget(
                totalXP = state.totalXP,
                level = state.level
            )

            Spacer(Modifier.height(20.dp))

            ProfileInfoCard(
                label = "Total Quests",
                value = state.quests.size.toString()
            )

            Spacer(Modifier.height(12.dp))

            ProfileInfoCard(
                label = "Completed Quests",
                value = completedCount.toString()
            )

            Spacer(Modifier.height(12.dp))

            ProfileInfoCard(
                label = "Current Level",
                value = state.level.toString()
            )
        }
    }
}

@Composable
private fun ProfileInfoCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.LightGray)
            Text(
                value,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MissingQuestScreen(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Quest not found",
                color = Color.White,
                fontSize = 22.sp
            )

            Spacer(Modifier.height(16.dp))

            Button(onClick = onBack) {
                Text("Go Back")
            }
        }
    }
}

@Composable
fun CreateQuestForm(
    onCreateQuest: (String, Int, Difficulty) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var xpText by rememberSaveable { mutableStateOf("50") }
    var difficultyName by rememberSaveable {
        mutableStateOf(Difficulty.EASY.name)
    }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    val difficulty = Difficulty.valueOf(difficultyName)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Create New Quest",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMessage = ""
                },
                label = { Text("Quest title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = xpText,
                onValueChange = {
                    xpText = it
                    errorMessage = ""
                },
                label = { Text("Base XP reward") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Difficulty: ${difficulty.name}")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Difficulty.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = {
                                difficultyName = option.name
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    val cleanTitle = title.trim()
                    val xp = xpText.toIntOrNull()

                    when {
                        cleanTitle.isEmpty() -> {
                            errorMessage = "Enter a quest title."
                        }

                        xp == null || xp <= 0 -> {
                            errorMessage = "Enter a valid positive XP."
                        }

                        else -> {
                            onCreateQuest(cleanTitle, xp, difficulty)

                            title = ""
                            xpText = "50"
                            difficultyName = Difficulty.EASY.name
                            errorMessage = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Quest")
            }
        }
    }
}

@Composable
fun XPProgress(progress: Float) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth(),
        color = accentColor,
        trackColor = Color(0xFF334155)
    )
}

@Composable
fun XPWidget(totalXP: Int, level: Int) {
    val currentXP = totalXP % 100
    val progress = currentXP / 100f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = "LEVEL $level",
                color = accentColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$totalXP TOTAL XP", color = Color.White)
                Text("$currentXP / 100 XP", color = Color.LightGray)
            }

            Spacer(Modifier.height(12.dp))

            XPProgress(progress)

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${100 - currentXP} XP until Level ${level + 1}",
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun QuestCard(
    quest: Quest,
    onComplete: () -> Unit
) {
    val reward = quest.xp * quest.difficulty.multiplier

    Card(
        modifier = Modifier.fillMaxWidth(),
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

            Text(
                text = "${quest.difficulty.name} +$reward XP",
                color = accentColor
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onComplete,
                enabled = !quest.completed
            ) {
                Text(
                    if (quest.completed) {
                        "Completed"
                    } else {
                        "Finish Quest"
                    }
                )
            }
        }
    }
}
@Composable
fun AsyncQuestSection(
    viewModel: QuestViewModel
){
    val quests by viewModel.quests.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.loadQuests()
    }
    when{
        uiState.isLoading -> {
            CircularProgressIndicator()
        }
        uiState.errorMessage != null -> {
            Column{
                Text(uiState.errorMessage!!)
                Button(onClick = {
                    viewModel.loadQuests()
                }) {
                    Text("Retry")
                }
            }
        }
        else -> {
            LazyColumn{
                items(
                    items =  quests,
                    key = {
                        quest -> quest.id
                    }
                ){
                    quest -> Text(quest.title)
                }
            }
        }
    }
}
@Composable
@Preview(showSystemUi=true)
fun AsyncQuestDemoScreen(){
    val questViewModel: QuestViewModel = viewModel()
    AsyncQuestSection(
        viewModel=questViewModel
    )
}

