-- begin UNTITLED16_NOTES
create table UNTITLED16_NOTES (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    TEXT varchar(1000),
    NOTE_DATE date,
    --
    primary key (ID)
)^
-- end UNTITLED16_NOTES
-- begin UNTITLED16_NEWS
create table UNTITLED16_NEWS (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    TITLE varchar(255) not null,
    SHORT_TEXT longvarchar,
    FULL_TEXT longvarchar,
    PUBLISH_DATE timestamp,
    --
    primary key (ID)
)^
-- end UNTITLED16_NEWS
-- begin UNTITLED16_RECENT_DOC
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
)^
-- end UNTITLED16_RECENT_DOC
