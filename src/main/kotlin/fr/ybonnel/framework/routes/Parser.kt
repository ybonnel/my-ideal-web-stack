package fr.ybonnel.framework.routes

import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import io.vertx.ext.web.RoutingContext

object Parser {
    
    inline fun <reified T> fromJson(ctx: RoutingContext): T? {
        try {
            return ctx.bodyAsString?.let { 
                Klaxon().parse<T>(it)
            }
        } catch (exception: KlaxonException) {
            throw IllegalArgumentException(exception)
        }
    }
    
    fun toJson(ctx: RoutingContext, entity: Any?) {
        if (entity != null) {
            ctx.response().end(Klaxon().toJsonString(entity))
        } else {
            ctx.response().end()
        }
    }


}