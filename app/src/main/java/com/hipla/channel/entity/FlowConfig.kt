package com.hipla.channel.entity

import com.hipla.channel.common.AppConfig
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FlowConfig(val tag: String, val type: String) {

    companion object {
        fun createApplicationFlowConfig(): FlowConfig {
            return FlowConfig(tag = AppConfig.APPLICATION_TAG, type = AppConfig.APPLICATION_TAG)
        }

        fun createInventoryFlowConfig(): FlowConfig {
            return FlowConfig(tag = AppConfig.INVENTORY_TAG, type = AppConfig.INVENTORY_TYPE)
        }
    }

}

