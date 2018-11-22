package org.vitrivr.collabordinator.message

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class Message(val action: Action, @Optional val attribute: List<String> = listOf())