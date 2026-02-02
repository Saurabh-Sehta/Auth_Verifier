create database auth_app;

use auth_app;

show tables;

drop table email_verify_table;

select * from tbl_users;

select * from email_entity;
truncate email_entity;

delete  from tbl_users where email = "saurabhsehta@gmail.com";

truncate tbl_users;
 