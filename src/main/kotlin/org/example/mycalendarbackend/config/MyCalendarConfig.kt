package org.example.mycalendarbackend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class MyCalendarConfig {

    @Bean
    fun restClient(): RestClient {
        return RestClient.create("http://localhost:3000")
    }

}