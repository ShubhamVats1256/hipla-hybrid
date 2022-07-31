package com.hipla.channel.common.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import okio.Buffer
import org.json.JSONObject

object JSONObjectAdapter {

    @FromJson
    fun fromJson(jsonString: String): JSONObject {
        throw UnsupportedOperationException()
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: JSONObject?) {
        value?.let { writer.value(Buffer().writeUtf8(value.toString())) }
    }

}