package kz.bsbnb.microservices.nbcloud.mail.repository;

import kz.bsbnb.microservices.nbcloud.mail.entity.MailConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MailConfigRepository extends JpaRepository<MailConfig, Long> {
    @Query(value = """
            SELECT m.*
            FROM mail_config m
            LEFT JOIN ext_sys es ON m.ext_sys_id = es.id
            WHERE upper(es.code) = upper(:extSystemCode)
            """, nativeQuery = true)
    MailConfig findMailConfigByExtSystemCode(String extSystemCode);
}
