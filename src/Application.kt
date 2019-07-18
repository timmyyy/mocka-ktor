package ru.ned

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.cookies.cookies
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.slf4j.event.Level


fun main(args: Array<String>) {
    embeddedServer(Netty, 8081) {
        install(CallLogging) {
            level = Level.INFO
        }

        install(DefaultHeaders) {
            header("X-Engine", "Ktor") // will send this header with each response
        }

        install(ContentNegotiation) {
            gson {
            }
        }
        runBlocking {
            val client = HttpClient(CIO) {
                install(JsonFeature) {
                    serializer = GsonSerializer()
                }
                install(Logging) {
                    level = LogLevel.HEADERS
                }
                followRedirects = false
                install(HttpCookies) {
                    // Will keep an in-memory map with all the cookies from previous requests.
                    storage = AcceptAllCookiesStorage()
                }
            }.apply { cookies("localhost") }

//        client.cookies("mydomain.com")

            routing {
                get("/login") {
//                    runBlocking {
                        val host = "http://localhost:8080/myapp/rest"
//                    val casHost = "https://load-cas.domclick.ru/cas/rest"

                        client.get<Any> {
                            url("$host/esa/redirect")
                        }

                        val cb = client.get<String> {
                            url("$host/foo/barr")
                            parameter("state", "")
                            parameter("code", "")

                        }
                        log.info(cb)


//                    }
                    call.respond(HttpStatusCode.Accepted, "hui")
                }
            }

        }
    }.start(wait = true)
}
