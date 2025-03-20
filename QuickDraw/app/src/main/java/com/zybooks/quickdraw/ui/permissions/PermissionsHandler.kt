package com.zybooks.quickdraw.ui.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * handles requesting mic perms for speech rec
 */
@Composable
fun MicrophonePermissionHandler(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }

    // do we already have permission?
    val hasMicPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    // if so, call the callback
    if (hasMicPermission) {
        LaunchedEffect(key1 = Unit) {
            onPermissionGranted()
        }
        return
    }

    // create permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                showRationale = true
            }
        }
    )

    // request the permission when component is first composed
    LaunchedEffect(key1 = Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // show rationale dialog if needed
    if (showRationale) {
        RationaleDialog(
            onRequestPermission = {
                showRationale = false
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onDismiss = { showRationale = false },
            context = context
        )
    }
}

@Composable
private fun RationaleDialog(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    context: Context
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Microphone Permission Required") },
        text = { Text("To play this game, we need access to your microphone to recognize spoken words. Without this permission, you won't be able to play the game using voice.") },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text("Request Permission")
            }
        },
        dismissButton = {
            Button(onClick = {
                // open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
                onDismiss()
            }) {
                Text("Open Settings")
            }
        }
    )
}