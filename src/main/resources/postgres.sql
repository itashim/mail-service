create user ext_usr with createdb password 'PassWord';
create database ext owner ext_usr;

------------------------------------------------------------------------------------------------------------------------
create table IF NOT EXISTS ext_sys
(
    id int not null,
    name_kk_cy varchar(250),
    name_kk_la varchar(250),
    name_ru varchar(250) not null,
    name_en varchar(250),
    code varchar(50),
    created_at date default now() not null,
    created_by bigint,
    updated_at date,
    updated_by bigint
);

comment on table  ext_sys is 'Внешние системы НБ РК';
comment on column ext_sys.id is 'Идентификатор';
comment on column ext_sys.name_kk_cy is 'Наименование на казахском языке(кириллица)';
comment on column ext_sys.name_kk_la is 'Наименование на казахском языке(латинский)';
comment on column ext_sys.NAME_RU is 'Наименование на русском';
comment on column ext_sys.NAME_EN is 'Наименование на английском языке';
comment on column ext_sys.code is 'Код внешной системы';
comment on column ext_sys.created_at is 'Дата начала действия';
comment on column ext_sys.created_by is 'Исполнитель, создавший данные';
comment on column ext_sys.updated_at is 'Дата последнего редактирования';
comment on column ext_sys.updated_by is 'Исполнитель, редактировавший данные';

create unique index IF NOT EXISTS ext_sys_id_uindex
    on ext_sys (id);

alter table ext_sys
    add constraint ext_sys_pk
        primary key (id);

create unique index IF NOT EXISTS ext_sys_code_uindex
    on ext_sys (code);

create sequence IF NOT EXISTS ext_sys_id_seq;
------------------------------------------------------------------------------------------------------------------------
create table IF NOT EXISTS mail_config(
                                id int not null,
                                ext_sys_id int not null ,
                                host varchar(50) not null ,
                                port int not null ,
                                username varchar(100),
                                password varchar(100),
                                is_tls_enable boolean default false,
                                is_auth_enable boolean default false,
                                auth_mechanism varchar(50),
                                ssl_enable boolean default false,
                                auth_ntlm_domain varchar(50),
                                mail_sender varchar(150),
                                default_mail_subject varchar(250),
                                default_mail_text varchar(500),
                                is_debug_enable boolean default false,
                                created_at date default now() not null,
                                created_by bigint,
                                updated_at date,
                                updated_by bigint
);

comment on table  mail_config is 'Конфигурация почтового сервера для внешних систем';
comment on column mail_config.id is 'Идентификатор';
comment on column mail_config.ext_sys_id is 'Идентификатор внешной системы НБ РК';
comment on column mail_config.host is 'The SMTP server to connect to';
comment on column mail_config.port is 'The SMTP server port to connect to';
comment on column mail_config.username is 'username information during the Authenticator callback';
comment on column mail_config.password is 'password information during the Authenticator callback';
comment on column mail_config.is_tls_enable is 'If true, enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection before issuing any login commands. Defaults to false';
comment on column mail_config.is_auth_enable is 'If true, attempt to authenticate the user using the AUTH command. Defaults to false';
comment on column mail_config.ssl_enable is 'If set to true, use SSL to connect and use the SSL port by default. Defaults to false for the "smtp" protocol and true for the "smtps" protocol';
comment on column mail_config.auth_ntlm_domain is 'The NTLM authentication domain';
comment on column mail_config.auth_mechanism is 'If set, lists the authentication mechanisms to consider. Only mechanisms supported by the server and supported by the current implementation will be used. The default is "LOGIN PLAIN DIGEST-MD5 NTLM", which includes all the authentication mechanisms supported by the current implementation';
comment on column mail_config.default_mail_subject is 'Default mail text';
comment on column mail_config.default_mail_text is 'Default mail text';
comment on column mail_config.mail_sender is 'Mail sender';
comment on column mail_config.created_at is 'Дата начала действия';
comment on column mail_config.created_by is 'Исполнитель, создавший данные';
comment on column mail_config.updated_at is 'Дата последнего редактирования';
comment on column mail_config.updated_by is 'Исполнитель, редактировавший данные';
comment on column mail_config.is_debug_enable is 'Признак отладчика';

create unique index IF NOT EXISTS mail_config_id_uindex
    on mail_config (id);

alter table mail_config
    add constraint mail_config_pk
        primary key (id);

create sequence IF NOT EXISTS mail_config_id_seq;

------------------------------------------------------------------------------------------------------------------------
insert into ext_sys(id, name_kk_cy, name_kk_la, name_ru, name_en, code)
values (nextval('ext_sys_id_seq'),  'АИП "Портал НБ РК"', 'National Bank Portal', 'АИП "Портал НБ РК"', 'National Bank Portal', 'NBPORTAL');

INSERT INTO mail_config(id, ext_sys_id, host, port, username, password, auth_mechanism, auth_ntlm_domain, default_mail_subject, default_mail_text, is_debug_enable, mail_sender)
SELECT nextval('mail_config_id_seq'), e.id, '10.8.1.40', 25, 'corp/applications', 'Hkl@25xcv', 'NTLM', 'BSB', 'Default message from NB Portal', 'Default text for NB Portal', true, 'applications@nationalbank.kz'
FROM ext_sys e
where e.code = 'NBPORTAL';

insert into ext_sys(id, name_kk_cy, name_kk_la, name_ru, name_en, code)
values (nextval('ext_sys_id_seq'),  'ESSP', 'ESSP', 'ESSP', 'ESSP', 'ESSP');

INSERT INTO mail_config(id, ext_sys_id, host, port, username, auth_mechanism, auth_ntlm_domain, default_mail_subject, default_mail_text, is_debug_enable, mail_sender, is_auth_enable)
SELECT nextval('mail_config_id_seq'), e.id, '10.8.1.40', 25, 'essp_portal@nationalbank.kz', 'NTLM', 'nationalbank', 'Default message from ESSP', 'Default text for ESSP', true, 'essp_portal@nationalbank.kz', FALSE
FROM ext_sys e
where e.code = 'ESSP';
------------------------------------------------------------------------------------------------------------------------
--select * from ext_sys;
--select * from mail_config;

CREATE TABLE IF NOT EXISTS public.errors (
                               id int8 NOT NULL GENERATED BY DEFAULT AS IDENTITY,
                               body varchar(1200) NULL,
                               "date" date NULL,
                               email varchar(255) NULL,
                               error varchar(350) NULL,
                               subject varchar(255) NULL,
                               "system" varchar(255) NULL,
                               CONSTRAINT errors_pkey PRIMARY KEY (id)
);