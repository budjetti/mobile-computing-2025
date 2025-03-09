package com.example.hw

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

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

    val activity = context as? Activity
    var enableNotificationsButtonText by remember { mutableStateOf("Enable Notifications") }

    LaunchedEffect(activity?.intent) {
        if (activity?.intent?.getBooleanExtra("notificationTapped", false) == true) {
            enableNotificationsButtonText = "NOTIFICATION OPENED"
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                sendNotification(context)
            }
        }
    )

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
            Text("Save Username And Picture")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    sendNotification(context)
                }
            }
        ) {
            Text(enableNotificationsButtonText)
        }

        Spacer(modifier = Modifier.height(64.dp))

        YoutubePlayer(
            youtubeVideoId = "um0ETkJABmI",
            lifecycleOwner = LocalLifecycleOwner.current
        )
    }
}

const val CHANNEL_ID = "default_channel"

fun sendNotification(context: Context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is not in the Support Library.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Default Notification Channel"
        val descriptionText = "description"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Create an explicit intent for an Activity in your app.
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("notificationTapped", true)
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.cat)
        .setContentTitle("Enticing notification")
        .setContentText("Click me! Click me!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        // Set the intent that fires when the user taps the notification.
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return@with
        }
        // notificationId is a unique int for each notification that you must define.
        // hopefully 0 works
        notify(0, builder.build())
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


// https://www.youtube.com/watch?v=E_8LHkn4g-Q&t=44s&ab_channel=AhmedGuedmioui

@Composable
fun YoutubePlayer(
    youtubeVideoId: String,
    lifecycleOwner: LifecycleOwner
) {

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp)),
        factory = { context ->
            YouTubePlayerView(context = context).apply {
                lifecycleOwner.lifecycle.addObserver(this)

                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(youtubeVideoId, 0f)
                    }
                })
            }
        })

}