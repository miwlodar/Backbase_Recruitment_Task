DROP DATABASE IF EXISTS `users_db`;

CREATE DATABASE IF NOT EXISTS `users_db`;
USE `users_db`;


-- Structure for table `users`

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;


-- Dumping data for table `users`

INSERT INTO `users` (first_name, last_name)
VALUES
('John', 'Doe'),
('Jane','Doe'),
('John','Smith'),
('Jane','Smith'),
('Tom','Smith'),
('Jan', 'Kowalski'),
('Jan', 'Kowalski');
