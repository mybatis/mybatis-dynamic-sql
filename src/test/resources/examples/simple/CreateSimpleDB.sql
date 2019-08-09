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

drop table Address if exists;
drop table Person if exists;

create table Address (
   address_id int not null,
   street_address varchar(50) not null,
   city varchar(20) not null,
   state varchar(2) not null,
   primary key(address_id)
);

create table Person (
   id int not null,
   first_name varchar(30) not null,
   last_name varchar(30) not null,
   birth_date date not null,
   employed varchar(3) not null,
   occupation varchar(30) null,
   address_id int not null,
   primary key(id)
);

insert into Address (address_id, street_address, city, state)
values(1, '123 Main Street', 'Bedrock', 'IN');

insert into Address (address_id, street_address, city, state)
values(2, '456 Main Street', 'Bedrock', 'IN');

insert into Person values(1, 'Fred', 'Flintstone', '1935-02-01', 'Yes', 'Brontosaurus Operator', 1);

insert into Person values(2, 'Wilma', 'Flintstone', '1940-02-01', 'Yes', 'Accountant', 1);

insert into Person(id, first_name, last_name, birth_date, employed, address_id)
values(3, 'Pebbles', 'Flintstone', '1960-05-06', 'No', 1);

insert into Person values(4, 'Barney', 'Rubble', '1937-02-01', 'Yes', 'Brontosaurus Operator', 2);

insert into Person values(5, 'Betty', 'Rubble', '1943-02-01', 'Yes', 'Engineer', 2);

insert into Person(id, first_name, last_name, birth_date, employed, address_id)
values(6, 'Bamm Bamm', 'Rubble', '1963-07-08', 'No', 2);
