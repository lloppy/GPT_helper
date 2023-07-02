package com.example.gpt.api.model

import com.google.gson.annotations.SerializedName

data class Choice(
    @SerializedName("message")
    val message: Message,
    @SerializedName("finish_reason")
    val finishReason: String,
    val index: Int
)