--
--    Copyright 2016-2018 the original author or authors.
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

drop table ColumnComparison if exists;

create table ColumnComparison (
  number1 int not null,
  number2 int not null,
  primary key(number1, number2)
);

insert into ColumnComparison values(1, 11);
insert into ColumnComparison values(2, 10);
insert into ColumnComparison values(3, 9);
insert into ColumnComparison values(4, 8);
insert into ColumnComparison values(5, 7);
insert into ColumnComparison values(6, 6);
insert into ColumnComparison values(7, 5);
insert into ColumnComparison values(8, 4);
insert into ColumnComparison values(9, 3);
insert into ColumnComparison values(10, 2);
insert into ColumnComparison values(11, 1);
