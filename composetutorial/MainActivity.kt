package com.example.composetutorial

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.composetutorial.ui.theme.ComposeTutorialTheme
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeTutorialTheme {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Navigation()
                }
            }
        }
    }
}


data class Message(val author: String, val body: String, val profileImageUri: String? = null)

@Composable
fun MessageCard(msg: Message, userViewModel: UserViewModel) {

    val user by userViewModel.user.observeAsState()

    Row(verticalAlignment = Alignment.CenterVertically) {
        
        val selectedImageUri = msg.profileImageUri ?: user?.selectedImageUri
        if (selectedImageUri?.isNotEmpty() == true) {
            Image(
                painter = painterResource(id = getDrawableResourceId(selectedImageUri)),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.6.dp, MaterialTheme.colorScheme.secondary, CircleShape)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.profile_picture),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.6.dp, MaterialTheme.colorScheme.secondary, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            label = "",
            )

        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            val username = user?.username
            if (username?.isNotEmpty() == true) {
                Text(
                    text = username,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall
                )
            } else {
                Text(
                    text = msg.author,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier
                    .animateContentSize()
                    .padding(1.dp)
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun getDrawableResourceId(imageName: String): Int {
    return when (imageName) {
        "second_profilepicture" -> R.drawable.second_profilepicture
        else -> R.drawable.profile_picture
    }
}

@Composable
fun Conversation(
    messages1: List<Message>,
    messages2: List<Message>,
    navController: NavHostController,
    viewModel: UserViewModel) {

    val mergedMessages = mergeMessages(messages1, messages2)

    Column {
        Button(
            onClick = { navController.navigate("second") }
        ) {
            Text("Profile")
        }

        LazyColumn {
            items(mergedMessages) { message ->
                MessageCard(message, viewModel)
            }
        }
    }
}


fun mergeMessages(list1: List<Message>, list2: List<Message>): List<Message> {
    val mergedList = mutableListOf<Message>()
    val maxSize = maxOf(list1.size, list2.size)

    for (i in 0 until maxSize) {
        if (i < list1.size) mergedList.add(list1[i])
        if (i < list2.size) mergedList.add(list2[i])
    }
    return mergedList
}
