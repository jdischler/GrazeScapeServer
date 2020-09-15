# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table crop_year (
  id                            bigserial not null,
  field_id                      bigint,
  dominant_crop                 varchar(2),
  dominant_ratio                float,
  secondary_crop                varchar(2),
  secondary_ratio               float,
  constraint ck_crop_year_dominant_crop check ( dominant_crop in ('DL','CG','CS','SB','OT','WH','AL','GG','BG','OG','TA','LG','SG','WR')),
  constraint ck_crop_year_secondary_crop check ( secondary_crop in ('DL','CG','CS','SB','OT','WH','AL','GG','BG','OG','TA','LG','SG','WR')),
  constraint pk_crop_year primary key (id)
);

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
  soil_p                        float not null,
  om                            float,
  rotation                      varchar(2),
  tillage                       varchar(3),
  has_cover_crop                boolean,
  on_contour                    boolean,
  graze_dairy_lactating         boolean,
  graze_dairy_non_lactating     boolean,
  graze_beef_cattle             boolean,
  spread_confined_manure_on_pastures boolean,
  rotational_density            float,
  constraint ck_field_rotation check ( rotation in ('CC','CG','D1','D2','DL','PS','PE')),
  constraint ck_field_tillage check ( tillage in ('NT','SCU','SCH','FCH','SMB','FMB')),
  constraint pk_field primary key (id)
);

create table field_geometry (
  id                            bigserial not null,
  field_name                    varchar(255),
  farm_id                       bigint,
  geom                          TEXT,
  constraint pk_field_geometry primary key (id)
);

create table scenario (
  id                            bigserial not null,
  farm_id                       bigint,
  is_baseline                   boolean,
  scenario_name                 varchar(255),
  constraint pk_scenario primary key (id)
);

create index ix_crop_year_field_id on crop_year (field_id);
alter table crop_year add constraint fk_crop_year_field_id foreign key (field_id) references field (id) on delete restrict on update restrict;

create index ix_field_scenario_id on field (scenario_id);
alter table field add constraint fk_field_scenario_id foreign key (scenario_id) references scenario (id) on delete restrict on update restrict;

create index ix_field_geometry_id on field (geometry_id);
alter table field add constraint fk_field_geometry_id foreign key (geometry_id) references field_geometry (id) on delete restrict on update restrict;

create index ix_field_geometry_farm_id on field_geometry (farm_id);
alter table field_geometry add constraint fk_field_geometry_farm_id foreign key (farm_id) references farm (id) on delete restrict on update restrict;

create index ix_scenario_farm_id on scenario (farm_id);
alter table scenario add constraint fk_scenario_farm_id foreign key (farm_id) references farm (id) on delete restrict on update restrict;


# --- !Downs

alter table if exists crop_year drop constraint if exists fk_crop_year_field_id;
drop index if exists ix_crop_year_field_id;

alter table if exists field drop constraint if exists fk_field_scenario_id;
drop index if exists ix_field_scenario_id;

alter table if exists field drop constraint if exists fk_field_geometry_id;
drop index if exists ix_field_geometry_id;

alter table if exists field_geometry drop constraint if exists fk_field_geometry_farm_id;
drop index if exists ix_field_geometry_farm_id;

alter table if exists scenario drop constraint if exists fk_scenario_farm_id;
drop index if exists ix_scenario_farm_id;

drop table if exists crop_year cascade;

drop table if exists farm cascade;

drop table if exists field cascade;

drop table if exists field_geometry cascade;

drop table if exists scenario cascade;

