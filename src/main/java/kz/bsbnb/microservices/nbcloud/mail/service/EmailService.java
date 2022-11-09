package kz.bsbnb.microservices.nbcloud.mail.service;

import kz.bsbnb.microservices.common.exception.ErrorApi;
import kz.bsbnb.microservices.nbcloud.mail.entity.EmailAuthenticator;
import kz.bsbnb.microservices.nbcloud.mail.entity.MailConfig;
import kz.bsbnb.microservices.nbcloud.mail.property.FileStorageProperties;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    MailConfigService mailConfigService;
    @Autowired
    FileStorageProperties fileStorageProperties;

    @SneakyThrows
    public void sendMessage(String system, String to, String subject, String text, MultipartFile[] files) {
        long startAt = System.currentTimeMillis();

        MailConfig mailConfig = mailConfigService.getMailConfigByExtSystem(system.trim());

        if (mailConfig == null) {
            throw new ErrorApi("mail.system.notFound", new String[]{system});
        }

        // Настройка SMTP SSL
        Properties properties = new Properties();
        properties.put("mail.smtp.host"               , mailConfig.getHost());
        properties.put("mail.smtp.port"               , mailConfig.getPort());
        properties.put("mail.smtp.auth"               , mailConfig.getIsAuthEnable());
        properties.put("mail.smtp.auth.mechanisms"    , mailConfig.getAuthMechanism());
        properties.put("mail.smtp.ssl.enable"         , mailConfig.getSslEnable());
        properties.put("mail.smtp.starttls.enable"    , mailConfig.getIsTlsEnable());
        properties.put("mail.smtp.auth.ntlm.domain"   , mailConfig.getAuthNtlmDomain());

        Session session = null;
        if (mailConfig.getIsAuthEnable())
            session = Session.getDefaultInstance(properties, new EmailAuthenticator(mailConfig.getUsername(), mailConfig.getPassword()));
        else
            session = Session.getInstance(properties);

        session.setDebug(mailConfig.getIsDebugEnable());

        InternetAddress email_from = new InternetAddress(mailConfig.getMailSender());
        InternetAddress email_to   = new InternetAddress(to);

        Message message = new MimeMessage(session);
        message.setFrom(email_from);
        message.setRecipient(Message.RecipientType.TO, email_to);
        message.setSubject(subject == null ? mailConfig.getDefaultMailSubject(): subject);
        message.setText(text == null ? mailConfig.getDefaultMailText(): text);

        // Содержимое сообщения
        Multipart multipart = new MimeMultipart("mixed");
        // Add Text Part
        BodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setContent(text == null ? mailConfig.getDefaultMailText(): text, "text/html; charset=UTF-8");
        multipart.addBodyPart(textBodyPart);

        // Add attachment
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                // Attachments
                String filename = file.getOriginalFilename();
                if (filename.contains("/") || filename.contains("\\")) {
                    filename = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
                    filename = filename.substring(filename.lastIndexOf("\\") + 1, filename.length());
                }
                logger.info("Filename before={}, after={}", file.getOriginalFilename(), filename);
                Path filepath = Paths.get(fileStorageProperties.getUploadDir() + File.separator + filename);
                if (Files.notExists(filepath)) {
                    if (!Files.isDirectory(Paths.get(fileStorageProperties.getUploadDir())))
                        Files.createDirectory(Paths.get(fileStorageProperties.getUploadDir()));
                    new FileOutputStream(filepath.toString(), false).close();
                }

                InputStream inputStream = file.getInputStream();
                File f = new File(filepath.toUri());
                copyInputStreamToFile(inputStream, f);

                MimeBodyPart bodyPart = new MimeBodyPart();
                bodyPart.attachFile(f);
                bodyPart.setFileName(filename);
                multipart.addBodyPart(bodyPart);
            }
        }
        // Определение контента сообщения
        message.setContent(multipart);

        //This will show you internal structure of your message! D
        message.saveChanges();
        //message.writeTo(System.out);
        Transport.send(message);
        logger.info("E-mail from system={} sent to {} subject \"{}\" in {}ms", system, to, subject, System.currentTimeMillis() - startAt);
    }

    // InputStream -> File
    private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }

    }

    public File getTestFile(String filename) throws ErrorApi {
        Path filepath = Paths.get(fileStorageProperties.getUploadDir() + File.separator + filename);
        if (Files.notExists(filepath)) {
            throw new ErrorApi(filepath + " is not exist!");
        }
        File file = new File(filepath.toUri());
        try {
            FileInputStream inputStream = new FileInputStream(file);
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                int read;
                byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            }
            return file;
        } catch (Exception e) {
            throw new ErrorApi(e);
        }


    }
}
