--liquibase formatted sql

--changeset ybonnel:1
create table users (
  id  uuid,
  login text,
  password text,
  salt text,
  PRIMARY KEY (id)
);
--rollback drop table if exists users;



