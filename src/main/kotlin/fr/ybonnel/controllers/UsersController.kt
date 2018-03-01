package fr.ybonnel.controllers

import fr.ybonnel.framework.database.getConnection
import fr.ybonnel.framework.database.sqlClient
import fr.ybonnel.framework.database.update
import fr.ybonnel.model.User
import io.vertx.ext.web.RoutingContext
import java.util.*

object UsersController {


    suspend fun createUser(ctx: RoutingContext, user: User?): User? {
        return user?.let {
            val id = UUID.randomUUID().toString()

            ctx.sqlClient.getConnection().use { connection ->
                connection.update(
                        "INSERT INTO users (id, login, password, salt) VALUES (:id, :login, :password, :salt)",
                        "id" to id,
                        "login" to user.login,
                        "password" to user.password,
                        "salt" to user.salt
                )
            }

            user.copy(id = id)
        }
    }
    suspend fun updateUser(ctx: RoutingContext, user: User?): User? {
        return user?.let {
            val id = ctx.pathParam("id")

            ctx.sqlClient.getConnection().use { connection ->
                connection.update(
                        "UPDATE users SET login=:login, password=:password, salt=:salt WHERE id = :id",
                        "id" to id,
                        "login" to user.login,
                        "password" to user.password,
                        "salt" to user.salt
                )
            }

            user.copy(id = id)
        }
    }
    
    suspend fun deleteUser(ctx: RoutingContext): User? {
        val id = ctx.pathParam("id")
        
        return ctx.sqlClient.getConnection().use { connection ->
            val user = User.findById(connection, id)
            connection.update(
                    "DELETE FROM users WHERE id = :id1",
                    "id1" to id
            )
            user
        }
    }

    suspend fun getUser(ctx: RoutingContext): User? {
        return ctx.sqlClient.getConnection().use {connection ->
            User.findById(connection, ctx.pathParam("id"))
        }
    }

}
