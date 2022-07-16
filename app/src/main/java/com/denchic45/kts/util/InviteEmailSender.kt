package com.denchic45.kts.util

import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart


open class InviteEmailSender {

    fun send(email: String) {
        val prop = Properties()
        prop["mail.smtp.host"] = "smtp.gmail.com";
        prop["mail.smtp.socketFactory.port"] = "465";
        prop["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory";
        prop["mail.smtp.auth"] = "true";
        prop["mail.smtp.port"] = "465";

        val session: Session = Session.getInstance(prop, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication("denchic150@gmail.com", "Den141819")
            }
        })

        val message: Message = MimeMessage(session)
//        message.setFrom(InternetAddress("from@gmail.com"))
        message.setFrom(InternetAddress(email))
        message.setRecipients(
            Message.RecipientType.TO, InternetAddress.parse(email)
        )
        message.subject = "Mail Subject"

        val msg = "This is my first email using JavaMailer"

        val mimeBodyPart = MimeBodyPart()
        mimeBodyPart.setContent(msg, "text/html; charset=utf-8")

        val multipart: Multipart = MimeMultipart()
        multipart.addBodyPart(mimeBodyPart)

        message.setContent(multipart)

        Transport.send(message)
    }
}