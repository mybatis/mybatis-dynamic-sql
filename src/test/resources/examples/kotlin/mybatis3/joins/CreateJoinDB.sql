--
--    Copyright 2016-2019 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

drop table OrderLine if exists;
drop table ItemMaster if exists;
drop table OrderDetail if exists;
drop table OrderMaster if exists;
drop table User if exists;

create table User (
  user_id int not null,
  user_name varchar(30) not null,
  parent_id int null,
  primary key (user_id)
);

create table OrderMaster (
   order_id int not null,
   order_date date not null,
   primary key(order_id)
);

create table OrderDetail (
   order_id int not null,
   line_number int not null,
   description varchar(30) not null,
   quantity int not null,
   primary key(order_id, line_number)
);

create table ItemMaster (
   item_id int not null,
   description varchar(30) not null,
   primary key(item_id)
);

create table OrderLine (
   order_id int not null,
   item_id int not null,
   line_number int not null,
   quantity int not null,
   primary key(order_id, item_id)
);

insert into OrderMaster(order_id, order_date) values(1, '2017-01-17');
insert into OrderDetail(order_id, line_number, Description, quantity) values(1, 1, 'Tennis Ball', 3);
insert into OrderDetail(order_id, line_number, Description, quantity) values(1, 2, 'Tennis Racket', 1);

insert into OrderMaster (order_id, order_date) values(2, '2017-01-18');
insert into OrderDetail(order_id, line_number, Description, quantity) values(2, 1, 'Football', 2);

insert into ItemMaster(item_id, description) values(22, 'Helmet');
insert into ItemMaster(item_id, description) values(33, 'First Base Glove');
insert into ItemMaster(item_id, description) values(44, 'Outfield Glove');
insert into ItemMaster(item_id, description) values(55, 'Catcher Glove');

insert into OrderLine(order_id, item_id, line_number, quantity) values(1, 22, 1, 1);
insert into OrderLine(order_id, item_id, line_number, quantity) values(1, 33, 1, 1);
insert into OrderLine(order_id, item_id, line_number, quantity) values(2, 22, 1, 1);
insert into OrderLine(order_id, item_id, line_number, quantity) values(2, 44, 2, 1);
insert into OrderLine(order_id, item_id, line_number, quantity) values(2, 66, 3, 6);

insert into User(user_id, user_name) values(1, 'Fred');
insert into User(user_id, user_name) values(2, 'Barney');
insert into User(user_id, user_name, parent_id) values(3, 'Pebbles', 1);
insert into User(user_id, user_name, parent_id) values(4, 'Bamm Bamm', 2);
