# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "activity" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"date" DATE,"place" VARCHAR(254),"activity_type" BIGINT NOT NULL);
create table "activity_type" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"mnemonic" VARCHAR(254) NOT NULL,"description" VARCHAR(254) NOT NULL);
create table "animator" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"first_name" VARCHAR(254) NOT NULL,"last_name" VARCHAR(254) NOT NULL,"mobile_phone" VARCHAR(254),"landline" VARCHAR(254),"email" VARCHAR(254),"street" VARCHAR(254),"city" VARCHAR(254),"bank_account" VARCHAR(254),"year_started_volunteering" INTEGER,"is_core" BOOLEAN NOT NULL,"birthdate" DATE);
create table "child" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"first_name" VARCHAR(254) NOT NULL,"last_name" VARCHAR(254) NOT NULL,"mobile_phone" VARCHAR(254),"landline" VARCHAR(254),"street" VARCHAR(254),"city" VARCHAR(254),"birth_date" DATE,"medical_file_checked" DATE);
create table "child_to_activity" ("child_id" BIGINT NOT NULL,"activity_id" BIGINT NOT NULL);
alter table "child_to_activity" add constraint "child_to_activity_pk" primary key("child_id","activity_id");
create table "medical_file" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"first_name" VARCHAR(254) NOT NULL,"last_name" VARCHAR(254) NOT NULL,"street" VARCHAR(254) NOT NULL,"city" VARCHAR(254) NOT NULL,"blood_type" VARCHAR(254),"is_male" BOOLEAN NOT NULL,"allergic_to_dust" BOOLEAN NOT NULL,"allergic_to_face_paint" BOOLEAN NOT NULL,"allergic_to_bees" BOOLEAN NOT NULL,"other_allergies" VARCHAR(254),"has_asthma" BOOLEAN NOT NULL,"has_hay_fever" BOOLEAN NOT NULL,"has_epilepsy" BOOLEAN NOT NULL,"has_diabetes" BOOLEAN NOT NULL,"other_conditions" VARCHAR(254),"extra_information" VARCHAR(254),"birthdate" DATE);
alter table "activity" add constraint "fk_act_type" foreign key("activity_type") references "activity_type"("id") on update NO ACTION on delete NO ACTION;
alter table "child_to_activity" add constraint "activity_fk" foreign key("activity_id") references "activity"("id") on update NO ACTION on delete NO ACTION;
alter table "child_to_activity" add constraint "child_fk" foreign key("child_id") references "child"("id") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "child_to_activity" drop constraint "activity_fk";
alter table "child_to_activity" drop constraint "child_fk";
alter table "activity" drop constraint "fk_act_type";
drop table "medical_file";
alter table "child_to_activity" drop constraint "child_to_activity_pk";
drop table "child_to_activity";
drop table "child";
drop table "animator";
drop table "activity_type";
drop table "activity";

