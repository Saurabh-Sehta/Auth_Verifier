package com.Saurabh.Auth_app.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void setWelcomeEmail(String toEmail, String name){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to our platform");
        message.setText("Hello "+name+",\n\nThanks for registering with us!\n\nRegards,\nAuth-Verifier Team");
        mailSender.send(message);
    }

//    public void sendResetOtpEmail(String toEmail, String otp) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(fromEmail);
//        message.setTo(toEmail);
//        message.setSubject("Password Reset OTP");
//        message.setText("Your OTP for reseting your password is " + otp + ". Use this OTP to proceed with resetting your password.");
//        mailSender.send(message);
//    }
//
//    public void sendOtpEmail(String toEmail, String otp){
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(fromEmail);
//        message.setTo(toEmail);
//        message.setSubject("Account verifiation OTP");
//        message.setText("Your OTP is " + otp + ". Verify your account using this OTP.");
//        mailSender.send(message);
//    }

    public void sendOtpEmail(String toEmail, String name, String otp) throws MessagingException {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("otp",otp);
        context.setVariable("expiryMinutes", "15");

        String process = templateEngine.process("verify-email", context);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Account verification OTP");
        helper.setText(process, true);

        mailSender.send(mimeMessage);
    }

    public void sendResetOtpEmail(String toEmail, String name, String otp) throws MessagingException {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("otp",otp);
        context.setVariable("expiryMinutes", "15");

        String process = templateEngine.process("password-reset-email", context);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Reset Password OTP");
        helper.setText(process, true);

        mailSender.send(mimeMessage);
    }
}
