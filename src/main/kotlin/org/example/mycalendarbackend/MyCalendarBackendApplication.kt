package org.example.mycalendarbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan(basePackageClasses = [MyCalendarBackendApplication::class])
class MyCalendarBackendApplication

fun main(args: Array<String>) {
    runApplication<MyCalendarBackendApplication>(*args)
}
