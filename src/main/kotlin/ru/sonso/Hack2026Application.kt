package ru.sonso

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.sonso.properties.AuthenticationProperties

@SpringBootApplication
@EnableConfigurationProperties(
    AuthenticationProperties::class
)
class Hack2026Application

fun main(args: Array<String>) {
    runApplication<Hack2026Application>(*args)
}
