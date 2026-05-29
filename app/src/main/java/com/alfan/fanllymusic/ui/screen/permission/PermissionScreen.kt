package com.alfan.fanllymusic.ui.screen.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfan.fanllymusic.ui.theme.AccentPink
import com.alfan.fanllymusic.ui.theme.Black
import com.alfan.fanllymusic.ui.theme.DarkSurface
import com.alfan.fanllymusic.ui.theme.DarkSurfaceVariant
import com.alfan.fanllymusic.ui.theme.TextSecondary
import com.alfan.fanllymusic.ui.theme.White
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(onGranted: () -> Unit) {
    val context = LocalContext.current
    val perms = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        perms.add(Manifest.permission.READ_MEDIA_AUDIO)
        perms.add(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permissionState = rememberMultiplePermissionsState(perms)

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            onGranted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = AccentPink,
                modifier = Modifier
                    .size(110.dp)
                    .background(DarkSurfaceVariant, CircleShape)
                    .padding(28.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Find Your Local Music",
                color = White,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Allow FanllyMusic to scan your device and find MP3, FLAC, WAV, and other audio files.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.height(44.dp))

            if (!permissionState.allPermissionsGranted && permissionState.shouldShowRationale) {
                // Warning state if user denied before
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentPink.copy(alpha = 0.1f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = AccentPink,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Access is required to play offline audio. Please allow in app settings if denied.",
                        color = AccentPink,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = { permissionState.launchMultiplePermissionRequest() },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPink, contentColor = White),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp)
            ) {
                Text("Allow Music Access", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp)
            ) {
                Text("Open Settings", color = White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = "Your files stay on this device.",
                color = TextSecondary.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
