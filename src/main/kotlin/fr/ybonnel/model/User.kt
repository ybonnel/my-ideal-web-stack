package fr.ybonnel.model

import fr.ybonnel.framework.database.first
import fr.ybonnel.framework.database.select
import io.vertx.ext.sql.SQLConnection

data class User(val id: String? = null, val login: String, val password: String, val salt: String) {

    companion object {
        suspend fun findById(connection: SQLConnection, id: String): User? {
            return connection.select(
                    "SELECT * FROM users WHERE id = :id",
                    "id" to id
            ).first()?.mapTo(User::class.java)
        }
    }

}