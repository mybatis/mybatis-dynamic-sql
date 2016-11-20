drop table SimpleTable if exists;

create table SimpleTable (
   id int not null,
   first_name varchar(30) not null,
   last_name varchar(30) not null,
   birth_date date not null, 
   occupation varchar(30) null,
   primary key(id)
);

insert into SimpleTable values(1, 'Fred', 'Flintstone', '1935-02-01', 'Brontosaurus Operator');
insert into SimpleTable(id, first_name, last_name, birth_date) values(2, 'Wilma', 'Flintstone', '1940-02-01');
insert into SimpleTable values(3, 'Barney', 'Rubble', '1937-02-01', 'Brontosaurus Operator');
insert into SimpleTable(id, first_name, last_name, birth_date) values(4, 'Betty', 'Rubble', '1943-02-01');
