package kz.bsbnb.microservices.nbcloud.mail.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kz.bsbnb.microservices.common.exception.ErrorApi;
import kz.bsbnb.microservices.nbcloud.mail.dto.MailDto;
import kz.bsbnb.microservices.nbcloud.mail.entity.ErrorEntity;
import kz.bsbnb.microservices.nbcloud.mail.service.EmailService;
import kz.bsbnb.microservices.nbcloud.mail.service.ErrorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.SendFailedException;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping(path = "/mail")
@Api(tags = {"Mail Controller"}, description = "Сервис почтовых сообщении")
public class MailController {
    private static final Logger LOGGER = LoggerFactory.getLogger("MailController");

    @Autowired
    EmailService emailService;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private ErrorService errorService;


    private ResponseEntity<Object> sendResponse(HttpServletRequest request, Map<String, Object> msg, String status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status);
        if (msg != null) {
            body.put("msg", msg);
        }
        body.put("path", request.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.OK);
    }


    @PostMapping(value = "/send", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Отправка почты")
    public @ResponseBody
    ResponseEntity<Object> sendMail(HttpServletRequest request, @RequestBody MailDto mailDto) throws ErrorApi {
        try {
            emailService.sendMessage(mailDto.getSystem(), mailDto.getTo(), mailDto.getSubject(), mailDto.getText(), null);
            return sendResponse(request, null, "success");
        } catch (Throwable exp) {
            throw new ErrorApi(exp);
        }
    }

    @PostMapping("/send-files")
    public ResponseEntity<Object> sendMailWithFiles(@RequestPart("files") MultipartFile[] files,
                                                    @RequestPart("mail") MailDto mailDto,
                                                    HttpServletRequest request) throws ErrorApi {
        try {
            emailService.sendMessage(mailDto.getSystem(), mailDto.getTo(), mailDto.getSubject(), mailDto.getText(), files);
            return sendResponse(request, null, "success");
        } catch (Throwable throwable) {
            throw new ErrorApi(throwable);
        }

    }

    @PostMapping("/send-files-meta-parts")
    public ResponseEntity<Object> sendFiles(@RequestPart("files") MultipartFile[] files,
                                            @RequestPart("system") String system,
                                            @RequestPart("to") String to,
                                            @RequestPart("subject") String subject,
                                            @RequestPart("text") String text,
                                            HttpServletRequest request) throws ErrorApi {
        LOGGER.info("Rest service send-files, files size={}, subject={}, to={}, text={}, system={}",
                files.length, subject, to, text, system);

        try {
            if (system == null || system.isEmpty()) {
                throw new ErrorApi("mail.system.required");
            }
            if (to == null || to.isEmpty()) {
                throw new ErrorApi("mail.to.required");
            }
            if (subject == null || subject.isEmpty()) {
                throw new ErrorApi("mail.subject.required");
            }
            emailService.sendMessage(system, to, subject, text, files);
            return sendResponse(request, null, "success");
        } catch (Throwable throwable) {
            if (throwable instanceof ErrorApi)
                throw throwable;
            else
                throw new ErrorApi(throwable);
        }

    }

    @GetMapping("/testRestMultipart")
    public ResponseEntity<Object> testMultipart(HttpServletRequest request) throws ErrorApi {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("files", emailService.getTestFile("eye.jpg"));
        body.add("files", emailService.getTestFile("eye2.jpg"));

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        String serverUrl = "http://localhost:8777/api/mail/files/";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate
                .postForEntity(serverUrl, requestEntity, String.class);

        Map map = new HashMap();
        map.put("resp_status", response.getStatusCode());
        map.put("resp_body", response.getBody());
        return sendResponse(request, null, "success");
    }

    @PostMapping(value = "/send-by-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<Object> send(HttpServletRequest request, @RequestBody MailDto mailDto) {
        try {
            emailService.sendMessage(mailDto.getSystem(), mailDto.getTo(), mailDto.getSubject(), mailDto.getText(), null);
            LOGGER.info(mailDto.getText());
            return sendResponse(request, null, "success");
        } catch (Throwable e) {
            Map<String, Object> errorMessage = new HashMap<>();
            if (e instanceof SendFailedException) {
                SendFailedException exp = (SendFailedException) e;
                if (exp.getInvalidAddresses() != null && exp.getInvalidAddresses().length > 0) {
                    errorMessage.put("invalid", Arrays.toString(exp.getInvalidAddresses()));
                    LOGGER.error("Invalid addresses = " + errorMessage.get("invalid"));
                } else if (exp.getValidSentAddresses() != null && exp.getValidSentAddresses().length > 0) {
                    errorMessage.put("validSent", Arrays.toString(exp.getInvalidAddresses()));
                    LOGGER.error("ValidSent addresses = " + errorMessage.get("validSent"));
                } else if (exp.getValidUnsentAddresses() != null && exp.getValidUnsentAddresses().length > 0) {
                    errorMessage.put("validUnsent", Arrays.toString(exp.getInvalidAddresses()));
                    LOGGER.error("ValidUnsent addresses = " + errorMessage.get("validUnsent"));
                }
                return sendResponse(request, errorMessage, "fail");
            } else {
                errorMessage.put("exp", e.getMessage() != null ? e.getMessage() : e.getStackTrace());
                LOGGER.error("EXP = " + mailDto.toString(), e);
                return sendResponse(request, errorMessage, "error");
            }
        }
    }

    @PostMapping("/resend")
    @ApiOperation(value = """
            Этот метод служит для повторной попытки отправки сообщений, которые не смогли по какой-то причине оптравиться
            """)
    public ResponseEntity<Object> resend(HttpServletRequest request) {
        List<ErrorEntity> errorEntityList = errorService.findAll();
        LOGGER.info(":: MailController :: /mail/resend - обнаружено {} записей.. пытаемся переотправить", errorEntityList.size());
        int sended = 0;
        for (ErrorEntity e : errorEntityList) {
            boolean isSended = errorService.resend(e);
            if (isSended) {
                ++sended;
            }
        }
        LOGGER.info(":: MailController :: /mail/resend - отправлено {} из {} сообщений", sended, errorEntityList.size());
        return sendResponse(request, null, "success");
    }

    @PostMapping("/resend/{id}")
    @ApiOperation(value = """
            Этот метод служит для повторной попытки отправки сообщений, которые не смогли по какой-то причине оптравиться
            """)
    public ResponseEntity<Object> resendById(HttpServletRequest request, @PathVariable long id) {
        ErrorEntity errorEntity = errorService.getById(id);
        LOGGER.info(":: MailController :: /mail/resend/{} - запись обнаружена", id);
        boolean isSended = errorService.resend(errorEntity);
        LOGGER.info(":: MailController :: /mail/resend/{} - сообщение {}", id, isSended ? "отправлено" : "не отправлено");
        return sendResponse(request, null, "success");
    }


}
