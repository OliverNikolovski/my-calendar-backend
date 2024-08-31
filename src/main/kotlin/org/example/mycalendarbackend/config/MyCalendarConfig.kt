package org.example.mycalendarbackend.config

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class MyCalendarConfig {

    @Bean
    fun restClient(): RestClient = RestClient.create("http://localhost:3000")

    @Bean
    fun handlebars(): Handlebars {
        // Create a template loader to load templates from the classpath
        val templateLoader = ClassPathTemplateLoader("/templates", ".hbs")
        return Handlebars(templateLoader).with(ConcurrentMapTemplateCache())
    }

}