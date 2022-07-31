package com.hipla.channel.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data  class Test (val fact : String)