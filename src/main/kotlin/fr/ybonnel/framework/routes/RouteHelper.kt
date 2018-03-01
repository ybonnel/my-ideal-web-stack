package fr.ybonnel.framework.routes

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory


fun Route.jsonServices(logger: Logger, asyncSQLClient: AsyncSQLClient?) {
    this.handler(BodyHandler.create())
            .handler { ctx: RoutingContext ->
                ctx.response().putHeader("Content-Type", "application/json")
                asyncSQLClient?.let { 
                    ctx.put("sqlClient", it)
                }
                ctx.next()
            }
            .failureHandler {
                logger.error("Error on ${it.request().method()} ${it.request().uri()}", it.failure())
                when (it.failure()) {
                    is IllegalArgumentException -> {
                        it.response().statusCode = 400
                        it.response().end("{\"error\": \"Bad request\"}")
                    }
                    else -> {
                        it.response().statusCode = 500
                        it.response().end("{\"error\": \"Internal Server Error\"}")
                    }
                }
            }
}

inline fun <reified T, R> Route.jsonPostService(
        noinline handler: (RoutingContext, T?) -> R?
): Route {
    
    return this.handler {
            val bodyEntity = Parser.fromJson<T>(it)
            Parser.toJson(it, handler(it, bodyEntity))
    }
}

inline fun <reified T, R> Route.coJsonPostService(
        noinline handler: suspend (RoutingContext, T?) -> R?
): Route {
    return this.handler {ctx ->
        launch(ctx.vertx().dispatcher()) {
            try {
                val bodyEntity = Parser.fromJson<T>(ctx)
                handler(ctx, bodyEntity)
                Parser.toJson(ctx, handler(ctx, bodyEntity))
            } catch (exception: Exception) {
                ctx.fail(exception)
            }
        }
    }
}



inline fun <reified T> Route.coJsonGetService(
        noinline handler: suspend (RoutingContext) -> T?
): Route {
    return this.handler {ctx ->
        launch(ctx.vertx().dispatcher()) {
            try {
                val entity = handler(ctx)
                if (entity == null) {
                    ctx.response().statusCode = 404
                    ctx.response().end()
                } else { 
                    Parser.toJson(ctx, entity)
                }
            } catch (exception: Exception) {
                ctx.fail(exception)
            }
        }
    }
}

inline fun <reified T, R> Route.jsonPostService(
        noinline handler: (T?) -> R?
): Route {
    return this.jsonPostService({ _, param: T? -> handler(param)})
}

inline fun <reified T> Route.jsonGetService(
        crossinline handler: (RoutingContext) -> T?
): Route {
    return this.handler {ctx ->
        Parser.toJson(ctx, handler(ctx))
    }
}


fun Router.defaultHandlers(asyncSQLClient: AsyncSQLClient? = null) {
    get("/").handler({ it.reroute("/public/index.html") })
    get("/public/*").handler(StaticHandler.create("public"))

    route("/services/*").jsonServices(LoggerFactory.getLogger(javaClass), asyncSQLClient)
}