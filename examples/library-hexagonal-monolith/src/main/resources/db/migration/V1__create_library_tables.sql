create table books (
    id bigserial primary key,
    title varchar(200) not null,
    author varchar(200) not null,
    description varchar(2000),
    total_copies integer not null check (total_copies > 0),
    available_copies integer not null check (available_copies between 0 and total_copies),
    created_at timestamptz not null default current_timestamp
);

create table reviews (
    id bigserial primary key,
    book_id bigint not null references books(id) on delete cascade,
    user_id varchar(200) not null,
    comment varchar(2000) not null,
    rating smallint not null check (rating between 1 and 5),
    created_at timestamptz not null default current_timestamp,
    unique (book_id, user_id)
);

create table rentals (
    id bigserial primary key,
    book_id bigint not null references books(id),
    user_id varchar(200) not null,
    status varchar(30) not null check (status in ('RENTED', 'RETURNED')),
    rented_at timestamptz not null default current_timestamp,
    returned_at timestamptz
);

create index idx_reviews_book_id on reviews(book_id);
create index idx_rentals_book_id_status on rentals(book_id, status);
