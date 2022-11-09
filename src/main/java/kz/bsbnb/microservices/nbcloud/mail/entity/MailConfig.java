package kz.bsbnb.microservices.nbcloud.mail.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Table(name = "mail_config")
@Data
@SequenceGenerator(name = "entity_seq", sequenceName = "ext_sys_id_seq", allocationSize = 1)
@EqualsAndHashCode(callSuper=false)
public class MailConfig extends BaseEntityAudit {
    @ManyToOne
    @JoinColumn(name = "ext_sys_id")
    private ExtSystem extSystem;

    @Column(name = "host")
    private String  host;

    @Column(name = "port")
    private String port;

    @Column(name = "username")
    private String  username;

    @Column(name = "password")
    private String  password;

    @Column(name = "auth_ntlm_domain")
    private String  authNtlmDomain;

    @Column(name = "auth_mechanism")
    private String  authMechanism;

    @Column(name = "default_mail_subject")
    private String  defaultMailSubject;

    @Column(name = "default_mail_text")
    private String  defaultMailText;

    @Column(name = "mail_sender")
    private String  mailSender;

    @Column(name = "is_tls_enable")
    private Boolean  isTlsEnable;

    @Column(name = "is_auth_enable")
    private Boolean  isAuthEnable;

    @Column(name = "ssl_enable")
    private Boolean  sslEnable;

    @Column(name = "is_debug_enable")
    private Boolean  isDebugEnable;
}
