create table UNTITLED16_RECENT_DOC (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    USER_ID varchar(36) not null,
    ENTITY_NAME varchar(100) not null,
    ENTITY_ID varchar(36) not null,
    CAPTION varchar(255),
    VISITED_TS timestamp not null,
    --
    primary key (ID)
);