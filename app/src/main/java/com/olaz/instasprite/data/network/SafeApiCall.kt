package com.olaz.instasprite.data.network

import com.olaz.instasprite.data.network.model.ResultResponse
import com.olaz.instasprite.di.RetrofitModule
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> safeApiCall(
    block: suspend () -> Result<T>
): Result<T> {
    return try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: UnknownHostException) {
        Result.failure(ApiError.Network(e))
    } catch (e: ConnectException) {
        Result.failure(ApiError.Network(e))
    } catch (e: SocketTimeoutException) {
        Result.failure(ApiError.Network(e))
    } catch (e: SSLHandshakeException) {
        Result.failure(ApiError.Network(e))
    } catch (e: ApiError) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(ApiError.Unknown(e.message ?: ""))
    }
}

fun <T> Response<ResultResponse<T>>.toResult(): Result<T> {
    val body = getBodyOrError(RetrofitModule.gson)
    if (body != null && body.status == 200 && body.data != null) {
        return Result.success(body.data)
    }
    return Result.failure(mapHttpError(code(), body?.code, body?.message))
}

fun <T> Response<ResultResponse<T>>.toResultUnit(): Result<Unit> {
    val body = getBodyOrError(RetrofitModule.gson)
    if (body != null && body.status == 200) {
        return Result.success(Unit)
    }
    return Result.failure(mapHttpError(code(), body?.code, body?.message))
}

fun <T> Response<ResultResponse<T>>.toResultMessage(default: String = ""): Result<String> {
    val body = getBodyOrError(RetrofitModule.gson)
    if (body != null && body.status == 200) {
        return Result.success(body.message.ifBlank { default })
    }
    return Result.failure(mapHttpError(code(), body?.code, body?.message))
}

private fun mapHttpError(httpCode: Int, errorCode: String?, errorMessage: String?): ApiError {
    return when (httpCode) {
        401 -> ApiError.Unauthorized(errorMessage ?: "")
        403 -> ApiError.Forbidden(errorMessage ?: "")
        404 -> ApiError.NotFound(errorMessage ?: "")
        in 500..599 -> ApiError.Server(errorCode ?: httpCode.toString(), errorMessage ?: "")
        else -> ApiError.Server(errorCode ?: httpCode.toString(), errorMessage ?: "")
    }
}
