package org.example.mycalendarbackend.config

import org.example.mycalendarbackend.annotation.TimeFormatter
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TimeFormatArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.getParameterAnnotation(TimeFormatter::class.java) != null && parameter.parameterType == LocalTime::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val timeStr = parameter.parameterName?.let {
            webRequest.getParameter(it)
        } ?: return null
        val timeFormat = parameter.getParameterAnnotation(TimeFormatter::class.java)?.format ?: return null
        val formatter = when (timeFormat) {
            TimeFormatter.Format.TWELVE_HOUR -> DateTimeFormatter.ofPattern("hh:mm a")
            TimeFormatter.Format.TWENTY_FOUR_HOUR -> DateTimeFormatter.ofPattern("HH:mm")
        }
        return LocalTime.parse(timeStr, formatter)
    }
}