package org.example

import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

data class TodoItem(val id: Int, val title: String, val description: String)

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
        }
        install(StatusPages) {
            exception<Throwable> { cause ->
                call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
            }
        }
        routing {
            route("/todos") {
                get {
                    // Respond with a list of todo items from the database
                    val page = call.request.queryParameters["page"]?.toInt() ?: 1
                    val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
                    call.respond(mapOf(
                        "data" to listOf(
                            TodoItem(1, "Buy groceries", "Buy milk, eggs, and bread"),
                            TodoItem(2, "Pay bills", "Pay electricity and water bills")
                        ),
                        "page" to page,
                        "limit" to limit,
                        "total" to 2
                    ))
                }
                post {
                    // put the received todo item into the database
                    val todo = call.receive<TodoItem>()
                    call.respond(HttpStatusCode.Created, todo)
                }
                put("/{id}") {
                    // update the todo item with the given id in the database
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                        return@put
                    }
                    val todo = call.receive<TodoItem>()
                    call.respond(HttpStatusCode.OK, todo.copy(id = id))
                }
                delete("/{id}") {
                    // delete all todo items from the database
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                        return@delete
                    }
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }.start(wait = true)
}