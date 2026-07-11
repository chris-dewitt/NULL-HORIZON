package com.nullhorizon.app.content

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

fun JsonElement.toStateValue(): String {
    val primitive = this as? JsonPrimitive
        ?: error("Only primitive JSON values are supported in mission state")
    return primitive.content
}

fun Map<String, JsonElement>.toStateMap(): Map<String, String> =
    mapValues { (_, value) -> value.toStateValue() }
