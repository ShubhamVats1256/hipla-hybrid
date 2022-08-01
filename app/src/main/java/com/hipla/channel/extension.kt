package com.hipla.channel

import android.content.Context
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import com.hipla.channel.api.ApiErrorMessage
import com.hipla.channel.api.ApplicationRequest
import com.hipla.channel.common.ApiErrorCode
import com.hipla.channel.common.CLIENT_ERROR_NO_INTERNET
import com.hipla.channel.common.CLIENT_GENERIC
import com.hipla.channel.common.Utils.tryCatch
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.Qualifier
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun Context.getDeviceMetrics(): DisplayMetrics {
    val metrics = DisplayMetrics()
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display: Display = wm.defaultDisplay
    display.getMetrics(metrics)
    return metrics
}

fun Context.screenWidthDp(): Int {
    return resources.configuration.screenWidthDp
}

fun Context.screenHeightInDp(): Int {
    return resources.configuration.screenHeightDp
}


internal fun Interceptor.Chain.safeProceed(
    request: Request
): Response {
    return try {
        this.proceed(request)
        // IOException unknown host exception , socket connection , socket timeout  etc
    } catch (ex: IOException) {
        request.toErrorResponse(
            CLIENT_ERROR_NO_INTERNET
        )
    } catch (ex: Exception) {
        request.toErrorResponse(
            CLIENT_GENERIC
        )
    }
}


internal fun Request.toErrorResponse(
    @ApiErrorCode code: String,
): Response {
    val errorBody =
        moshiAdapter<ApiErrorMessage>().toJson(ApiErrorMessage("unable to connect", code))
    // 400 is a generic client side error
    return Response.Builder()
        .code(400)
        .message("client error")
        .request(this)
        .protocol(Protocol.HTTP_1_1)
        .body(errorBody.toResponseBody("application/json".toMediaType()))
        .build()
}

val moshi = getKoinInstance<Moshi>()


inline fun <reified T> moshiAdapter(): JsonAdapter<T> {
    return moshi.adapter(T::class.java)
}

inline fun <reified T> getKoinInstance(qualifier: Qualifier? = null): T {
    return object : KoinComponent {
        val value: T by inject(qualifier = qualifier)
    }.value
}


fun CoroutineScope.launchSafely(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend () -> Unit
) = launch(context, start) {
    tryCatch {
        block.invoke()
    }
}

fun ApplicationRequest.toRequestMap(): Map<String, String> {
    val requestMap = mutableMapOf<String, String>()
    requestMap["tag"] = tag
    requestMap["type"] = type
    requestMap["identifier"] = identifier
    requestMap["customerName"] = customerName
    requestMap["customerPhoneNumber"] = customerPhoneNumber
    requestMap["panNumber"] = panNumber
    requestMap["channelPartnerId"] = channelPartnerId
    requestMap["paymentTypeById"] = paymentType.typeId.toString()
    requestMap["paymentDetails"] = paymentDetails
    requestMap["paymentProofImageUrl"] = paymentProofImageUrl
    requestMap["ownerId"] = ownerId.toString()
    requestMap["createdBy"] = createdBy.toString()
    if (unitId >= 0) {
        requestMap["unitId"] = unitId.toString()
    }
    return requestMap
}