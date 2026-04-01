package com.quizapp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetLink) {
        String subject = "Password Reset Request - Quiz App";
        String body = """
                <html><body>
                <h2>Password Reset</h2>
                <p>Hi %s,</p>
                <p>Click the link below to reset your password. This link expires in 1 hour.</p>
                <a href="%s" style="background:#4CAF50;color:white;padding:10px 20px;text-decoration:none;border-radius:4px;">Reset Password</a>
                <p>If you did not request this, please ignore this email.</p>
                </body></html>
                """.formatted(fullName, resetLink);
        send(toEmail, subject, body);
    }

    @Async
    public void sendAccessCodeEmail(String toEmail, String userName, String code,
                                     String categoryName, String expiryDate) {
        String subject = "Your Access Code - Quiz App";
        String body = """
                <html><body>
                <h2>Payment Approved! 🎉</h2>
                <p>Hi %s,</p>
                <p>Your payment has been approved. Here are your access details:</p>
                <table border="1" cellpadding="8" cellspacing="0">
                  <tr><td><b>Category</b></td><td>%s</td></tr>
                  <tr><td><b>Access Code</b></td><td style="font-size:24px;font-weight:bold;color:#4CAF50;">%s</td></tr>
                  <tr><td><b>Valid Until</b></td><td>%s</td></tr>
                </table>
                <p>Use this code in the app to unlock all quizzes under <b>%s</b>.</p>
                <p><i>Note: This code is personal and cannot be shared.</i></p>
                </body></html>
                """.formatted(userName, categoryName, code, expiryDate, categoryName);
        send(toEmail, subject, body);
    }

    @Async
    public void sendPaymentRejectionEmail(String toEmail, String userName, String categoryName, String reason) {
        String subject = "Payment Rejected - Quiz App";
        String body = """
                <html><body>
                <h2>Payment Status Update</h2>
                <p>Hi %s,</p>
                <p>Unfortunately, your payment for <b>%s</b> was rejected.</p>
                <p><b>Reason:</b> %s</p>
                <p>Please contact support if you have questions.</p>
                </body></html>
                """.formatted(userName, categoryName, reason);
        send(toEmail, subject, body);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
