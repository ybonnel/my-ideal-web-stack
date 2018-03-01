package fr.ybonnel


import fr.ybonnel.controllers.UsersController
import fr.ybonnel.framework.Server
import fr.ybonnel.framework.configuration.Configuration
import fr.ybonnel.framework.database.Database
import fr.ybonnel.framework.routes.*
import fr.ybonnel.model.User
import io.vertx.core.Vertx
import io.vertx.ext.web.Router


fun main(args: Array<String>) {
    System.setProperty("log4j.configurationFile", Configuration.getProperty("log4j2.file"))
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
    Database.migrate()

    Server(::routes).startServer()
}

fun routes(router: Router, vertx: Vertx) {
    router.defaultHandlers(Database.createPostgresClient(vertx))

    router.post("/services/users").coJsonPostService<User, User> { ctx, user -> UsersController.createUser(ctx, user) }
    router.get("/services/users/:id").coJsonGetService { ctx -> UsersController.getUser(ctx) }
    router.put("/services/users/:id").coJsonPostService<User, User> { ctx, user -> UsersController.updateUser(ctx, user) }
    router.delete("/services/users/:id").coJsonGetService { ctx -> UsersController.deleteUser(ctx) }
}