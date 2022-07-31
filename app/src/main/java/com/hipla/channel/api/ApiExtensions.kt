package com.hipla.channel.api

import com.hipla.channel.getKoinInstance
import com.squareup.moshi.Moshi
import org.json.JSONObject
import retrofit2.Response

fun <T> Response<T>.asResource(): Resource<T> {
    return try {
        if (this.isSuccessful) {
            val body = this.body() ?: Unit as T
            ResourceSuccess(body)
        } else {
            val jsonAdapter = getKoinInstance<Moshi>().adapter(ApiErrorMessage::class.java)
            ResourceError(
                throwable = ApiException(
                    this.code(),
                    jsonAdapter.fromJson(this.errorBody()!!.string())
                ), code = this.code()
            )
        }
    } catch (e: Exception) {
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

