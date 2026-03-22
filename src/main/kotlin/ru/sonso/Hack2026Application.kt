package ru.sonso

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.sonso.properties.AuthenticationProperties
import ru.sonso.properties.SuperadminProperties

@SpringBootApplication
@EnableConfigurationProperties(
    AuthenticationProperties::class,
    SuperadminProperties::class,
)
class Hack2026Application

fun main(args: Array<String>) {
    runApplication<Hack2026Application>(*args)
}
