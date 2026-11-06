package com.nightcode.snout.shared.api.errortracking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One exception of an `$exception` event's `$exception_list`. Once PostHog has processed the event its frames are
 * resolved (`resolved_name`, `source`, `line`, and `context` when it has the source); frames it hasn't keep the
 * SDK's raw keys (`function`, `filename`, `lineno`, `colno`).
 */
@Serializable
data class ExceptionDto(
    @SerialName("type") val type: String? = null,
    @SerialName("value") val value: String? = null,
    @SerialName("module") val module: String? = null,
    @SerialName("mechanism") val mechanism: ExceptionMechanismDto? = null,
    @SerialName("stacktrace") val stacktrace: ExceptionStacktraceDto? = null,
)

@Serializable
data class ExceptionMechanismDto(@SerialName("handled") val handled: Boolean? = null)

/** Frames oldest call first, so the last one threw. */
@Serializable
data class ExceptionStacktraceDto(@SerialName("frames") val frames: List<ExceptionFrameDto> = emptyList())

@Serializable
data class ExceptionFrameDto(
    @SerialName("resolved_name") val resolvedName: String? = null,
    @SerialName("mangled_name") val mangledName: String? = null,
    @SerialName("function") val function: String? = null,
    @SerialName("module") val module: String? = null,
    @SerialName("source") val source: String? = null,
    @SerialName("filename") val filename: String? = null,
    @SerialName("line") val line: Int? = null,
    @SerialName("lineno") val lineNumber: Int? = null,
    @SerialName("column") val column: Int? = null,
    @SerialName("colno") val columnNumber: Int? = null,
    @SerialName("in_app") val inApp: Boolean = false,
    @SerialName("context") val context: FrameContextDto? = null,
)

@Serializable
data class FrameContextDto(
    @SerialName("before") val before: List<ContextLineDto> = emptyList(),
    @SerialName("line") val line: ContextLineDto? = null,
    @SerialName("after") val after: List<ContextLineDto> = emptyList(),
)

@Serializable
data class ContextLineDto(@SerialName("number") val number: Int, @SerialName("line") val line: String)
