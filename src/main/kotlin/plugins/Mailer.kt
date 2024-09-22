package plugins

import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import security.Keys.emailAddress
import security.Keys.emailPassword

object Mailer {
    private val mailer = MailerBuilder
        .withSMTPServer("smtp.gmail.com", 587, emailAddress, emailPassword)
        .withTransportStrategy(TransportStrategy.SMTP_TLS)
        .buildMailer()
    fun sendPasswordRestorationEmail(name: String, surname: String, email: String, link: String) {
        val plainTextMessage = """
            Drogi (a) ${name} ${surname} otrzymaliśmy prośbę o odzyskanie konta powiązanego z tym adresem e-mail. 
            Jeśli chcesz przywrócić konto przejdź pod wskazany adres: ${link}.
            Jeśli to nie ty ją wysłałeś prośbę o przywrócenie konta wystarczy, że zignorujesz tę wiadomość. 
        """

        val htmlMessage = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Przywrócenie Konta</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        color: #333;
                        line-height: 1.6;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        border: 1px solid #ddd;
                        border-radius: 5px;
                    }
                    .header {
                        background-color: #f4f4f4;
                        padding: 10px;
                        text-align: center;
                        border-bottom: 1px solid #ddd;
                    }
                    .content {
                        margin: 20px 0;
                        text-align: center;
                    }
                    .footer {
                        text-align: center;
                        font-size: 0.8em;
                        color: #777;
                    }
                    .content a {
                        color: #fff !important;
                        text-decoration: none;
                        border: 1px solid #fff;
                        border-radius: 5px;
                        padding: 15px;
                        margin: 10px 0;
                        background-color: #232b54;
                        transition: background-color .2s ease-in-out;
                        display: inline-block;
                    }
                    .content a:focus {
                        outline: none;
                        color: #fff !important;
                    }
                    .content a:visited {
                        color: #fff !important;
                    }
                    .content a:hover {
                        background-color: #40476b;
                        color: #fff !important;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Przywrócenie konta</h1>
                    </div>
                    <div class="content">
                        <p>Drogi (a) ${name} ${surname} otrzymaliśmy prośbę o odzyskanie konta powiązanego z tym adresem e-mail.</p>
                        <p>Jeśli chcesz przywrócić konto kliknij w poniższy link:</p>
                        <a href="${link}" target="_blank">Ustaw nowe hasło</a>
                         <p>Jeśli to nie ty ją wysłałeś prośbę o przywrócenie konta wystarczy, że zignorujesz tę wiadomość.</p>
                    </div>
                    <div class="footer">
                        <p>Jeśli masz pytania, skontaktuj się z nami.</p>
                    </div>
                </div>
            </body>
            </html>
        """

        val email = EmailBuilder.startingBlank()
            .from(emailAddress, emailAddress)
            .to("${name} ${surname}", email)
            .withSubject("Przywrócenie konta")
            .withPlainText(plainTextMessage)
            .withHTMLText(htmlMessage)
            .buildEmail()

        try {
            mailer.sendMail(email)
        } catch (e: Exception) {
            println("Failed to send email: ${e.message}")
            e.printStackTrace()
        }
    }
    fun sendNewAccountEmail(name: String, surname: String, email: String, login: String, password: String) {
        val plainTextMessage = """
        Drogi (a) ${name} ${surname},

        Twoje konto zostało pomyślnie utworzone. Poniżej znajdują się dane logowania:

        Login: ${login}
        Hasło: ${password}

        Zalecamy jak najszybszą zmianę hasła po pierwszym logowaniu dla zwiększenia bezpieczeństwa.

        Dziękujemy za zaufanie!
    """

        val htmlMessage = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Nowe Konto</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    color: #333;
                    line-height: 1.6;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                    border: 1px solid #ddd;
                    border-radius: 5px;
                }
                .header {
                    background-color: #f4f4f4;
                    padding: 10px;
                    text-align: center;
                    border-bottom: 1px solid #ddd;
                }
                .content {
                    margin: 20px 0;
                    text-align: center;
                }
                .footer {
                    text-align: center;
                    font-size: 0.8em;
                    color: #777;
                }
                .important {
                    color: #d9534f;
                    font-weight: bold;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Nowe Konto</h1>
                </div>
                <div class="content">
                    <p>Drogi (a) ${name} ${surname},</p>
                    <p>Twoje konto zostało pomyślnie utworzone. Poniżej znajdują się dane logowania:</p>
                    <p><strong>Login:</strong> ${login}</p>
                    <p><strong>Hasło:</strong> ${password}</p>
                    <p class="important">Zalecamy jak najszybszą zmianę hasła po pierwszym logowaniu dla zwiększenia bezpieczeństwa.</p>
                    <p>Dziękujemy za zaufanie!</p>
                </div>
                <div class="footer">
                    <p>Jeśli masz pytania, skontaktuj się z nami.</p>
                </div>
            </div>
        </body>
        </html>
    """

        val email = EmailBuilder.startingBlank()
            .from(emailAddress, emailAddress)
            .to("${name} ${surname}", email)
            .withSubject("Nowe Konto - Dane Logowania")
            .withPlainText(plainTextMessage)
            .withHTMLText(htmlMessage)
            .buildEmail()

        try {
            mailer.sendMail(email)
        } catch (e: Exception) {
            println("Failed to send email: ${e.message}")
            e.printStackTrace()
        }
    }


}