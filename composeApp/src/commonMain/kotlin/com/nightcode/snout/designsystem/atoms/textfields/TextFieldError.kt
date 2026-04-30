package com.nightcode.snout.designsystem.atoms.textfields

import org.jetbrains.compose.resources.StringResource

internal sealed interface TextFieldError {

    data class Resource(val value: StringResource) : TextFieldError

    data class Text(val value: String) : TextFieldError
}
