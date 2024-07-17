DROP TABLE IF EXISTS vaxapp.forum_answer;
DROP TABLE IF EXISTS vaxapp.forum_question;
DROP TABLE IF EXISTS vaxapp.notification;
DROP TABLE IF EXISTS vaxapp.vaccine;
DROP TABLE IF EXISTS vaxapp.appointment;
DROP TABLE IF EXISTS vaxapp.appointment_slot;
DROP TABLE IF EXISTS vaxapp.vaccine_centre;
DROP TABLE IF EXISTS vaxapp.users;

create table vaxapp.users
(
    id            int auto_increment
        primary key,
    user_pps      varchar(255) null,
    address       varchar(255) null,
    admin         int          not null,
    date_of_birth varchar(255) null,
    email         varchar(255) null,
    full_name     varchar(255) null,
    gender        varchar(255) null,
    nationality   varchar(255) null,
    phone_number  varchar(255) null,
    constraint UK_ne52mbkn6bvjvcancspi6mtui
        unique (user_pps)
);

create table vaxapp.forum_question
(
    id             int auto_increment
        primary key,
    date_submitted varchar(255) null,
    details        varchar(255) null,
    title          varchar(255) null,
    user_id        int          null,
    constraint FK6iiud5i6j0o5s4tn6aesdkhfd
        foreign key (user_id) references vaxapp.users (id)
);

create table vaxapp.forum_answer
(
    id                int auto_increment
        primary key,
    body              varchar(255) null,
    date_dubmitted    varchar(255) null,
    admin_id          int          null,
    forum_question_id int          not null,
    constraint FKs8s67hshh1bf91jnw7aa3rjcj
        foreign key (forum_question_id) references vaxapp.forum_question (id),
    constraint FKsm8jovg1cdo4tijnx0td7305y
        foreign key (admin_id) references vaxapp.users (id)
);

create table vaxapp.notification
(
    id                int auto_increment
        primary key,
    date              date         null,
    is_read           int          not null,
    message           varchar(255) null,
    time              time         null,
    type              int          null,
    url               varchar(255) null,
    user_to_notify_id int          null,
    constraint FKrcc1d41kcbmkfbbq0u8dku3cr
        foreign key (user_to_notify_id) references vaxapp.users (id)
);

create table vaxapp.vaccine_centre
(
    id   int auto_increment
        primary key,
    name varchar(255) null
);

create table vaxapp.appointment
(
    id                int auto_increment
        primary key,
    date              date         null,
    status            varchar(255) null,
    time              time         null,
    user_id           int          null,
    vaccine_centre_id int          null,
    constraint FK7bo52i6usixwb7ira9l16y3bu
        foreign key (user_id) references vaxapp.users (id),
    constraint FKbdwbqvnyflcplcahpdcxic0ua
        foreign key (vaccine_centre_id) references vaxapp.vaccine_centre (id)
);

create table vaxapp.appointment_slot
(
    id                int auto_increment
        primary key,
    date              date null,
    start_time        time null,
    vaccine_centre_id int  null,
    constraint FKbipw5arh7rc89jg5l9tk9tg93
        foreign key (vaccine_centre_id) references vaxapp.vaccine_centre (id)
);

create table vaxapp.vaccine
(
    id                int auto_increment
        primary key,
    date_received     date         null,
    type              varchar(255) null,
    admin_id          int          null,
    user_id           int          null,
    vaccine_centre_id int          null,
    constraint FK2n1jvw3n30bw4w5rgvsdt9f0e
        foreign key (user_id) references vaxapp.users (id),
    constraint FKepicxryr0lousvihe0v5uc7vw
        foreign key (admin_id) references vaxapp.users (id),
    constraint FKk0v6bt2bkypw2vebt8vk8cp2j
        foreign key (vaccine_centre_id) references vaxapp.vaccine_centre (id)
);