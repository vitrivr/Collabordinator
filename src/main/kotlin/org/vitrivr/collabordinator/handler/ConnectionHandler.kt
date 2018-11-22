package org.vitrivr.collabordinator.handler

import kotlinx.serialization.json.JSON
import org.eclipse.jetty.websocket.api.Session
import org.vitrivr.collabordinator.message.Message
import java.util.concurrent.ConcurrentSkipListSet

object ConnectionHandler {

    private val connections = ConcurrentSkipListSet<Session>()

    fun addConnection(connection: Session) {
        this.connections.add(connection)
    }

    fun removeConnection(connection: Session) {
        this.connections.remove(connection)
    }

    fun broadcast(message: Message) {
        val jsonData = JSON.stringify(Message.serializer(), message)
        this.connections.forEach {
            it.remote.sendString(jsonData)
        }
    }

}