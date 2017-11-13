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

drop table AnimalData if exists;
drop table AnimalDataCopy if exists;

create table AnimalData (
  id int not null,
  animal_name varchar(50) null,
  brain_weight double not null,
  body_weight double not null,
  primary key(id)
);

-- for the insert with select tests
create table AnimalDataCopy (
  id int not null,
  animal_name varchar(50) null,
  brain_weight double not null,
  body_weight double not null,
  primary key(id)
);

--
-- the following records originally came from the Animals2 dataset hosted here:
-- https://vincentarelbundock.github.io/Rdatasets/datasets.html
--
insert into AnimalData values(1, 'Lesser short-tailed shrew', 0.005, 0.14);
insert into AnimalData values(2,'Little brown bat',0.01,0.25);
insert into AnimalData values(3,'Big brown bat',0.023,0.3);
insert into AnimalData values(4,'Mouse',0.023,0.4);
insert into AnimalData values(5,'Musk shrew',0.048,0.33);
insert into AnimalData values(6,'Star-nosed mole',0.06,1);
insert into AnimalData values(7,'E. American mole',0.075,1.2);
insert into AnimalData values(8,'Ground squirrel',0.101,4);
insert into AnimalData values(9,'Tree shrew',0.104,2.5);
insert into AnimalData values(10,'Golden hamster',0.12,1);
insert into AnimalData values(11,'Mole',0.122,3);
insert into AnimalData values(12,'Galago',0.2,5);
insert into AnimalData values(13,'Rat',0.28,1.9);
insert into AnimalData values(14,'Chinchilla',0.425,6.4);
insert into AnimalData values(15,'Owl monkey',0.48,15.5);
insert into AnimalData values(16,'Desert hedgehog',0.55,2.4);
insert into AnimalData values(17,'Rock hyrax-a',0.75,12.3);
insert into AnimalData values(18,'European hedgehog',0.785,3.5);
insert into AnimalData values(19,'Tenrec',0.9,2.6);
insert into AnimalData values(20,'Artic ground squirrel',0.92,5.7);
insert into AnimalData values(21,'African giant pouched rat',1,6.6);
insert into AnimalData values(22,'Guinea pig',1.04,5.5);
insert into AnimalData values(23,'Mountain beaver',1.35,8.1);
insert into AnimalData values(24,'Slow loris',1.4,12.5);
insert into AnimalData values(25,'Genet',1.41,17.5);
insert into AnimalData values(26,'Phalanger',1.62,11.4);
insert into AnimalData values(27,'N.A. opossum',1.7,6.3);
insert into AnimalData values(28,'Tree hyrax',2,12.3);
insert into AnimalData values(29,'Rabbit',2.5,12.1);
insert into AnimalData values(30,'Echidna',3,25);
insert into AnimalData values(31,'Cat',3.3,25.6);
insert into AnimalData values(32,'Artic fox',3.385,44.5);
insert into AnimalData values(33,'Water opossum',3.5,3.9);
insert into AnimalData values(34,'Nine-banded armadillo',3.5,10.8);
insert into AnimalData values(35,'Rock hyrax-b',3.6,21);
insert into AnimalData values(36,'Yellow-bellied marmot',4.05,17);
insert into AnimalData values(37,'Verbet',4.19,58);
insert into AnimalData values(38,'Red fox',4.235,50.4);
insert into AnimalData values(39,'Raccoon',4.288,39.2);
insert into AnimalData values(40,'Rhesus monkey',6.8,179);
insert into AnimalData values(41,'Potar monkey',10,115);
insert into AnimalData values(42,'Baboon',10.55,179.5);
insert into AnimalData values(43,'Roe deer',14.83,98.2);
insert into AnimalData values(44,'Goat',27.66,115);
insert into AnimalData values(45,'Kangaroo',35,56);
insert into AnimalData values(46,'Grey wolf',36.33,119.5);
insert into AnimalData values(47,'Chimpanzee',52.16,440);
insert into AnimalData values(48,'Sheep',55.5,175);
insert into AnimalData values(49,'Giant armadillo',60,81);
insert into AnimalData values(50,'Human',62,1320);
insert into AnimalData values(51,'Grey seal',85,325);
insert into AnimalData values(52,'Jaguar',100,157);
insert into AnimalData values(53,'Brazilian tapir',160,169);
insert into AnimalData values(54,'Donkey',187.1,419);
insert into AnimalData values(55,'Pig',192,180);
insert into AnimalData values(56,'Gorilla',207,406);
insert into AnimalData values(57,'Okapi',250,490);
insert into AnimalData values(58,'Cow',465,423);
insert into AnimalData values(59,'Horse',521,655);
insert into AnimalData values(60,'Giraffe',529,680);
insert into AnimalData values(61,'Asian elephant',2547,4603);
insert into AnimalData values(62,'African elephant',6654,5712);
insert into AnimalData values(63,'Triceratops',9400,70);
insert into AnimalData values(64,'Dipliodocus',11700,50);
insert into AnimalData values(65,'Brachiosaurus',87000,154.5);
