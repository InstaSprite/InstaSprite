package com.instasprite.app.ui.social.editprofile

import com.instasprite.app.utils.pixelDp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.instasprite.app.ui.components.composable.TopBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.instasprite.app.ui.components.composable.ImageCropperDialog
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.instasprite.app.R
import com.instasprite.app.ui.social.editprofile.component.AvatarSourceSheet
import com.instasprite.app.ui.social.editprofile.component.EditAvatarSection
import com.instasprite.app.ui.social.editprofile.component.EditProfileFields
import com.instasprite.app.ui.social.editprofile.contract.EditProfileEvent
import com.instasprite.app.ui.social.editprofile.contract.EditProfileState
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.graphics.asAndroidBitmap
import kotlinx.coroutines.launch
import androidx.core.graphics.scale
import com.instasprite.app.ui.components.composable.AsyncCanvasPreviewer
import com.instasprite.app.ui.components.composable.PixelIcon

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageCropper = rememberImageCropper()

    LaunchedEffect(state.savedSuccess) {
        if (state.savedSuccess) onBackClick()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    val devicePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = imageCropper.crop(uri, context)
                if (result is CropResult.Success) {
                    val destFile = File(context.cacheDir, "avatar_crop_${System.currentTimeMillis()}.png")
                    FileOutputStream(destFile).use { out ->
                        result.bitmap.asAndroidBitmap().compress(
                            android.graphics.Bitmap.CompressFormat.PNG, 100, out
                        )
                    }
                    viewModel.onAvatarPicked(Uri.fromFile(destFile))
                }
            }
        }
    }

    ImageCropperDialog(
        imageCropper = imageCropper,
        aspectLock = true
    )

    val event = remember(viewModel) {
        EditProfileEvent(
            onBackClick = onBackClick,
            onDisplayNameChange = viewModel::onDisplayNameChange,
            onBioChange = viewModel::onBioChange,
            onOpenAvatarSourceSheet = viewModel::openAvatarSourceSheet,
            onDismissAvatarSourceSheet = viewModel::dismissAvatarSourceSheet,
            onPickFromDevice = { devicePickerLauncher.launch("image/*") },
            onPickFromSprite = viewModel::openSpritePicker,
            onDismissSpritePicker = viewModel::dismissSpritePicker,
            onSpriteSelected = { spriteId ->
                viewModel.dismissSpritePicker()
                scope.launch {
                    val file = File(context.filesDir, "thumbnail_$spriteId.png")
                    if (file.exists()) {
                        val original = BitmapFactory.decodeFile(file.absolutePath)
                        val targetWidth = 256
                        val targetHeight = (original.height.toFloat() / original.width.toFloat() * targetWidth).toInt()
                        val scaled = original.scale(targetWidth, targetHeight, false)
                        val tempFile = File(context.cacheDir, "scaled_sprite_$spriteId.png")
                        FileOutputStream(tempFile).use { out ->
                            scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        
                        val result = imageCropper.crop(Uri.fromFile(tempFile), context)
                        if (tempFile.exists()) {
                            tempFile.delete()
                        }
                        
                        if (result is CropResult.Success) {
                            val destFile = File(context.cacheDir, "avatar_crop_${System.currentTimeMillis()}.png")
                            FileOutputStream(destFile).use { out ->
                                result.bitmap.asAndroidBitmap().compress(
                                    Bitmap.CompressFormat.PNG, 100, out
                                )
                            }
                            viewModel.onAvatarPicked(Uri.fromFile(destFile))
                        }
                    }
                }
            },
            onAvatarPicked = viewModel::onAvatarPicked,
            onSave = viewModel::save,
            onClearError = viewModel::clearError,
        )
    }

    EditProfileContent(state = state, event = event)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    state: EditProfileState,
    event: EditProfileEvent
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val spritePickerSheetProperties = remember {
        ModalBottomSheetProperties(
            isAppearanceLightStatusBars = false,
            isAppearanceLightNavigationBars = false,
        )
    }

    Scaffold(
        containerColor = AppTheme.colors.BackgroundColorDarker,
        topBar = {
            TopBar(
                title = stringResource(R.string.edit_profile),
                onBackClick = event.onBackClick,
                actions = {
                    Button(
                        onClick = event.onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.AccentButtonColor
                        ),
                        enabled = !state.isSaving,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(stringResource(R.string.save), color = AppTheme.colors.TextColorDark)
                    }
                }
            )
        }
    ) { innerPadding ->
        val context = LocalContext.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = AppTheme.colors.SelectedColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.pixelDp)
                ) {
                    Spacer(modifier = Modifier.height(16.pixelDp))

                    EditAvatarSection(
                        avatarUrl = state.avatarUrl,
                        pendingAvatarUri = state.pendingAvatarUri,
                        onOpenSheet = event.onOpenAvatarSourceSheet
                    )

                    Spacer(modifier = Modifier.height(22.pixelDp))

                    EditProfileFields(
                        displayName = state.displayName,
                        bio = state.bio,
                        email = state.email,
                        onDisplayNameChange = event.onDisplayNameChange,
                        onBioChange = event.onBioChange,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(22.pixelDp))
                }
            }

            if (state.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppTheme.colors.BackgroundColorDarker.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AppTheme.colors.SelectedColor,
                        modifier = Modifier.size(32.pixelDp)
                    )
                }
            }
        }

        if (state.showSpritePicker) {
            ModalBottomSheet(
                onDismissRequest = event.onDismissSpritePicker,
                properties = spritePickerSheetProperties,
                containerColor = AppTheme.colors.BackgroundColorDarker
            ) {
                val context = LocalContext.current
                if (state.userSprites.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.pixelDp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_sprite_yet),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.colors.TextColorLight
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.pixelDp)
                    ) {
                        items(state.userSprites) { spriteWithMeta ->
                            val file =
                                File(context.filesDir, "thumbnail_${spriteWithMeta.sprite.id}.png")
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(2.pixelDp)
                                    .clickable {
                                        event.onSpriteSelected(spriteWithMeta.sprite.id)
                                    }
                            ) {
                                AsyncCanvasPreviewer(
                                    sprite = spriteWithMeta.sprite,
                                    meta = spriteWithMeta.meta,
                                    onClick = { event.onSpriteSelected(spriteWithMeta.sprite.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .background(AppTheme.colors.BackgroundColor)
                                        .padding(2.pixelDp),
                                )
                                Text(
                                    text = spriteWithMeta.meta?.spriteName ?: "Untitled",
                                    color = AppTheme.colors.TextColorLight,
                                    modifier = Modifier.padding(top = 2.pixelDp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showAvatarSourceSheet) {
        AvatarSourceSheet(
            sheetState = sheetState,
            onDismiss = event.onDismissAvatarSourceSheet,
            onPickFromDevice = event.onPickFromDevice,
            onPickFromSprite = event.onPickFromSprite,
        )
    }
}

@Preview
@Composable
private fun EditProfileContentPreview() {
    InstaSpriteTheme {
        EditProfileContent(
            state = EditProfileState(
                displayName = "John Doe",
                bio = "Making pixel art one sprite at a time.",
                email = "john@example.com"
            ),
            event = EditProfileEvent()
        )
    }
}
