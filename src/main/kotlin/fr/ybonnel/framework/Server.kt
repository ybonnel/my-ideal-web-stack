package fr.ybonnel.framework

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean


class Server(val register: (Router, Vertx) -> Unit, val port: Int = 9000) {
    
    init {
        Json.mapper.registerModule(KotlinModule())
    }

    companion object {
        val logger = LoggerFactory.getLogger(Server::class.java)!!
    }
    
    private val vertx: Vertx = Vertx.vertx()
    
    fun startServer(): Server {
        logger.info("Starting server")
        val server = vertx.createHttpServer()
        val router = Router.router(vertx)
        register(router, vertx)
        val serverStarted = AtomicBoolean(false)
        server.requestHandler(router::accept).listen(port, {result ->
            if (result.succeeded()) {
                logger.info("Server listening on $port")
            } else {
                logger.error("Server failed to start", result.cause())
            }
            serverStarted.set(true)
        })
        while (!serverStarted.get()) {
            Thread.sleep(10)
        }
        return this
    }
    
    fun stop() {
        vertx.close()
    }
}