package com.hipla.channel.extension

import com.hipla.channel.common.Constant
import com.hipla.channel.entity.api.*
import com.squareup.moshi.Moshi
import org.json.JSONObject
import retrofit2.Response
import timber.log.Timber

fun <T> Response<T>.asResource(): Resource<T> {
    return try {
        if (this.isSuccessful) {
            val body = this.body() ?: Unit as T
            ResourceSuccess(body)
        } else {
            val jsonAdapter = getKoinInstance<Moshi>().adapter(ApiError::class.java)
            ResourceError(
                throwable = jsonAdapter.fromJson(this.errorBody()!!.string())
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Timber.tag(Constant.API_ERROR).e(e.toString())
        ResourceError(e, this.code())
    }
}

fun Map<String, Any>.toJSONObject(): JSONObject {
    val o = JSONObject()
    for ((k, v) in this) {
        o.put(k, v)
    }
    return o
}

suspend fun <T> perform(
    id: String,
    call: suspend (String) -> Response<T>
) = try {
    call.invoke(id).asResource()
} catch (e: Exception) {
    Resource.error<T>(e)
}

suspend fun <K, T> perform(
    o: K,
    call: suspend (K) -> Response<T>
) = try {
    call.invoke(o).asResource()
} catch (e: Exception) {
    Resource.error<T>(e)
}

suspend fun <T> perform(
    id: String,
    values: Map<String, Any>,
    call: suspend (String, JSONObject) -> Response<T>
) = try {
    call.invoke(id, values.toJSONObject()).asResource()
} catch (e: Exception) {
    Resource.error<T>(e)
}

suspend fun <T> perform(
    call: suspend () -> Response<T>
) = try {
    call.invoke().asResource()
} catch (e: Exception) {
    Resource.error<T>(e)
}

suspend fun <T> performWithQueryParams(
    queryParams: Map<String, String>,
    call: suspend (Map<String, String>) -> Response<T>
) = try {
    call.invoke(queryParams).asResource()
} catch (e: Exception) {
    Resource.error<T>(e)
}


