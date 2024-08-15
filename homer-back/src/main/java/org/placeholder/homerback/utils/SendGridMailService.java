package org.placeholder.homerback.utils;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.annotation.PostConstruct;
import org.placeholder.homerback.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SendGridMailService {
    Logger logger = LoggerFactory.getLogger(UserService.class);
    SendGrid sendGrid;
    @Value("${sendgridApiKey}")
    private String apiKey;

    @Value("${adminEmail}")
    private String adminEmail;

    @Value("${sendgridEmail}")
    private String sendgridEmail;

    @Value("${sendMail}")
    private Boolean sendMailEnabled;

    @Value("${sendAdminMail}")
    private Boolean sendAdminMailEnabled;

    @PostConstruct
    public void initSendGrid() {
        this.sendGrid = new SendGrid(apiKey);
    }

    public Mail verificationMail(String token, String email, String subject, String hintText, String buttonText, String redirectUrl, String bigTitle) {
        String html= "<html> <head> <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"> <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin> <link href=\"https://fonts.googleapis.com/css2?family=Cardo&family=Roboto&display=swap\" rel=\"stylesheet\"> <style> @import url('https://fonts.googleapis.com/css2?family=Cardo&family=Roboto&display=swap'); </style> </head> <body> <div style=\"width: 1000px; text-align:center\"> <h1 style=\"margin-bottom: 0;font-family:Cardo, serif; font-style: normal;font-size:36pt;\">" + bigTitle + "</h1> <p style=\"margin-left:50px; margin-right:50px;margin-top:20px; margin-bottom: 100px;font-family:Cardo, serif; font-style: normal;font-size:16pt;\">" + hintText + "</p> <button style=\"font-family:Roboto, sans-serif; border-radius:15px; letter-spacing:0.25em; font-size: 16pt; background-color:#212121; color: white; width: 300px; padding: 10px;\"> <a href=\"" + redirectUrl + "?token=" + token + "\" style=\" text-decoration: none; color:white; display: inline-block; width:100%;\">" + buttonText + "</a></button> </div> </body> </html>\n";
        String body = "<html>\n" +
                "   <head>\n" +
                "        <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n" +
                "        <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n" +
                "        <link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css2?family=Dosis:wght@100;400;500;600;700&display=swap\"/>\n" +
                "        <style> " +
                "           @import url('https://fonts.googleapis.com/css2?family=Cardo&family=Roboto&display=swap'); " +
                "           @import url('https://fonts.googleapis.com/css2?family=Dosis:wght@100;400;500;600;700&display=swap'); " +
                "       </style>" +
                "   </head>\n" +
                "   <body>\n" +
                "        <div style=\"width: 1000px; padding: 20px; border-radius: 20px; text-align:center; background-color: #ebc61c;\"> \n" +
                "            <h1 style=\"margin-bottom: 0;font-family:Dosis; font-style: normal;font-size:36pt;\">" + bigTitle + "</h1> \n" +
                "            <p style=\"margin-left:50px; margin-right:50px;margin-top:20px; font-family:Dosis; font-style: normal;font-size:20pt;\">" + hintText + "</p> \n" +
                "            <button style=\"font-family:Roboto, sans-serif; border-radius:15px; margin-bottom:20px; letter-spacing:0.25em; font-size: 16pt; background-color:#212121; color: white; width: 300px; padding: 10px;\"> " +
                "               <a href=\"" + redirectUrl + "?token=" + token + "\" style=\" text-decoration: none; color:white; display: inline-block; width:100%;\">" + buttonText + "</a>" +
                "            </button>" +
                "        </div> \n" +
                "    </body>\n" +
                "</html>";
        Email from = new Email(sendgridEmail);
        Email to = new Email(email);
        Content content = new Content("text/html", body);
        return new Mail(from, subject, to, content);
    }

    private void send(Mail mail) {
        logger.info("Sending mail...");
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = this.sendGrid.api(request);
            //logger.info(response.getBody());
        } catch (IOException ex) {
            System.out.println("SENDGRID MAIL ERROR");
            ex.printStackTrace();
        }
    }

    public void sendRegistrationMail(String token, String email) {
        if(!sendAdminMailEnabled) return;
        Mail mail = verificationMail(token, email, "Homer - Account verification", "Click the button below to verify your account","VERIFY", "http://localhost:3000/validate", "Welcome to Homer!");
        send(mail);
    }

    public void sendAdminPassword(String password) {
        if(!sendAdminMailEnabled) return;
        String body = "<html>\n" +
                "   <head>\n" +
                "        <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n" +
                "        <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n" +
                "        <link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css2?family=Dosis:wght@100;400;500;600;700&display=swap\"/>\n" +
                "   </head>\n" +
                "   <body>\n" +
                "        <div style=\"width: 1000px; padding: 20px; border-radius: 20px; text-align:center; background-color: #ebc61c;\"> \n" +
                "            <h1 style=\"margin-bottom: 0;font-family:Dosis; font-style: normal;font-size:36pt;\">Superadmin Password</h1> \n" +
                "            <p style=\"margin-left:50px; margin-right:50px;margin-top:20px; font-family:Dosis; font-style: normal;font-size:20pt;\">" + password + "</p> \n" +
                "            <p style=\"margin-left:50px; margin-right:50px;margin-top:20px; margin-bottom: 76px;font-family:Dosis; font-style: normal;font-size:14pt;\">This password is temporary and needs to be changed!</p>\n" +
                "        </div> \n" +
                "    </body>\n" +
                "</html>";
        Email from = new Email(sendgridEmail);
        Email to = new Email(adminEmail);
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, "Superadmin password", to, content);
        send(mail);
    }


    public void sendPropertyResponse(OnPropertyResponseEvent event) {
        if(!sendMailEnabled) return;
        String body = "<html>\n" +
                "   <head>\n" +
                "        <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n" +
                "        <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n" +
                "        <link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css2?family=Dosis:wght@100;400;500;600;700&display=swap\"/>\n" +
                "   </head>\n" +
                "   <body>\n" +
                "        <div style=\"width: 1000px; padding: 20px; border-radius: 20px; text-align:center; background-color: #ebc61c;\"> \n" +
                "            <h1 style=\"margin-bottom: 0;font-family:Dosis; font-style: normal;font-size:36pt;\">" + event.getHeroText() + "</h1> \n" +
                "            <p style=\"margin-left:50px; margin-right:50px;margin-top:20px; font-family:Dosis; font-style: normal;font-size:20pt;\">" + event.getBody() + "</p> \n" +
                "            <p style=\"margin-left:50px; margin-right:50px;margin-top:20px; margin-bottom: 76px;font-family:Dosis; font-style: normal;font-size:14pt;\">" + event.getReason() + "</p>\n" +
                "        </div> \n" +
                "    </body>\n" +
                "</html>";
        Email from = new Email(sendgridEmail);
        Email to = new Email(event.getEmail());
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, event.getTitle(), to, content);
        send(mail);
    }
}
