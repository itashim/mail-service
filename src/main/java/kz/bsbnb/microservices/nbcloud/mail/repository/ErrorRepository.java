package kz.bsbnb.microservices.nbcloud.mail.repository;

import kz.bsbnb.microservices.nbcloud.mail.entity.ErrorEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorRepository extends CrudRepository<ErrorEntity, Long> {

    List<ErrorEntity> findAllByEmail(String email);

    List<ErrorEntity> findAllBySystem(String email);

}
