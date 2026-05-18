package com.instasprite.app.utils

import android.content.Context
import com.instasprite.app.R
import com.instasprite.app.data.network.ApiError

fun ApiError.toUserMessage(context: Context): String = when (this) {
    is ApiError.Network -> context.getString(R.string.error_no_connection)
    is ApiError.Unauthorized -> if (this.msg.isNotBlank()) this.msg else context.getString(R.string.error_session_expired)
    is ApiError.Forbidden -> context.getString(R.string.error_no_permission)
    is ApiError.NotFound -> context.getString(R.string.error_content_not_found)
    is ApiError.BadRequest -> if (this.msg.isNotBlank()) this.msg else context.getString(R.string.error_something_went_wrong)
    is ApiError.Conflict -> if (this.msg.isNotBlank()) this.msg else context.getString(R.string.error_something_went_wrong)
    is ApiError.TooManyRequests -> if (this.msg.isNotBlank()) this.msg else context.getString(R.string.error_too_many_requests)
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
