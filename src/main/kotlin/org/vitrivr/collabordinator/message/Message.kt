package org.vitrivr.collabordinator.message

data class Message(val action: Action, val key: String = "default", val attribute: List<String> = listOf())