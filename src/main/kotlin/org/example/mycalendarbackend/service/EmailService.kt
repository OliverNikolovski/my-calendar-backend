package org.example.mycalendarbackend.service

import jakarta.annotation.PostConstruct
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender
) {

    fun sendEmail(to: String, subject: String, text: String) {
        val message = mailSender.createMimeMessage()
        MimeMessageHelper(message, true).also {
            it.setTo(to)
            it.setSubject(subject)
            it.setText(text, true)
        }
        mailSender.send(message)
    }

}