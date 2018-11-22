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
                val newEntries = ListHandler.add(message.attribute)
                if (!newEntries.isEmpty()) {
                    ConnectionHandler.broadcast(Message(Action.ADD, newEntries))
                }
            }
            Action.REMOVE -> {
                val removedEntries = ListHandler.remove(message.attribute)
                if (!removedEntries.isEmpty()) {
                    ConnectionHandler.broadcast(Message(Action.REMOVE, removedEntries))
                }
            }
            Action.CLEAR -> {
                ListHandler.clear()
                ConnectionHandler.broadcast(Message(Action.CLEAR))
            }
            Action.LIST -> {
                user.remote.sendString(
                        JSON.stringify(Message.serializer(), Message(Action.LIST, ListHandler.list()))
                )
            }
        }
    }
}