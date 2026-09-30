create table rentals (
    id bigserial primary key,
    book_id bigint not null,
    user_id varchar(200) not null,
    status varchar(30) not null check (status in ('RENTED', 'RETURNED')),
    rented_at timestamptz not null default current_timestamp,
    returned_at timestamptz
);

create index idx_rentals_book_id_status on rentals(book_id, status);
