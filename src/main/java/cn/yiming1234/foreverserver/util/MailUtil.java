package cn.yiming1234.foreverserver.util;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class MailUtil {

    @Autowired
    private static JavaMailSender mailSender;

    /**
     * 发送文本邮件
     */
    public static void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("pleasurecruise@qq.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText("当前审核状态为：" + text);
        mailSender.send(message);
    }

    /**
     * 发送HTML邮件
     */
    public void sendHtmlMail(String to, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            message.setFrom("pleasurecruise@qq.com");
            messageHelper.setTo(InternetAddress.parse(to));
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
            mailSender.send(message);
            log.info("发送HTML邮件成功");
        } catch (Exception e) {
            log.error("发送HTML邮件失败", e);
        }
    }

    /**
     * 发送带附件的邮件
     */
    public void sendAttachmentsMail(String to, String subject, String content, String filePath) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            message.setFrom("pleasurecruise@qq.com");
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(content);

            // 添加附件
            FileSystemResource file = new FileSystemResource(new File(filePath));
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1); // 提取文件名
            messageHelper.addAttachment(fileName, file);

            mailSender.send(message);
            log.info("发送带附件的邮件成功");
        } catch (Exception e) {
            log.error("发送带附件的邮件失败", e);
        }
    }
}
