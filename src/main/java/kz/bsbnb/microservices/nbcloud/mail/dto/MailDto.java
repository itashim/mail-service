package kz.bsbnb.microservices.nbcloud.mail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Data
@ToString
public class MailDto {
    @JsonProperty("system")
    @NotNull
    private String system;

    @JsonProperty("receiver")
    @NotNull
    private String to;

    @JsonProperty("subject")
    @NotNull
    private String subject;

    @JsonProperty("text")
    @NotNull
    @ToString.Exclude
    private String text;
}