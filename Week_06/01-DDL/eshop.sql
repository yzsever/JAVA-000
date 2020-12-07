/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2020/11/25 14:20:41                          */
/*==============================================================*/


drop table if exists cart;

drop table if exists cart_x_commodity;

drop table if exists commodity;

drop table if exists "order";

drop table if exists supplier;

drop table if exists user;

/*==============================================================*/
/* Table: cart                                                  */
/*==============================================================*/
create table cart
(
   cart_id              bigint not null,
   user_id              bigint,
   total_price          double,
   favorate_price       double,
   is_deleted           bigint,
   updater_id           bigint,
   update_time          bigint,
   creator_id           bigint,
   create_time          bigint,
   primary key (cart_id)
);

/*==============================================================*/
/* Table: cart_x_commodity                                      */
/*==============================================================*/
create table cart_x_commodity
(
   cart_x_commodity_id  bigint,
   cart_id              bigint,
   commodity_id         bigint,
   is_deleted           smallint,
   creator_id           bigint,
   create_time          bigint,
   updater_id           bigint,
   "update_ time"       bigint
);

/*==============================================================*/
/* Table: commodity                                             */
/*==============================================================*/
create table commodity
(
   commodity_id         bigint not null,
   name                 varchar(128),
   description          varchar(1024),
   category_id          bigint,
   weight               double,
   weight_unit          int,
   price                double,
   supplier_id          bigint,
   is_delete            smallint,
   creator_id           bigint,
   create_time          bigint,
   updater_id           bigint,
   update_time          bigint,
   primary key (commodity_id)
);

/*==============================================================*/
/* Table: "order"                                               */
/*==============================================================*/
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
   `order_id`             bigint NOT NULL,
   `user_id`              bigint,
   `prices`               int,
   `status`               smallint,
   `is_deleted`           smallint,
   `crteator_id`          bigint,
   `create_time`          bigint,
   `updater_id`           bigint,
   `update_time`          bigint,
   `address`              varchar(128),
   `phone`                varchar(32),
   `province`             smallint,
   `city`                 smallint,
   PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*==============================================================*/
/* Table: supplier                                              */
/*==============================================================*/
create table supplier
(
   supplier_id          bigint not null,
   name                 varchar(64),
   contact_no           varchar(32),
   contact_name         varchar(64),
   identify             varchar(64),
   is_deleted           smallint,
   creator_id           bigint,
   create_time          bigint,
   updater_id           bigint,
   update_time          bigint,
   primary key (supplier_id)
);

/*==============================================================*/
/* Table: user                                                  */
/*==============================================================*/
create table user
(
   user_id              bigint not null,
   username             varchar(64),
   password             varchar(128),
   nickname             varchar(128),
   identify             varchar(64),
   is_deleted           smallint,
   creator_id           bigint,
   create_time          bigint,
   updater_id           bigint,
   update_time          bigint,
   primary key (user_id)
);

