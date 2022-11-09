package kz.bsbnb.microservices.nbcloud.mail.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity(name = "ext_sys")
@Data
@SequenceGenerator(name = "entity_seq", sequenceName = "mail_config_id_seq", allocationSize = 1)
@EqualsAndHashCode(callSuper=false)
public class ExtSystem extends BaseEntityAudit {

    @Column(name = "name_kk_cy")
    private String nameKazCyl;

    @Column(name = "name_kk_la")
    private String nameKazLat;

    @Column(name = "name_ru")
    private String nameRu;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "code")
    private String code;

}
