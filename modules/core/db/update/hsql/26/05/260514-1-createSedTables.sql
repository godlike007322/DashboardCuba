-- begin UNTITLED16_SED_DOCUMENT_TYPE
create table UNTITLED16_SED_DOCUMENT_TYPE (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    CODE varchar(50) not null,
    NAME varchar(255) not null,
    DESCRIPTION longvarchar,
    RETENTION_PERIOD_DAYS integer,
    ACTIVE boolean not null,
    --
    primary key (ID)
)^
alter table UNTITLED16_SED_DOCUMENT_TYPE add constraint IDX_UNQ_SED_DOCUMENT_TYPE_CODE unique (CODE)^
-- end UNTITLED16_SED_DOCUMENT_TYPE
-- begin UNTITLED16_SED_BUSINESS_ROUTE
create table UNTITLED16_SED_BUSINESS_ROUTE (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    CODE varchar(50) not null,
    NAME varchar(255) not null,
    DESCRIPTION longvarchar,
    DOCUMENT_TYPE_ID varchar(36),
    ACTIVE boolean not null,
    --
    primary key (ID)
)^
alter table UNTITLED16_SED_BUSINESS_ROUTE add constraint IDX_UNQ_SED_BUSINESS_ROUTE_CODE unique (CODE)^
alter table UNTITLED16_SED_BUSINESS_ROUTE add constraint FK_SED_BUSINESS_ROUTE_DOCUMENT_TYPE foreign key (DOCUMENT_TYPE_ID) references UNTITLED16_SED_DOCUMENT_TYPE(ID)^
create index IDX_SED_BUSINESS_ROUTE_DOCUMENT_TYPE on UNTITLED16_SED_BUSINESS_ROUTE (DOCUMENT_TYPE_ID)^
-- end UNTITLED16_SED_BUSINESS_ROUTE
-- begin UNTITLED16_SED_ROUTE_STAGE
create table UNTITLED16_SED_ROUTE_STAGE (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    ROUTE_ID varchar(36) not null,
    ORDER_NO integer not null,
    NAME varchar(255) not null,
    RESPONSIBLE_ROLE varchar(100) not null,
    SLA_HOURS integer,
    DECISION_POLICY varchar(20) not null,
    INSTRUCTIONS longvarchar,
    --
    primary key (ID)
)^
alter table UNTITLED16_SED_ROUTE_STAGE add constraint FK_SED_ROUTE_STAGE_ROUTE foreign key (ROUTE_ID) references UNTITLED16_SED_BUSINESS_ROUTE(ID)^
create index IDX_SED_ROUTE_STAGE_ROUTE on UNTITLED16_SED_ROUTE_STAGE (ROUTE_ID)^
-- end UNTITLED16_SED_ROUTE_STAGE
-- begin UNTITLED16_SED_DOCUMENT
create table UNTITLED16_SED_DOCUMENT (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    REGISTRATION_NUMBER varchar(50) not null,
    SUBJECT varchar(500) not null,
    DOCUMENT_TYPE_ID varchar(36) not null,
    ROUTE_ID varchar(36),
    STATUS varchar(30) not null,
    PRIORITY varchar(20) not null,
    INITIATOR varchar(255) not null,
    COUNTERPARTY varchar(255),
    AMOUNT decimal(19, 2),
    DUE_DATE date,
    SIGNED_AT timestamp,
    SUMMARY longvarchar,
    --
    primary key (ID)
)^
alter table UNTITLED16_SED_DOCUMENT add constraint IDX_UNQ_SED_DOCUMENT_REG_NUMBER unique (REGISTRATION_NUMBER)^
alter table UNTITLED16_SED_DOCUMENT add constraint FK_SED_DOCUMENT_DOCUMENT_TYPE foreign key (DOCUMENT_TYPE_ID) references UNTITLED16_SED_DOCUMENT_TYPE(ID)^
alter table UNTITLED16_SED_DOCUMENT add constraint FK_SED_DOCUMENT_ROUTE foreign key (ROUTE_ID) references UNTITLED16_SED_BUSINESS_ROUTE(ID)^
create index IDX_SED_DOCUMENT_DOCUMENT_TYPE on UNTITLED16_SED_DOCUMENT (DOCUMENT_TYPE_ID)^
create index IDX_SED_DOCUMENT_ROUTE on UNTITLED16_SED_DOCUMENT (ROUTE_ID)^
-- end UNTITLED16_SED_DOCUMENT
-- begin UNTITLED16_SED_DOCUMENT_ROUTE_TASK
create table UNTITLED16_SED_DOCUMENT_ROUTE_TASK (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    DOCUMENT_ID varchar(36) not null,
    STAGE_ID varchar(36),
    ASSIGNEE_ROLE varchar(100) not null,
    ASSIGNEE_NAME varchar(255),
    STATUS varchar(30) not null,
    STARTED_AT timestamp,
    COMPLETED_AT timestamp,
    COMMENT_TEXT longvarchar,
    --
    primary key (ID)
)^
alter table UNTITLED16_SED_DOCUMENT_ROUTE_TASK add constraint FK_SED_TASK_DOCUMENT foreign key (DOCUMENT_ID) references UNTITLED16_SED_DOCUMENT(ID)^
alter table UNTITLED16_SED_DOCUMENT_ROUTE_TASK add constraint FK_SED_TASK_STAGE foreign key (STAGE_ID) references UNTITLED16_SED_ROUTE_STAGE(ID)^
create index IDX_SED_TASK_DOCUMENT on UNTITLED16_SED_DOCUMENT_ROUTE_TASK (DOCUMENT_ID)^
create index IDX_SED_TASK_STAGE on UNTITLED16_SED_DOCUMENT_ROUTE_TASK (STAGE_ID)^
-- end UNTITLED16_SED_DOCUMENT_ROUTE_TASK
-- begin UNTITLED16_SED_DOCUMENT_ATTACHMENT
create table UNTITLED16_SED_DOCUMENT_ATTACHMENT (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    DOCUMENT_ID varchar(36) not null,
    FILE_NAME varchar(255) not null,
    MIME_TYPE varchar(100) not null,
    SIZE_BYTES bigint,
    DESCRIPTION longvarchar,
    --
    primary key (ID)
)^
alter table UNTITLED16_SED_DOCUMENT_ATTACHMENT add constraint FK_SED_ATTACHMENT_DOCUMENT foreign key (DOCUMENT_ID) references UNTITLED16_SED_DOCUMENT(ID)^
create index IDX_SED_ATTACHMENT_DOCUMENT on UNTITLED16_SED_DOCUMENT_ATTACHMENT (DOCUMENT_ID)^
-- end UNTITLED16_SED_DOCUMENT_ATTACHMENT
