package kz.bsbnb.microservices.nbcloud.mail.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "errors")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class ErrorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private String email;
    private String subject;
    @Column(length = 1200)
    private String body;
    private Date date;
    @Column(length = 350)
    private String error;
    private String system;

}
