package com.instasprite.app.data.network

sealed class ApiError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    data class Network(override val cause: Throwable? = null) : ApiError(cause = cause)
    data class Unauthorized(val msg: String = "", val code: String = "") : ApiError(if (code.isNotBlank()) "$code: $msg" else msg)
    data class Forbidden(val msg: String = "", val code: String = "") : ApiError(if (code.isNotBlank()) "$code: $msg" else msg)
    data class NotFound(val msg: String = "", val code: String = "") : ApiError(if (code.isNotBlank()) "$code: $msg" else msg)
    data class BadRequest(val msg: String = "", val code: String = "") : ApiError(if (code.isNotBlank()) "$code: $msg" else msg)
    data class Conflict(val msg: String = "", val code: String = "") : ApiError(if (code.isNotBlank()) "$code: $msg" else msg)
    data class TooManyRequests(val msg: String = "", val code: String = "") : ApiError(if (code.isNotBlank()) "$code: $msg" else msg)
    data class Server(val code: String, val msg: String) : ApiError(if (code.isNotBlank()) "$code: $msg" else msg)
    data class Unknown(val msg: String = "") : ApiError(msg)
}
