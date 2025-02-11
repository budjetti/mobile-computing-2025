package com.example.hw

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun OtherScreen(navController: NavController) {
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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        uri: Uri? -> uri?.let {
            val savedFile = savePfp(context, it)
            pfpPath = savedFile.absolutePath
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Button(onClick = {
            navController.navigate("Conversation")
        },
            modifier = Modifier.padding(all = 20.dp)
        ) {
            Text("Conversation")
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (null != pfpPath) {
                AsyncImage(
                    model = File(pfpPath!!),
                    contentDescription = "Depiction of User",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            } else {
                Text("Tap to select profile picture")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        val scope = rememberCoroutineScope()

        Button(
            onClick = {
                scope.launch {
                    val profile = UserProfile(id = 0, username = username, imagePath = pfpPath)
                    dao.insertProfile(profile)
                }
            }
        ) {
            Text("Save Profile Picture")
        }
    }
}

suspend fun copyUriToFile(context: Context, uri: Uri, outFile: File) {
    withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}

fun savePfp(context: Context, uri: Uri): File {
    val outFile = File(context.filesDir, "profile_pic.jpg")
    kotlinx.coroutines.runBlocking {
        copyUriToFile(context, uri, outFile)
    }
    return outFile
}