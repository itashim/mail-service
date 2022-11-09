package kz.bsbnb.microservices.nbcloud.mail.service;

import kz.bsbnb.microservices.nbcloud.mail.entity.ErrorEntity;
import kz.bsbnb.microservices.nbcloud.mail.repository.ErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ErrorService {

    @Autowired
    private ErrorRepository errorRepository;

    @Autowired
    private EmailService emailService;

    public ErrorEntity getById(long id) {
        return errorRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public List<ErrorEntity> getByEmail(String email) {
        return errorRepository.findAllByEmail(email);
    }

    public ErrorEntity save(ErrorEntity errorEntity) {
        List<ErrorEntity> errors = this.getByEmail(errorEntity.getEmail());
        if (errors.size() != 0) {
            errors.forEach(e -> {
                if (e.getBody().equals(errorEntity.getBody())) {
                    errorEntity.setId(e.getId());
                }
            });
        }
        return errorRepository.save(errorEntity);
    }

    public void delete(ErrorEntity errorEntity) {
        errorRepository.delete(errorEntity);
    }

    public void deleteById(long id) throws IllegalArgumentException {
        errorRepository.deleteById(id);
    }

    public List<ErrorEntity> findAll() {
        List<ErrorEntity> errors = new ArrayList<>();
        errorRepository.findAll().forEach(errors::add);
        return errors;
    }

    public List<ErrorEntity> findAllBySystem(String system) {
        return errorRepository.findAllBySystem(system);
    }

    public boolean resend(ErrorEntity errorEntity) {
        String email = errorEntity.getEmail();
        String subject = errorEntity.getSubject();
        String body = errorEntity.getBody();
        String system = errorEntity.getSystem();
        emailService.sendMessage(system, email, subject, body, null);
        deleteById(errorEntity.getId());
        return true;
    }


}
