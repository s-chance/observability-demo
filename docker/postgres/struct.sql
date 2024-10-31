drop table if exists short_urls cascade;
drop table if exists click_stats;

create table short_urls
(
    id           serial primary key,
    short_code   varchar(255) not null,
    original_url text         not null,
    create_time  timestamp with time zone default current_timestamp,
    expire_time  timestamp with time zone,
    click_count  integer                  default 0,
    status       varchar(50)              default 'ACTIVE'
);

create unique index idx_short_code on short_urls (short_code);
create index idx_original_url on short_urls (original_url);
create index idx_creation_date on short_urls (create_time);
create index idx_expiration_date on short_urls (expire_time);

create table click_stats
(
    click_id   serial primary key,
    short_code varchar(255) not null,
    click_time timestamp with time zone default current_timestamp,
    user_ip    inet,
    user_agent varchar(255),
    referrer   text,
    constraint fk_short_code
        foreign key (short_code)
            references short_urls (short_code)
            on delete cascade
);

create index idx_click_stats_short_code on click_stats (short_code);
create index idx_click_stats_click_date on click_stats (click_time);