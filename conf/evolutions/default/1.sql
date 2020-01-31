# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table account (
  email                         varchar(255) not null,
  password                      varchar(255),
  organization                  varchar(255),
  password_salt                 varchar(255),
  admin                         boolean default false not null,
  access_flags                  integer not null,
  constraint pk_account primary key (email)
);


# --- !Downs

drop table if exists account;

