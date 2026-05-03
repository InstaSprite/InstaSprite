package com.olaz.instasprite.utils

import android.content.Context
import com.olaz.instasprite.R
import com.olaz.instasprite.data.network.ApiError

fun ApiError.toUserMessage(context: Context): String = when (this) {
    is ApiError.Network -> context.getString(R.string.error_no_connection)
    is ApiError.Unauthorized -> context.getString(R.string.error_session_expired)
    is ApiError.Forbidden -> context.getString(R.string.error_no_permission)
    is ApiError.NotFound -> context.getString(R.string.error_content_not_found)
    is ApiError.Server -> context.getString(R.string.error_server_problem)
    is ApiError.Unknown -> context.getString(R.string.error_something_went_wrong)
}

fun Throwable.toUserMessage(context: Context): String {
    return if (this is ApiError) {
        toUserMessage(context)
    } else {
        context.getString(R.string.error_something_went_wrong)
    }
}
