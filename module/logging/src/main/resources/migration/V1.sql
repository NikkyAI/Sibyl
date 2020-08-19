-- auto-generated definition
create table logs
(
    id        serial    not null
        constraint logs_pk
            primary key,
    gateway   text      not null,
    timestamp timestamp not null,
    username  text      not null,
    userid    text      not null,
    text      text      not null,
    incoming  boolean   not null,
    event     text,
    protocol  text
);

alter table logs
    owner to sibyl;