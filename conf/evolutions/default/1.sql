# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table farm (
  id                            bigserial not null,
  farm_name                     varchar(255),
  farm_owner                    varchar(255),
  farm_address                  varchar(255),
  location                      TEXT,
  constraint pk_farm primary key (id)
);

create table field (
  id                            bigserial not null,
  scenario_id                   bigint,
  geometry_id                   bigint,
  p_override                    float,
  om_override                   float,
  tillage                       varchar(2),
  tillage_season                varchar(2),
  constraint ck_field_tillage check ( tillage in ('NT','CD','MP')),
  constraint ck_field_tillage_season check ( tillage_season in ('SP','FL')),
  constraint pk_field primary key (id)
);

create table field_geometry (
  id                            bigserial not null,
  farm_id                       bigint,
  geom                          TEXT,
  field_p                       float not null,
  field_om                      float,
  constraint pk_field_geometry primary key (id)
);

create table rotation (
  id                            bigserial not null,
  name                          varchar(255),
  definition                    varchar[],
  constraint pk_rotation primary key (id)
);

create table scenario (
  id                            bigserial not null,
  farm_id                       bigint,
  is_baseline                   boolean,
  scenario_name                 varchar(255),
  constraint pk_scenario primary key (id)
);

create index ix_field_scenario_id on field (scenario_id);
alter table field add constraint fk_field_scenario_id foreign key (scenario_id) references scenario (id) on delete restrict on update restrict;

create index ix_field_geometry_id on field (geometry_id);
alter table field add constraint fk_field_geometry_id foreign key (geometry_id) references field_geometry (id) on delete restrict on update restrict;

create index ix_field_geometry_farm_id on field_geometry (farm_id);
alter table field_geometry add constraint fk_field_geometry_farm_id foreign key (farm_id) references farm (id) on delete restrict on update restrict;

create index ix_scenario_farm_id on scenario (farm_id);
alter table scenario add constraint fk_scenario_farm_id foreign key (farm_id) references farm (id) on delete restrict on update restrict;


# --- !Downs

alter table if exists field drop constraint if exists fk_field_scenario_id;
drop index if exists ix_field_scenario_id;

alter table if exists field drop constraint if exists fk_field_geometry_id;
drop index if exists ix_field_geometry_id;

alter table if exists field_geometry drop constraint if exists fk_field_geometry_farm_id;
drop index if exists ix_field_geometry_farm_id;

alter table if exists scenario drop constraint if exists fk_scenario_farm_id;
drop index if exists ix_scenario_farm_id;

drop table if exists farm cascade;

drop table if exists field cascade;

drop table if exists field_geometry cascade;

drop table if exists rotation cascade;

drop table if exists scenario cascade;

