package org.vitrivr.collabordinator


import org.vitrivr.collabordinator.handler.MessageHandler
import spark.Spark


object Collabordinator {

    @JvmStatic
    fun main(args: Array<String>) {

        val port = args.firstOrNull()?.toIntOrNull() ?: 12345

        Spark.port(port)

        Spark.webSocket("/ws", MessageHandler)

        Spark.init()

    }

}