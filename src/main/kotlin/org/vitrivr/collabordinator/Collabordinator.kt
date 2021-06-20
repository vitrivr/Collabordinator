package org.vitrivr.collabordinator


import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory
import org.eclipse.jetty.server.*
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.vitrivr.collabordinator.handler.MessageHandler


object Collabordinator {

    @JvmStatic
    fun main(args: Array<String>) {

        val plainPort = args.firstOrNull()?.toIntOrNull() ?: 12345
        val sslPort = args.getOrNull(1)?.toIntOrNull() ?: -1
        val keystorePath = args.getOrElse(2) {"keystore.jks"}
        val keystorePassword = args.getOrElse(3) {"password"}

        Javalin.create {
            it.enableCorsForAllOrigins()
            it.server { setupHttpServer(plainPort, sslPort, keystorePath, keystorePassword) }
            it.defaultContentType = "application/json"
            it.prefer405over404 = true
            it.enforceSsl = true
        }.routes {
            ws("ws"){ ws ->
                ws.onConnect {
                    MessageHandler.onConnect(it.session)
                }
                ws.onClose {
                    MessageHandler.onClose(it.session, it.status(), it.reason() ?: "no reason provided")
                }
                ws.onMessage {
                    MessageHandler.onMessage(it.session, it.message())
                }
            }
            get("/") { ctx ->
                ctx.contentType("text/html")
                ctx.result("Connect to Collabordinator using WebSocket")
            }
        }.start()

    }

    private fun setupHttpServer(plainPort: Int, sslPort: Int, keystorePath: String, keystorePassword : String ): Server {

        val httpConfig = HttpConfiguration().apply {
            sendServerVersion = false
            sendXPoweredBy = false
            if (sslPort > 0) {
                secureScheme = "https"
                securePort = sslPort
            }

        }


        if (sslPort > 0) {
            val httpsConfig = HttpConfiguration(httpConfig).apply {
                addCustomizer(SecureRequestCustomizer())
            }

            val alpn = ALPNServerConnectionFactory().apply {
                defaultProtocol = "http/1.1"
            }

            val sslContextFactory = SslContextFactory.Server().apply {
                keyStorePath = keystorePath
                setKeyStorePassword(keystorePassword)
                provider = "Conscrypt"
            }

            val ssl = SslConnectionFactory(sslContextFactory, alpn.protocol)

            val http2 = HTTP2ServerConnectionFactory(httpsConfig)

            val fallback = HttpConnectionFactory(httpsConfig)


            return Server().apply {
                //HTTP Connector
                addConnector(
                    ServerConnector(
                        server,
                        HttpConnectionFactory(httpConfig),
                        HTTP2ServerConnectionFactory(httpConfig)
                    ).apply {
                        port = plainPort
                    })
                // HTTPS Connector
                addConnector(ServerConnector(server, ssl, alpn, http2, fallback).apply {
                    port = sslPort
                })
            }
        } else {
            return Server().apply {
                //HTTP Connector
                addConnector(ServerConnector(server, HttpConnectionFactory(httpConfig), HTTP2ServerConnectionFactory(httpConfig)).apply {
                    port = plainPort
                })

            }
        }

    }

}