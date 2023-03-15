create table users(
    id serial primary key,
    username varchar(10) not null,
    password varchar(100) not null
);

create table items(
    item_id serial primary key,
    user_id int not null references users(id) on delete cascade,
    text varchar(1000),
    marked boolean not null
);