package com.instasprite.app.ui.social.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.instasprite.app.R
import com.instasprite.app.domain.dialog.Dialog
import com.instasprite.app.ui.components.dialog.ConfirmationDialog
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
                ConfirmationDialog(
                    title = stringResource(R.string.delete_sprite),
                    text = stringResource(R.string.are_you_sure_you_want_to_delete_this_post),
                    highlightText = "",
                    confirmButtonText = stringResource(R.string.delete),
                    dismissButtonText = stringResource(R.string.cancel),
                    hasQuestionMark = false,
                    onConfirm = {
                        viewModel.deletePost(dialog.postId)
                        viewModel.closeTopDialog()
                    },
                    onDismiss = viewModel::closeTopDialog
                )
        }
    }
}