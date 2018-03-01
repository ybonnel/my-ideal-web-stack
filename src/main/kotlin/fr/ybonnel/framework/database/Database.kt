package fr.ybonnel.framework.database

import fr.ybonnel.framework.configuration.Configuration
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.PostgreSQLClient
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import java.sql.Connection
import java.sql.DriverManager

object Database {

    fun openConnection(): Connection {
        val host= Configuration.getProperty("database.host", "localhost")
        val port= Configuration.getProperty("database.port", "5432")
        val database= Configuration.getProperty("database.database", "testdb")
        val user= Configuration.getProperty("database.username", "postgres")
        val password= Configuration.getProperty("database.password", "")
        return DriverManager.getConnection("jdbc:postgresql://$host:$port/$database?user=$user&password=$password")
    }
    
    fun createPostgresClient(vertx: Vertx): AsyncSQLClient? {
        val host= Configuration.getProperty("database.host", "localhost")
        val port= Configuration.getProperty("database.port", "5432")
        val database= Configuration.getProperty("database.database", "testdb")
        val user= Configuration.getProperty("database.username", "postgres")
        val password= Configuration.getProperty("database.password", "")
        val maxPoolSize= Configuration.getProperty("database.maxPoolSize", "10")
        return PostgreSQLClient.createShared(vertx, JsonObject( mapOf(
                "host" to host,
                "port" to port.toInt(),
                "maxPoolSize" to maxPoolSize.toInt(),
                "username" to user,
                "password" to password,
                "database" to database
        )))
    }
    


    fun migrate() {
        openConnection().use {

            val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(it))
            val liquibase = Liquibase("db/migration.sql", ClassLoaderResourceAccessor(), database)

            val checksumOfChangesSets =
                    liquibase.databaseChangeLog.changeSets.map {
                        (it.author + ":" + it.id) to it.generateCheckSum()
                    }.toMap()
            
            val nbChangeSetsToRollback = database.ranChangeSetList.dropWhile {
                val key = it.author + ":" + it.id
                it.lastCheckSum != null && checksumOfChangesSets[key] == it.lastCheckSum
            }.size

            if (nbChangeSetsToRollback > 0) {
                liquibase.clearCheckSums()
                liquibase.rollback(nbChangeSetsToRollback, Contexts(), LabelExpression())
            }

            liquibase.update(Contexts(), LabelExpression())
        }
    }
    
    
}