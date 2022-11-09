package kz.bsbnb.microservices.nbcloud.mail.service;

import kz.bsbnb.microservices.nbcloud.mail.entity.MailConfig;
import kz.bsbnb.microservices.nbcloud.mail.repository.MailConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MailConfigService {
    @Autowired
    MailConfigRepository mailConfigRepository;

    public MailConfig getMailConfigByExtSystem(String extSystemCode) {
        return mailConfigRepository.findMailConfigByExtSystemCode(extSystemCode);
    }
}
