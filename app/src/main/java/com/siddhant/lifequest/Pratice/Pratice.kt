package com.siddhant.lifequest.Pratice

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.siddhant.lifequest.QuestState

// Create a small counter and observe the UI update as you tap the Button
@Preview(showSystemUi = true)

@Composable
fun ParentScreen(){
    var completed by rememberSaveable{
        mutableStateOf(false)

    }
    QuestStatus(
        completed = completed,
        onComplete = {completed = true}
    )
}
@Composable
fun QuestStatus(
    completed: Boolean,
    onComplete: () -> Unit
){
    Button(
        onClick = onComplete,
        enabled = !completed
    ){
        Text(if(completed) "Completed" else "Complete Quest")
    }
}
