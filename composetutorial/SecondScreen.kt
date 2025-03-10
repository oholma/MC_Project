package com.example.composetutorial

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import kotlinx.coroutines.Dispatchers

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import androidx.navigation.*
import androidx.navigation.compose.*
import androidx.lifecycle.*
import androidx.room.*
import kotlinx.coroutines.*
import android.content.*
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi

import androidx.compose.runtime.livedata.observeAsState
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat


import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SecondScreen(navController: NavController, viewModel: UserViewModel) {

    val user by viewModel.user.observeAsState()

    var username by remember { mutableStateOf(user?.username ?: "") }
    var selectedImageUri by remember { mutableStateOf(user?.selectedImageUri ?: "") }

    val context = LocalContext.current
    val openDocumentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        uri: Uri? -> uri?.let {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                val contentResolver = context.contentResolver
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                selectedImageUri = uri.toString()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error granting persistable permission")
            }
        }
    }

    var hasNotificationPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                        == PackageManager.PERMISSION_GRANTED
            )
        } else mutableStateOf(true)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Button(
            onClick = { navController.popBackStack() }
        ) {
            Text("Conversation")
        }

        Text("User:")

        AsyncImage(
            model = selectedImageUri,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(1.6.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                .clickable {
                    openDocumentLauncher.launch(arrayOf("image/*"))
                }
            )

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Enter name..") }
        )
        
        Button(onClick = {
            if (username.isNotEmpty() && selectedImageUri.isNotEmpty()) {
                viewModel.saveUser(username, selectedImageUri)
            }
        }) {
            Text("Save")
        }

        val notificationLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                hasNotificationPermission = isGranted
            }
        )
        Button(onClick = {
            notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            showNotification(context)
        }
        ) {
            Text("Enable notifications")
        }
    }
}


class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val db: AppDatabase by lazy {
        Room.databaseBuilder(application, AppDatabase::class.java, "user_profile_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val user = MutableLiveData<User?>()

    init {
        loadUser()
    }

    fun saveUser(username: String, selectedImageUri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val newUser = User(username = username, selectedImageUri = selectedImageUri ?: "")
            db.userDao().insertOrUpdateUser(newUser)
            user.postValue(newUser)
        }
    }

    private fun loadUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val loadedUser = db.userDao().getLastUser()
            user.postValue(loadedUser)
        }
    }
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            "Channel",
            "Channel name",
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

private fun showNotification(context: Context) {
    createNotificationChannel(context)

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context,"Channel")
        .setContentTitle("New notification")
        .setContentText("Notifications enabled")
        .setSmallIcon(R.drawable.notification_icon)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as
            NotificationManager
    notificationManager.notify(1, notification)
}
