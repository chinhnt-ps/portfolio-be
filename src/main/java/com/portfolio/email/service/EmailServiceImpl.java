package com.portfolio.email.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Email service implementation using Spring Mail (Gmail SMTP)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from}")
    private String fromEmail;
    
    @Value("${spring.mail.from-name:ChinhNT Auth Service}")
    private String fromName;

    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;
    
    @Override
    public void sendVerificationEmail(String to, String code) {
        try {
            String subject = "Xác nhận email đăng ký tài khoản";
            String htmlContent = buildVerificationEmailHtml(code);
            
            sendEmail(to, subject, htmlContent);
            log.info("Verification email sent successfully to: {}", to);
        } catch (org.springframework.mail.MailSendException e) {
            // Log chi tiết lỗi SendGrid
            String errorMessage = extractSendGridErrorMessage(e);
            log.error("Failed to send verification email to: {}. SendGrid error: {}", to, errorMessage, e);
            
            if (errorMessage != null && errorMessage.contains("verified Sender Identity")) {
                throw new RuntimeException(
                    "Email không được gửi vì địa chỉ gửi chưa được verify trong SendGrid. " +
                    "Vui lòng verify Single Sender hoặc Domain trong SendGrid Dashboard. " +
                    "Xem hướng dẫn: docs/ai/deployment/sendgrid-smtp-setup.md", e);
            }
            throw new RuntimeException("Không thể gửi email xác nhận. Vui lòng thử lại sau.", e);
        } catch (IOException e) {
            // SendGrid Web API error
            log.error("Failed to send verification email via SendGrid API to: {}", to, e);
            throw new RuntimeException("Không thể gửi email xác nhận. Vui lòng thử lại sau.", e);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
            throw new RuntimeException("Không thể gửi email xác nhận. Vui lòng thử lại sau.", e);
        }
    }
    
    @Override
    public void sendPasswordResetEmail(String to, String code) {
        try {
            String subject = "Đặt lại mật khẩu";
            String htmlContent = buildPasswordResetEmailHtml(code);
            
            sendEmail(to, subject, htmlContent);
            log.info("Password reset email sent successfully to: {}", to);
        } catch (org.springframework.mail.MailSendException e) {
            // Log chi tiết lỗi SendGrid
            String errorMessage = extractSendGridErrorMessage(e);
            log.error("Failed to send password reset email to: {}. SendGrid error: {}", to, errorMessage, e);
            
            if (errorMessage != null && errorMessage.contains("verified Sender Identity")) {
                throw new RuntimeException(
                    "Email không được gửi vì địa chỉ gửi chưa được verify trong SendGrid. " +
                    "Vui lòng verify Single Sender hoặc Domain trong SendGrid Dashboard. " +
                    "Xem hướng dẫn: docs/ai/deployment/sendgrid-smtp-setup.md", e);
            }
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.", e);
        } catch (IOException e) {
            // SendGrid Web API error
            log.error("Failed to send password reset email via SendGrid API to: {}", to, e);
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.", e);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.", e);
        }
    }
    
    /**
     * Extract SendGrid error message from exception
     */
    private String extractSendGridErrorMessage(org.springframework.mail.MailSendException e) {
        if (e.getFailedMessages() != null && !e.getFailedMessages().isEmpty()) {
            Exception failedException = e.getFailedMessages().values().iterator().next();
            if (failedException != null) {
                return failedException.getMessage();
            }
        }
        return e.getMessage();
    }
    
    /**
     * Send email using Spring Mail
     */
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException, IOException {
        // Railway thường chặn outbound SMTP (587/465). Nếu có SENDGRID_API_KEY thì ưu tiên dùng Web API (HTTPS/443).
        if (sendGridApiKey != null && !sendGridApiKey.isBlank()) {
            sendEmailViaSendGridApi(to, subject, htmlContent);
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        try {
            helper.setFrom(fromEmail, fromName);
        } catch (java.io.UnsupportedEncodingException e) {
            // Fallback to email only if encoding fails
            helper.setFrom(fromEmail);
        }
        
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML content
        
        mailSender.send(message);
    }

    private void sendEmailViaSendGridApi(String to, String subject, String htmlContent) throws IOException {
        Email from = new Email(fromEmail, fromName);
        Email recipient = new Email(to);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, recipient, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);
        int statusCode = response.getStatusCode();

        // SendGrid trả 202 khi accept gửi mail
        if (statusCode < 200 || statusCode >= 300) {
            String body = response.getBody();
            log.error("SendGrid API failed. status={}, body={}", statusCode, body);
            throw new IOException("SendGrid API error. status=" + statusCode + ", body=" + body);
        }

        log.info("SendGrid API accepted email. status={}", statusCode);
    }
    
    /**
     * Build HTML email template for verification
     */
    private String buildVerificationEmailHtml(String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .container {
                        background-color: #f9f9f9;
                        border-radius: 8px;
                        padding: 30px;
                        margin: 20px 0;
                    }
                    .code-box {
                        background-color: #ffffff;
                        border: 2px dashed #4CAF50;
                        border-radius: 8px;
                        padding: 20px;
                        text-align: center;
                        margin: 20px 0;
                        font-size: 24px;
                        font-weight: bold;
                        color: #4CAF50;
                        letter-spacing: 4px;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #ddd;
                        font-size: 12px;
                        color: #666;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Xác nhận email đăng ký tài khoản</h2>
                    <p>Xin chào,</p>
                    <p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng sử dụng mã xác nhận sau để kích hoạt tài khoản:</p>
                    
                    <div class="code-box">
                        %s
                    </div>
                    
                    <p><strong>Lưu ý:</strong></p>
                    <ul>
                        <li>Mã xác nhận có hiệu lực trong <strong>10 phút</strong></li>
                        <li>Không chia sẻ mã này với bất kỳ ai</li>
                        <li>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này</li>
                    </ul>
                    
                    <div class="footer">
                        <p>Email này được gửi tự động, vui lòng không trả lời.</p>
                        <p>&copy; 2026 ChinhNT Auth Service. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(code);
    }
    
    /**
     * Build HTML email template for password reset
     */
    private String buildPasswordResetEmailHtml(String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .container {
                        background-color: #f9f9f9;
                        border-radius: 8px;
                        padding: 30px;
                        margin: 20px 0;
                    }
                    .code-box {
                        background-color: #ffffff;
                        border: 2px dashed #FF9800;
                        border-radius: 8px;
                        padding: 20px;
                        text-align: center;
                        margin: 20px 0;
                        font-size: 24px;
                        font-weight: bold;
                        color: #FF9800;
                        letter-spacing: 4px;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #ddd;
                        font-size: 12px;
                        color: #666;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Đặt lại mật khẩu</h2>
                    <p>Xin chào,</p>
                    <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Vui lòng sử dụng mã xác nhận sau:</p>
                    
                    <div class="code-box">
                        %s
                    </div>
                    
                    <p><strong>Lưu ý:</strong></p>
                    <ul>
                        <li>Mã xác nhận có hiệu lực trong <strong>10 phút</strong></li>
                        <li>Không chia sẻ mã này với bất kỳ ai</li>
                        <li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>
                    </ul>
                    
                    <div class="footer">
                        <p>Email này được gửi tự động, vui lòng không trả lời.</p>
                        <p>&copy; 2026 ChinhNT Auth Service. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(code);
    }
}
