package com.nightcode.snout.shared.telemetry.sdk

/** PostHog's name for error reports, the SDK's crashes and [PostHogTelemetry]'s non-fatal errors alike. */
internal const val ExceptionEvent = "\$exception"

private const val ExceptionList = "\$exception_list"
private const val ExceptionMessage = "\$exception_message"
private const val ExceptionValue = "value"

/**
 * What PostHog may send of an event the SDK is about to queue, or null to drop it. An event goes only while its kind
 * is switched on: error reports with [errors], everything else (usage events, screens, the app's opens and closes)
 * with [usage]. Error reports lose their messages: an exception's message can hold what the person's PostHog sent
 * (a serializer quotes the JSON it failed on, a client error names the URL), and the type and stack say where it
 * broke without it.
 */
internal fun filteredProperties(
    event: String,
    properties: Map<String, Any?>,
    usage: Boolean,
    errors: Boolean,
): Map<String, Any?>? {
    val isError = event == ExceptionEvent
    return when {
        isError && !errors -> null
        !isError && !usage -> null
        isError -> properties.withoutExceptionMessages()
        else -> properties
    }
}

private fun Map<String, Any?>.withoutExceptionMessages(): Map<String, Any?> = this - ExceptionMessage +
    (
        ExceptionList to (this[ExceptionList] as? List<*>)?.map { exception ->
            (exception as? Map<*, *>)?.minus(
                ExceptionValue,
            )
        }
        )
