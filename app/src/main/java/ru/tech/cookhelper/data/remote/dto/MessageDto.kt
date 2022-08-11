package ru.tech.cookhelper.data.remote.dto

import com.squareup.moshi.Json
import ru.tech.cookhelper.domain.model.Message

data class MessageDto(
    val id: Long,
    @field:Json(name = "time_stamp")
    val timestamp: Long,
    val text: String,
    @field:Json(name = "attachment_id")
    val attachmentId: String?,
    @field:Json(name = "user_id")
    val userId: Long
)

fun MessageDto.toMessage() = Message(id, timestamp, text, attachmentId, userId)