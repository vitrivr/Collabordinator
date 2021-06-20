package org.vitrivr.collabordinator.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketException
import org.slf4j.LoggerFactory
import org.vitrivr.collabordinator.message.Action
import org.vitrivr.collabordinator.message.Message
import java.util.concurrent.CopyOnWriteArrayList

object ConnectionHandler {

    private val logger = LoggerFactory.getLogger("ConnectionHandler")
    private val mapper = jacksonObjectMapper()

    private val connections = CopyOnWriteArrayList<Session>()

    fun addConnection(connection: Session) {
        this.connections.add(connection)
        logger.info("new connection: ${connection.remoteAddress}")
        ListHandler.getKeys().forEach {
            connection.remote.sendString(
                    mapper.writeValueAsString(Message(Action.ADD, it, ListHandler.list(it)))
            )
        }
    }

    fun removeConnection(connection: Session) {
        this.connections.remove(connection)
        logger.info("connection closed: ${connection.remoteAddress}")
    }

    fun broadcast(message: Message) {
        val jsonData = mapper.writeValueAsString(message)
        this.connections.forEach {
            try {
                it.remote.sendString(jsonData)
            } catch (e: WebSocketException) {
                logger.error("Connection to ${it.remoteAddress} already closed, removing")
                this.connections.remove(it)
            }
        }
    }

}