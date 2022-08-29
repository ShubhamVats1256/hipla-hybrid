package com.hipla.sentinelvms.sentinelKt.foodModule.network

import com.google.gson.annotations.SerializedName

class QuickSettingResponse(
    @SerializedName("status") var status: String,
    @SerializedName("message") var message: String,
    @SerializedName("data") var data: QuickSettingData
) {
}


data class QuickSettingData(
    @SerializedName("mayIComeIn") var mayIComeIn: MayIComeIn? = MayIComeIn(),
    @SerializedName("pantry") var pantry: Pantry? = Pantry()
) {
}

data class Pantry(
    @SerializedName("hideStock") var hideStock: Boolean? = null
) {

}

data class MayIComeIn(

    @SerializedName("autoClose") var autoClose: Boolean? = null,
    @SerializedName("dialogExitTime") var dialogExitTime: Int? = null,
    @SerializedName("meeting") var meeting: Meeting? = Meeting()

)

data class Meeting(

    @SerializedName("endMeeting") var endMeeting: EndMeeting? = EndMeeting()

)

data class EndMeeting(

    @SerializedName("startWithHiplaTone") var startWithHiplaTone: Boolean? = null,
    @SerializedName("endMeetingVoiceAlert") var endMeetingVoiceAlert: String? = null,
    @SerializedName("endWithHiplaTone") var endWithHiplaTone: Boolean? = null

)
