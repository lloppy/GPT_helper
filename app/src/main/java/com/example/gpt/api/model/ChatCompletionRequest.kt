package com.example.gpt.api.model

data class ChatCompletionRequest (
    val model: String,
    val messages: List<Message>
)