package org.vitrivr.collabordinator.handler

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eclipse.jetty.websocket.api.Session
import org.slf4j.LoggerFactory
import org.vitrivr.collabordinator.message.Action
import org.vitrivr.collabordinator.message.Message

object MessageHandler {

    private val logger = LoggerFactory.getLogger("MessageHandler")
    private val mapper = jacksonObjectMapper()

    fun onConnect(user: Session) {
        ConnectionHandler.addConnection(user)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClose(user: Session, status: Int, reason: String) {
        ConnectionHandler.removeConnection(user)
    }

    fun onMessage(user: Session, data: String) {
        val message = try {
            mapper.readValue(data, Message::class.java)
        } catch (e: JacksonException) {
            logger.error("received invalid message: $data")
            null
        } ?: return

        when (message.action) {
            Action.ADD -> {
                val newEntries = ListHandler.add(message.key, message.attribute)
                if (newEntries.isNotEmpty()) {
                    ConnectionHandler.broadcast(Message(Action.ADD, message.key, newEntries))
                    logger.info("Adding entries $newEntries to list ${message.key}")
                }
            }
            Action.REMOVE -> {
                val removedEntries = ListHandler.remove(message.key, message.attribute)
                if (removedEntries.isNotEmpty()) {
                    ConnectionHandler.broadcast(Message(Action.REMOVE, message.key, removedEntries))
                    logger.info("Removing entries $removedEntries to list ${message.key}")
                }
            }
            Action.CLEAR -> {
                ListHandler.clear(message.key)
                ConnectionHandler.broadcast(Message(Action.CLEAR, message.key))
                logger.info("Clearing list ${message.key}")
            }
            Action.LIST -> {
                user.remote.sendString(
                        mapper.writeValueAsString(Message(Action.ADD, message.key, ListHandler.list(message.key)))
                )
            }
        }
    }

}