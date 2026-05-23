package com.instasprite.app.ui.social.profile.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.instasprite.app.ui.components.dialog.CustomDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.social.profile.contract.UserProfileState

@Composable
fun EditProfileDialog(
    userProfile: UserProfileState,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    LocalContext.current
    var displayName by remember { mutableStateOf(userProfile.displayName) }
    var bio by remember { mutableStateOf(userProfile.bio) }

    CustomDialog(
        title = stringResource(R.string.edit_profile),
        onDismiss = onDismiss,
        onConfirm = { onSave(displayName, bio) },
        confirmButtonText = stringResource(R.string.save),
        dismissButtonText = stringResource(R.string.cancel),
    ) {
        Column {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text(stringResource(R.string.display_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text(stringResource(R.string.bio)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }
}
