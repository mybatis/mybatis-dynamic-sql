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

drop table GeneratedAlways if exists;

create table GeneratedAlways (
   id int not null,
   first_name varchar(30) not null,
   last_name varchar(30) not null,
   age integer null,
   full_name varchar(60) generated always as (first_name || ' ' || last_name),
   primary key(id)
);

insert into GeneratedAlways(id, first_name, last_name) values(1, 'Fred', 'Flintstone');
insert into GeneratedAlways(id, first_name, last_name) values(2, 'Wilma', 'Flintstone');
insert into GeneratedAlways(id, first_name, last_name) values(3, 'Pebbles', 'Flintstone');
insert into GeneratedAlways(id, first_name, last_name) values(4, 'Barney', 'Rubble');
insert into GeneratedAlways(id, first_name, last_name) values(5, 'Betty', 'Rubble');
insert into GeneratedAlways(id, first_name, last_name) values(6, 'Bamm Bamm', 'Rubble');
