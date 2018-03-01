package fr.ybonnel

import com.github.andrewoma.kwery.core.DefaultSession
import com.github.andrewoma.kwery.core.dialect.PostgresDialect
import fr.ybonnel.framework.Server
import fr.ybonnel.framework.configuration.Configuration
import fr.ybonnel.framework.database.Database
import io.restassured.RestAssured
import io.restassured.config.HttpClientConfig
import io.restassured.config.RestAssuredConfig
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.sql.Connection
import java.util.*

val port = Random().nextInt(10000) + 10000

var server: Server? = null

class TestServer : BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
    override fun beforeEach(context: ExtensionContext?) {
        Database.openConnection().use { connection: Connection ->

            with(DefaultSession(connection, PostgresDialect())) {

                select("SELECT tablename FROM pg_tables WHERE schemaname=:schema",
                        mapOf(
                                "schema" to "public"
                        )
                ) { row ->
                    row.string("tablename")
                }.filter {tableName -> 
                    !tableName.startsWith("database") // ignore liquibase tables
                }.forEach { tableName ->
                    update("TRUNCATE $tableName CASCADE")
                }
            }

        }
    }

    override fun beforeAll(context: ExtensionContext?) {
        System.setProperty("application.mode", "test")
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
        System.setProperty("log4j.configurationFile", Configuration.getProperty("log4j2.file"))

        Database.migrate()

        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 10000)
                        .setParam("http.socket.timeout", 10000))

        server = Server(::routes, port).startServer()
    }


    override fun afterAll(context: ExtensionContext?) {
        server?.stop()
    }

}