--
--    Copyright 2016-2017 the original author or authors.
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

drop table Person if exists;

create table Person (
   person_id int not null,
   first_name varchar(20) not null,
   last_name varchar(20) not null,
   gender varchar(20) not null,
   human_flag char(1) not null,
   age int not null,
   primary key(person_id)
);

insert into Person (person_id, first_name, last_name, gender, human_flag, age)
values (1, 'Fred', 'Flintstone', 'Male', 'Y', 47);

insert into Person (person_id, first_name, last_name, gender, human_flag, age)
values(2, 'Wilma', 'Flintstone', 'Female', 'Y', 42);

insert into Person (person_id, first_name, last_name, gender, human_flag, age)
values(3, 'Barney', 'Rubble', 'Male', 'Y', 45);

insert into Person (person_id, first_name, last_name, gender, human_flag, age)
values(4, 'Betty', 'Rubble', 'Female', 'Y', 37);

insert into Person (person_id, first_name, last_name, gender, human_flag, age)
values(5, 'Bamm Bamm', 'Rubble', 'Male', 'Y', 3);

insert into Person (person_id, first_name, last_name, gender, human_flag, age)
values(6, 'Pebbles', 'Flintstone', 'Female', 'Y', 2);

insert into Person (person_id, first_name, last_name, gender, human_flag, age)
values(7, 'Dino', 'Flintstone', 'Male', 'N', 5);
