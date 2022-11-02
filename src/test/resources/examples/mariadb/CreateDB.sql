--
--    Copyright 2016-2022 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       https://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

create table items (
    id int not null,
    description varchar(50) not null,
    amount int not null,
    primary key (id)
);

insert into items values (1, 'Item 1', 101);
insert into items values (2, 'Item 2', 102);
insert into items values (3, 'Item 3', 103);
insert into items values (4, 'Item 4', 104);
insert into items values (5, 'Item 5', 105);
insert into items values (6, 'Item 6', 106);
insert into items values (7, 'Item 7', 107);
insert into items values (8, 'Item 8', 108);
insert into items values (9, 'Item 9', 109);
insert into items values (10, 'Item 10', 110);
insert into items values (11, 'Item 11', 111);
insert into items values (12, 'Item 12', 112);
insert into items values (13, 'Item 13', 113);
insert into items values (14, 'Item 14', 114);
insert into items values (15, 'Item 15', 115);
insert into items values (16, 'Item 16', 116);
insert into items values (17, 'Item 17', 117);
insert into items values (18, 'Item 18', 118);
insert into items values (19, 'Item 19', 119);
insert into items values (20, 'Item 20', 120);
