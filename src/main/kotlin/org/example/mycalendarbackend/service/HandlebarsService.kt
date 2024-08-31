package org.example.mycalendarbackend.service

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
import org.springframework.stereotype.Service

@Service
class HandlebarsService(
    private val handlebars: Handlebars
) {
    val emailReminderTemplateName = "eventReminder"

    private fun getCompiledEmailTemplate(): Template = handlebars.compile(emailReminderTemplateName)

    fun getEmailTemplate(context: Any): String = getCompiledEmailTemplate().apply(context)

}