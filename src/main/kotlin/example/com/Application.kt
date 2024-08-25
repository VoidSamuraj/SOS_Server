package example.com

import dao.DatabaseFactory
import example.com.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val database = DatabaseFactory.init("jdbc:h2:file:./build/db", "org.h2.Driver", "root", "password")

    configureTemplating()
    configureSockets()
    configureHTTP()
    configureSecurity()
    configureMonitoring()
    configureRouting()
    configureSerialization()
}
