package fr.ybonnel.framework.database

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.coroutines.await


val regexp = """[^:]:([a-zA-Z][a-zA-Z0-9]*)""".toRegex()

fun extractParamsNames(sql: String): Pair<String, List<String>> {
    val params = mutableListOf<String>()
    val sqlResult = regexp.replace(sql, { matchResult ->
        params.add(matchResult.groupValues[1])
        "${matchResult.groupValues[0][0]}?"
    })
    return sqlResult to params
}

private fun paramsToJsonArray(paramNames: List<String>, params: Array<out Pair<String, Any?>>): JsonArray {
    val paramsMap = params.toMap()
    return json {
        array(
                *paramNames.map {
                    paramsMap[it]
                }.toTypedArray()
        )
    }
}

suspend fun SQLConnection.update(sql: String, vararg params: Pair<String, Any?>): UpdateResult {
    val result = Future.future<UpdateResult>()
    val (sqlReplaced, paramNames) = extractParamsNames(sql)

    val paramsToJsonArray = paramsToJsonArray(paramNames, params)
    this.updateWithParams(
            sqlReplaced,
            paramsToJsonArray
    ) {
        it.sendToFuture(result)
    }
    return result.await()
}

suspend fun SQLConnection.select(sql: String, vararg params: Pair<String, Any?>): ResultSet {
    val result = Future.future<ResultSet>()
    val (sqlReplaced, paramNames) = extractParamsNames(sql)

    this.queryWithParams(
            sqlReplaced,
            paramsToJsonArray(paramNames, params)
    ) {
        it.sendToFuture(result)
    }
    return result.await()
}

fun ResultSet.first(): JsonObject? {
    return if (this.rows.isNotEmpty())
        this.rows.first()
    else
        null
}



val RoutingContext.sqlClient : AsyncSQLClient
    get() = this.get("sqlClient")

fun <T> AsyncResult<T>.sendToFuture(future: Future<T>) {
    if (this.succeeded()) {
        future.complete(this.result())
    } else {
        future.fail(this.cause())
    }
}

suspend fun AsyncSQLClient.getConnection(): SQLConnection {
    val future = Future.future<SQLConnection>()
    this.getConnection { it.sendToFuture(future) }
    return future.await()
}