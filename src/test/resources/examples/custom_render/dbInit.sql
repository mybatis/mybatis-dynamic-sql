create table JsonTest (
  id int not null,
  description varchar(30) not null,
  info json null,
  primary key (id)
);
