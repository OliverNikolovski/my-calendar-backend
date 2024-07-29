package org.example.mycalendarbackend.config

import org.example.mycalendarbackend.converter.deletiontypeconverter.StringToDeletionTypeConverter
import org.springframework.format.FormatterRegistry
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

class WebConfig : WebMvcConfigurer {

//    override fun addFormatters(registry: FormatterRegistry) {
//        registry.addConverter(StringToDeletionTypeConverter())
//    }

//    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
//        resolvers.add(TimeFormatArgumentResolver())
//    }

}