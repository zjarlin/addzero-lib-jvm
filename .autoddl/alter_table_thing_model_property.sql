create table if not exists "thing_model_property" (
    `id` BIGINT primary key,
    `create_by` BIGINT ,
    `update_by` BIGINT ,
    `create_time` timestamp ,
    `update_time` timestamp ,
    `productId` varchar  (255)
   
);
comment on table "thing_model_property" is '物模型属性实体类，属于产品的一部分';
comment on column "thing_model_property".`id` is '主键';
comment on column "thing_model_property".`create_by` is '创建者';
comment on column "thing_model_property".`create_time` is '创建时间';
comment on column "thing_model_property".`update_by` is '更新者';
comment on column "thing_model_property".`update_time` is '更新时间';
comment on column "thing_model_property".`productId` is '所属产品';

alter table "thing_model_property" add column `product_id` varchar(255);
comment on column "thing_model_property".`product_id` is '所属产品';