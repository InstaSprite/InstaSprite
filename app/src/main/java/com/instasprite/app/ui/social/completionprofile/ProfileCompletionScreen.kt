package com.instasprite.app.ui.social.completionprofile

import com.instasprite.app.utils.pixelDp

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.instasprite.app.ui.components.composable.TopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instasprite.app.R
import com.instasprite.app.domain.model.InputField
import com.instasprite.app.ui.components.composable.InputTextField
import com.instasprite.app.ui.social.PostInteractionEvent
import com.instasprite.app.ui.social.completionprofile.contract.ProfileCompletionScreenEvent
import com.instasprite.app.ui.social.completionprofile.contract.ProfileCompletionState
import com.instasprite.app.ui.social.feed.component.AvatarSelectionComponent
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.UiUtils
import kotlinx.coroutines.delay

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun ProfileCompletionScreen(
    onProfileCompleted: () -> Unit,
    viewModel: ProfileCompletionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadProfileData()
    }

    LaunchedEffect(uiState.isProfileUpdated) {
        if (uiState.isProfileUpdated) {
            Toast.makeText(
                context,
                context.getString(R.string.profile_updated_successfully),
                Toast.LENGTH_SHORT
            ).show()

            delay(1000)
            PostInteractionEvent.emitProfileRefreshEvent()
            onProfileCompleted()
        }
    }

    ProfileCompletionContent(
        state = uiState,
        event = ProfileCompletionScreenEvent(
            onUpdateClick = { username, name, introduce, email ->
                viewModel.updateProfile(username, name, introduce, email)
            },
            onErrorChanged = { _ ->
                viewModel.clearError()
            },
            onImageSelected = { uri ->
                viewModel.selectImage(uri)
            },
            onUploadImage = {
                viewModel.uploadImage(context)
            },
            onClearImageError = {
                viewModel.clearImageError()
            },
            onProfileCompleted = onProfileCompleted
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCompletionContent(
    state: ProfileCompletionState,
    event: ProfileCompletionScreenEvent
) {
    LocalContext.current

    UiUtils.SetStatusBarColor(AppTheme.colors.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(AppTheme.colors.BackgroundColorDarker)

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.complete_your_profile)
            )
        },
        containerColor = AppTheme.colors.BackgroundColorDarker
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 10.pixelDp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (state.isLoading && state.profileData == null) {
                CircularProgressIndicator(
                    color = AppTheme.colors.BottomBarColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                ProfileCompletionForm(
                    state = state,
                    event = event
                )
            }
        }
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun ProfileCompletionForm(
    state: ProfileCompletionState,
    event: ProfileCompletionScreenEvent
) {
    var username by remember { mutableStateOf(state.profileData?.memberUsername ?: "") }
    var name by remember { mutableStateOf(state.profileData?.memberName ?: "") }
    var introduce by remember { mutableStateOf(state.profileData?.memberIntroduce ?: "") }
    var email by remember { mutableStateOf(state.profileData?.memberEmail ?: "") }

    val context = LocalContext.current

    LaunchedEffect(state.profileData) {
        state.profileData?.let {
            username = it.memberUsername
            name = it.memberName
            introduce = it.memberIntroduce ?: ""
            email = it.memberEmail
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.lets_set_up_your_profile),
            fontSize = 16.sp,
            color = AppTheme.colors.Foreground2Color,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(10.pixelDp))

        AvatarSelectionComponent(
            selectedImageUri = state.selectedImageUri,
            onImageSelected = { uri ->
                event.onImageSelected(uri)
                event.onErrorChanged("")
                event.onClearImageError()
            },
            modifier = Modifier.padding(horizontal = 10.pixelDp)
        )

        if (state.imageUploadError != null) {
            Text(
                text = state.imageUploadError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.pixelDp, start = 10.pixelDp)
            )
        }

        Spacer(modifier = Modifier.height(16.pixelDp))

        InputTextField(
            value = username,
            onValueChange = {
                username = it
                event.onErrorChanged("")
            },
            inputField = InputField(label = stringResource(R.string.username), keyboardType = KeyboardType.Text),
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(10.pixelDp))

        InputTextField(
            value = name,
            onValueChange = {
                name = it
                event.onErrorChanged("")
            },
            inputField = InputField(label = stringResource(R.string.full_name), keyboardType = KeyboardType.Text),
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(10.pixelDp))

        InputTextField(
            value = email,
            onValueChange = {
                email = it
                event.onErrorChanged("")
            },
            inputField = InputField(label = stringResource(R.string.email), keyboardType = KeyboardType.Email),
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(10.pixelDp))

        InputTextField(
            value = introduce,
            onValueChange = {
                introduce = it
                event.onErrorChanged("")
            },
            inputField = InputField(label = stringResource(R.string.introduction_optional), keyboardType = KeyboardType.Text),
            imeAction = ImeAction.Done,
            maxLines = 3
        )

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.pixelDp, start = 10.pixelDp)
            )
        }

        Spacer(modifier = Modifier.height(22.pixelDp))

        Button(
            onClick = {
                when {
                    username.isBlank() -> event.onErrorChanged(
                        context.getString(R.string.please_enter_username)
                    )

                    username.length !in 4..20 -> event.onErrorChanged(
                        context.getString(R.string.username_length_error)
                    )

                    name.isBlank() -> event.onErrorChanged(
                        context.getString(R.string.please_enter_full_name)
                    )

                    email.isBlank() -> event.onErrorChanged(
                        context.getString(R.string.please_enter_email)
                    )

                    else -> {
                        if (state.selectedImageUri != null) {
                            event.onUploadImage()
                        }
                        event.onUpdateClick(username, name, introduce.ifBlank { null }, email)
                    }
                }
            },
            enabled = !state.isLoading && !state.isUploadingImage,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.BottomBarColor,
                contentColor = AppTheme.colors.TextColorLight
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(38.pixelDp)
        ) {
            if (state.isLoading || state.isUploadingImage) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.pixelDp),
                    color = AppTheme.colors.TextColorLight,
                    strokeWidth = 2.pixelDp
                )
            } else {
                Text(
                    text = if (state.isUploadingImage) stringResource(R.string.uploading_image) else stringResource(
                        R.string.complete_profile
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(22.pixelDp))
    }
}

@Preview
@Composable
fun ProfileCompletionPreview() {
    InstaSpriteTheme {
        ProfileCompletionContent(
            state = ProfileCompletionState(
                profileData = null,
                isLoading = false
            ),
            event = ProfileCompletionScreenEvent()
        )
    }
}
