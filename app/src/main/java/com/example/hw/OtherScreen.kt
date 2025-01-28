package com.example.hw

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun OtherScreen(navController: NavController){
    Button(onClick = {
        navController.navigate("Conversation")
    } ) {
        Text("Conversation")
    }
}