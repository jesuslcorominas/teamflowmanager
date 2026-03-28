package com.jesuslcorominas.teamflowmanager.ui.players.wizard

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.components.form.AppTextField
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import kotlinx.coroutines.tasks.await
import java.io.File

@Composable
fun PlayerDataStep(
    initialFirstName: String,
    initialLastName: String,
    initialNumber: String,
    initialIsCaptain: Boolean,
    initialImageUri: String?,
    onDataChanged: (String, String, String, Boolean, String?) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var firstName by remember(initialFirstName) { mutableStateOf(initialFirstName) }
    var lastName by remember(initialLastName) { mutableStateOf(initialLastName) }
    var number by remember(initialNumber) { mutableStateOf(initialNumber) }
    var isCaptain by remember(initialIsCaptain) { mutableStateOf(initialIsCaptain) }
    var imageUri by remember(initialImageUri) { mutableStateOf<String?>(initialImageUri) }
    var showImageOptions by remember { mutableStateOf(false) }

    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var numberError by remember { mutableStateOf<String?>(null) }

    // Temporary URI for camera capture
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
        ) { success ->
            if (success) {
                tempCameraUri?.let {
                    imageUri = it.toString()
                }
            }
        }

    // Permission launcher for camera
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                // Permission granted, launch camera
                tempCameraUri?.let { uri ->
                    cameraLauncher.launch(uri)
                }
            }
        }

    // Gallery launcher
    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                imageUri = it.toString()
            }
            showImageOptions = false
        }

    val validateAndNext = {
        firstName = firstName.trim()
        lastName = lastName.trim()
        number = number.trim()

        firstNameError = if (firstName.isBlank()) context.getString(R.string.first_name_required) else null
        lastNameError = if (lastName.isBlank()) context.getString(R.string.last_name_required) else null
        numberError = if (number.isBlank()) context.getString(R.string.number_required) else null

        if (firstNameError == null && lastNameError == null && numberError == null) {
            onDataChanged(firstName, lastName, number, isCaptain, imageUri)
            onNext()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
    ) {
        // Player image
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showImageOptions = true },
                contentAlignment = Alignment.Center,
            ) {
                val contentDescription = "${stringResource(R.string.player_image)} $firstName $lastName"

                imageUri?.let { fullUrl ->
                    var downloadUrl by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(imageUri) {
                        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(fullUrl)
                        downloadUrl = storageRef.downloadUrl.await().toString()
                    }

                    AsyncImage(
                        model = downloadUrl,
                        placeholder = rememberVectorPainter(Icons.Default.Person),
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } ?: run {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(TFMSpacing.spacing01))

            Text(
                text = stringResource(R.string.tap_to_add_photo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

        // First Name
        AppTextField(
            modifier = Modifier.fillMaxWidth(),
            value = firstName,
            onValueChange = {
                firstName = it
                firstNameError = null
            },
            label = { Text(stringResource(R.string.first_name)) },
            isError = firstNameError != null,
            supportingText =
                if (firstNameError != null) {
                    { Text(firstNameError!!) }
                } else {
                    null
                },
            keyboardOptions =
                KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words,
                ),
            keyboardActions =
                KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
        )

        // Last Name
        AppTextField(
            modifier = Modifier.fillMaxWidth(),
            value = lastName,
            onValueChange = {
                lastName = it
                lastNameError = null
            },
            label = { Text(stringResource(R.string.last_name)) },
            isError = lastNameError != null,
            supportingText =
                if (lastNameError != null) {
                    { Text(lastNameError!!) }
                } else {
                    null
                },
            keyboardOptions =
                KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words,
                ),
            keyboardActions =
                KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
        )

        // Number
        AppTextField(
            modifier = Modifier.fillMaxWidth(),
            value = number,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    number = newValue
                    numberError = null
                }
            },
            label = { Text(stringResource(R.string.number)) },
            isError = numberError != null,
            supportingText =
                if (numberError != null) {
                    { Text(numberError!!) }
                } else {
                    null
                },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )

        // Captain checkbox
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { isCaptain = !isCaptain }
                    .padding(vertical = TFMSpacing.spacing01),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing01),
        ) {
            Checkbox(
                checked = isCaptain,
                onCheckedChange = { isCaptain = it },
            )
            Text(
                text = stringResource(R.string.is_captain),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OutlinedButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }

            Button(onClick = validateAndNext) {
                Text(stringResource(R.string.next))
            }
        }
    }

    // Image options dialog
    if (showImageOptions) {
        AppAlertDialog(
            title = stringResource(R.string.select_image_source),
            message = "",
            confirmText = stringResource(R.string.camera),
            dismissText = stringResource(R.string.gallery),
            onConfirm = {
                showImageOptions = false
                // Check camera permission
                val permission = Manifest.permission.CAMERA
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                    // Create temp file for camera
                    val photoFile = File.createTempFile("player_", ".jpg", context.cacheDir)
                    tempCameraUri =
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile,
                        )
                    cameraLauncher.launch(tempCameraUri!!)
                } else {
                    // Request permission
                    val photoFile = File.createTempFile("player_", ".jpg", context.cacheDir)
                    tempCameraUri =
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile,
                        )
                    cameraPermissionLauncher.launch(permission)
                }
            },
            onDismiss = {
                showImageOptions = false
                // Dismiss to select from gallery
                galleryLauncher.launch("image/*")
            },
        )
    }
}
