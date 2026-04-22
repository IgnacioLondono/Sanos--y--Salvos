-- =====================================================================
-- Sanos y Salvos - Script de inicializacion de bases de datos
-- Patron: Database per Service (un schema independiente por microservicio)
-- Las entidades JPA se crean automaticamente via ddl-auto=update;
-- este script asegura que los schemas y los privilegios existan antes
-- de que arranquen los microservicios.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS db_iam       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_pets      CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_reports   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_geo       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_media     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_matching  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_capacity  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_audit     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usuario de aplicacion comun (tiene acceso unicamente a los schemas anteriores)
CREATE USER IF NOT EXISTS 'sanos'@'%' IDENTIFIED BY 'sanos_pwd';

GRANT ALL PRIVILEGES ON db_iam.*      TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_pets.*     TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_reports.*  TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_geo.*      TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_media.*    TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_matching.* TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_capacity.* TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_audit.*    TO 'sanos'@'%';

FLUSH PRIVILEGES;
