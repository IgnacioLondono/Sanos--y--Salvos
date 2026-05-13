-- =====================================================================
-- Sanos y Salvos - Script de inicializacion de bases de datos
-- Patron: una sola base compartida para que todo aparezca junto en phpMyAdmin.
-- Las entidades JPA se crean automaticamente via ddl-auto=update;
-- este script asegura que la base y los privilegios existan antes
-- de que arranquen los microservicios.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS sanosysalvos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usuario de aplicacion comun
CREATE USER IF NOT EXISTS 'sanos'@'%' IDENTIFIED BY 'sanos_pwd';

GRANT ALL PRIVILEGES ON sanosysalvos.* TO 'sanos'@'%';

FLUSH PRIVILEGES;
