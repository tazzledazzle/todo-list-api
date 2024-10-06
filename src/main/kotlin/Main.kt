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
                    call.respond(listOf(
                        TodoItem(1, "Buy groceries", "Buy milk, eggs, and bread"),
                        TodoItem(2, "Pay bills", "Pay electricity and water bills")
                    ))
                }
                post {
                    // put the received todo item into the database
                    val todo = call.receive<TodoItem>()
                    call.respond(HttpStatusCode.Created, todo)
                }
                delete {
                    // delete all todo items from the database
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }.start(wait = true)
}