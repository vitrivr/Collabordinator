package org.vitrivr.collabordinator


import org.vitrivr.collabordinator.handler.MessageHandler
import spark.Spark


object Collabordinator {

    @JvmStatic
    fun main(args: Array<String>) {

        Spark.port(12345)

        Spark.webSocket("/ws", MessageHandler)

        Spark.init()

    }

}