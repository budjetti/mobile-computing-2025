package com.example.hw

import SampleData
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hw.ui.theme.HWTheme
import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.room.Room
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun Conversation(navController: NavController) {
    HWTheme {
        Column {
            Button(onClick = {
                navController.navigate("OtherScreen") {
                    popUpTo("OtherScreen") { inclusive = true }
                }
            },
            modifier = Modifier.padding(all = 20.dp)
            ) {
                Text("Other Screen")
            }
            ConversationList(SampleData.conversationSample)
        }
    }
}

data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message) {

    val context = LocalContext.current

    val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "database-name"
    ).build()
    val dao = db.userProfileDao()

    var username by remember { mutableStateOf("") }
    var pfpPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            dao.getProfile()?.let { profile ->
                username = profile.username
                pfpPath = profile.imagePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Row (modifier = Modifier.padding(all = 8.dp)){
        if (pfpPath != null) {
            // Use Coil's AsyncImage to load the image from the stored file.
            AsyncImage(
                model = File(pfpPath!!),
                contentDescription = "Depiction of user",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.cat),
                contentDescription = "Depiction of user",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        )

        if (username == ""){
            username = "default username"
        }

        Column (
            modifier = Modifier.clickable { isExpanded = !isExpanded }
        ){
            Text(
                //text = msg.author,
                text = username,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(
                modifier = Modifier.height(4.dp),
            )
            Surface (
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier.animateContentSize().padding(1.dp)
            ){
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

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)

@Preview
@Composable
fun PreviewMessageCard() {
    HWTheme {
        Surface {
            MessageCard(
                msg = Message("John Meow", "Feed me!")
            )
        }
    }
}

@Composable
fun ConversationList(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message)
        }
    }
}

@Preview
@Composable
fun PreviewConversation() {
    HWTheme {
        ConversationList(SampleData.conversationSample)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HWTheme {
        Greeting("Android")
    }
}