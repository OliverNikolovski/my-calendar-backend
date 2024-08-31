package org.example.mycalendarbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan(basePackageClasses = [MyCalendarBackendApplication::class])
@EnableScheduling
@EnableAsync
class MyCalendarBackendApplication

fun main(args: Array<String>) {
    runApplication<MyCalendarBackendApplication>(*args)
}
