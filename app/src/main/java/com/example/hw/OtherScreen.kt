package com.example.hw

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun OtherScreen(navController: NavController){
    Button(onClick = {
        navController.navigate("Conversation")
    },
        modifier = Modifier.padding(all = 20.dp)
    ) {
        Text("Conversation")
    }
}