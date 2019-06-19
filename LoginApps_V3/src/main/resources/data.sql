INSERT INTO `user` (`user_id`, `enabled`, `username`,`password`,`password_confirm`)
 VALUES(1,1,'singhavinash857@gmail.com','$2a$10$AOL./V.Oh4i/8yTFcrc.WeZnAAdsOeGbD8WIQKoYbf6kgdmh/eEiC','$2a$10$AOL./V.Oh4i/8yTFcrc.WeZnAAdsOeGbD8WIQKoYbf6kgdmh/eEiC');

INSERT INTO `role` (`role_id`, `role_name`) VALUES(1,'ADMIN');

INSERT INTO `user_roles` (`users_user_id`, `roles_role_id`) VALUES(1,1);


INSERT INTO `role` (`role_id`, `role_name`) VALUES(1,'ADMIN');
INSERT INTO `role` (`role_id`, `role_name`) VALUES(2,'MANAGER');
INSERT INTO `role` (`role_id`, `role_name`) VALUES(3,'EMPLOYEE');
