package com.hipla.channel.extension

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.navigation.NavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hipla.channel.common.*
import com.hipla.channel.common.Utils.tryCatch
import com.hipla.channel.contract.ILoader
import com.hipla.channel.entity.AppEvent
import com.hipla.channel.entity.AppEventWithData
import com.hipla.channel.entity.ApplicationRequest
import com.hipla.channel.entity.api.ApiErrorMessage
import com.hipla.channel.entity.api.Status
import com.hipla.channel.entity.response.RecordStatus
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
import timber.log.Timber
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

fun ApplicationRequest.toCreateApplicationRequestMap(): Map<String, String> {
    val requestMap = mutableMapOf<String, String>()
    requestMap["tag"] = tag
    requestMap["type"] = type
    return requestMap
}

fun ApplicationRequest.toUpdateApplicationRequestMap(): Map<String, String> {
    val requestMap = mutableMapOf<String, String>()
    requestMap["tag"] = tag
    requestMap["type"] = type
    requestMap["identifier"] = identifier ?: Constant.EMPTY_STRING
    requestMap["customerName"] = customerName!!
    requestMap["customerLastName"] = customerLastName!!
    requestMap["customerPhoneNumber"] = customerPhoneNumber!!
    requestMap["panNumber"] = panNumber!!
    requestMap["channelPartnerId"] = channelPartnerId!!
    requestMap["paymentTypeById"] = paymentType.typeId.toString()
    // requestMap["paymentDetails"] = paymentDetails ?: Constant.EMPTY_STRING
    requestMap["paymentProofImageUrl"] = this.paymentProofImageUrl!!
    requestMap["amountPayable"] = this.amountPayable!!
    requestMap["ownerId"] = ownerId.toString()
    requestMap["createdBy"] = createdBy.toString()
    if (unitId >= 0) {
        requestMap["unitId"] = unitId.toString()
    }
    if (floorPreferenceId > 0) {
        requestMap["floorId"] = floorPreferenceId.toString()
    }
    return requestMap
}

fun RecyclerView.canLoadNextGridPage(newScrollState: Int): Boolean {
    if (newScrollState != RecyclerView.SCROLL_STATE_IDLE) {
        return false
    }
    val layoutManager = layoutManager
    return if (layoutManager is GridLayoutManager) {
        layoutManager.findLastVisibleItemPosition() >= layoutManager.itemCount - ((AppConfig.PAGE_DOWNLOAD_SIZE) / 2)
    } else {
        false
    }
}

fun EditText.hasValidPhone(): Boolean {
    return this.text.toString().length == 10
}

fun EditText.hasValidData(): Boolean {
    return this.text.trim().isEmpty().not()
}

fun EditText.content(): String {
    return this.text.trim().toString()
}

fun EditText.onSubmit(block: () -> Unit) {
    setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            block.invoke()
            return@OnEditorActionListener true
        }
        false
    })
}

fun NavController?.isCurrentDestination(destinationId: Int): Boolean {
    this ?: return false
    return currentDestination?.id == destinationId
}

fun Activity.toILoader() = this as ILoader

fun Context.showToastShortDuration(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showToastLongDuration(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun String.toApplicationRequest(): ApplicationRequest? {
    return try {
        val applicationReqJsonAdapter =
            getKoinInstance<Moshi>().adapter(ApplicationRequest::class.java)
        applicationReqJsonAdapter.fromJson(this)
    } catch (ex: Exception) {
        Timber.tag(LogConstant.APP_EXCEPTION).e(ex)
        null
    }
}

fun ApplicationRequest.toJsonString(): String? {
    return try {
        val applicationReqJsonAdapter =
            getKoinInstance<Moshi>().adapter(ApplicationRequest::class.java)
        applicationReqJsonAdapter.toJson(this)
    } catch (ex: Exception) {
        Timber.tag(LogConstant.APP_EXCEPTION).e(ex)
        null
    }
}


fun AppEvent.toApplicationRequest(): ApplicationRequest? {
    (this as? AppEventWithData<*>)?.let { appRequestEventData ->
        appRequestEventData.extras?.let { appRequest ->
            return appRequest as? ApplicationRequest
        } ?: Timber.tag(LogConstant.APP_CONFIRM).e("application request casting failed")
    }
    return null
}


fun AppEvent.toSalesUserId(): String? {
    val appEventData: AppEventWithData<*>? = this as? AppEventWithData<*>
    return ((appEventData?.extras) as? String)
}

fun RecordStatus.isSuccess(): Boolean {
    return this.statusCode == SUCCESS;
}
