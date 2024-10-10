package plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.hsts.*

fun Application.configureHTTPS() {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
        /*
        unsafe-inline - allow scripts/styles defined in file
        wasm-unsafe-eval - dynamic generated
        blob - for images
        data - inline photos, dynamic content
         */
        header("Content-Security-Policy",
            "default-src 'self'; " +
                    "script-src 'self' https://maps.googleapis.com https://maps.gstatic.com 'unsafe-inline' 'wasm-unsafe-eval'; " +
                    "style-src 'self' https://fonts.googleapis.com 'unsafe-inline'; " +
                    /* fetch */
                    "connect-src 'self' https://maps.googleapis.com https://mapsresources-pa.googleapis.com data:;" +
                    /* async */
                    "worker-src 'self' blob:; " +
                    "img-src 'self' https://maps.gstatic.com https://maps.googleapis.com data:; " +
                    "font-src 'self' https://fonts.gstatic.com;"
        )
    }
    //inform websites to use https
    install(HSTS) {
        includeSubDomains = true
        preload = true
    }
}
