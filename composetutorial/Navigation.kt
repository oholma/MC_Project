package com.example.composetutorial

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.composetutorial.ui.theme.MessageData

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val viewModel: UserViewModel = viewModel()
    val messages1 = SampleData.conversationSample
    val messages2 = MessageData.messageSample

    NavHost(
        navController,
        startDestination = "first"
    ) {
        composable("first") { Conversation(messages1, messages2, navController, viewModel) }
        composable("second") { SecondScreen(navController, viewModel)}
    }
}
