package com.olaz.instasprite.data.network

sealed class ApiError : Exception() {
    data class Network(override val cause: Throwable? = null) : ApiError()
    data class Unauthorized(val msg: String = "") : ApiError()
    data class Forbidden(val msg: String = "") : ApiError()
    data class NotFound(val msg: String = "") : ApiError()
    data class Server(val code: String, val msg: String) : ApiError()
    data class Unknown(val msg: String = "") : ApiError()
}
