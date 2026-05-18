package com.instasprite.app.ui.social.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.instasprite.app.domain.dialog.Dialog
import com.instasprite.app.ui.social.feed.dialog.DeletePostConfirmDialog
import com.instasprite.app.ui.social.feed.dialog.PostFilterDialog
import com.instasprite.app.ui.social.feed.dialog.VerifyEmailDialog

sealed interface FeedDialog : Dialog {
    data object PostFilter : FeedDialog
    data object VerifyEmail : FeedDialog
    data class DeletePostConfirm(val postId: Long) : FeedDialog
}

@Composable
fun FeedScreenDialogs(
    dialogState: List<FeedDialog>,
    viewModel: FeedViewModel
) {
    val context = LocalContext.current
    val contentState = viewModel.contentState.collectAsState().value

    dialogState.forEach { dialog ->
        when (dialog) {
            is FeedDialog.PostFilter ->
                PostFilterDialog(
                    onDismiss = viewModel::closeTopDialog,
                    onFilterSelected = { filter ->
                        viewModel.setPostFilter(filter)
                        viewModel.closeTopDialog()
                    },
                    currentFilter = contentState.uiState.postFilter
                )

            is FeedDialog.VerifyEmail ->
                VerifyEmailDialog(
                    verifyEmailState = contentState.verifyEmailState,
                    onDismiss = {
                        if (!contentState.verifyEmailState.isSending) viewModel.closeTopDialog()
                    },
                    onConfirm = {
                        if (!contentState.verifyEmailState.isSending) viewModel.verifyEmail(context)
                    }
                )

            is FeedDialog.DeletePostConfirm ->
                DeletePostConfirmDialog(
                    onConfirm = {
                        viewModel.deletePost(dialog.postId)
                        viewModel.closeTopDialog()
                    },
                    onDismiss = viewModel::closeTopDialog
                )
        }
    }
}