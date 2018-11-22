package org.vitrivr.collabordinator.handler

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JSON
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.vitrivr.collabordinator.message.Action
import org.vitrivr.collabordinator.message.Message

@WebSocket
object MessageHandler {

    @OnWebSocketConnect
    fun onConnect(user: Session) {
        ConnectionHandler.addConnection(user)
    }

    @OnWebSocketClose
    fun onClose(user: Session, status: Int, reason: String) {
        ConnectionHandler.removeConnection(user)
    }

    @OnWebSocketMessage
    fun onMessage(user: Session, data: String) {
        val message = try {
            JSON.parse(Message.serializer(), data)
        } catch (e: SerializationException) {
            //TODO log somewhere
            null
        } ?: return

        when (message.action) {
            Action.ADD -> {
                val newEntries = ListHandler.add(message.key, message.attribute)
                if (!newEntries.isEmpty()) {
                    ConnectionHandler.broadcast(Message(Action.ADD, message.key, newEntries))
                }
            }
            Action.REMOVE -> {
                val removedEntries = ListHandler.remove(message.key, message.attribute)
                if (!removedEntries.isEmpty()) {
                    ConnectionHandler.broadcast(Message(Action.REMOVE, message.key, removedEntries))
                }
            }
            Action.CLEAR -> {
                ListHandler.clear(message.key)
                ConnectionHandler.broadcast(Message(Action.CLEAR, message.key))
            }
            Action.LIST -> {
                user.remote.sendString(
                        JSON.stringify(Message.serializer(), Message(Action.LIST, message.key, ListHandler.list(message.key)))
                )
            }
        }
    }
}