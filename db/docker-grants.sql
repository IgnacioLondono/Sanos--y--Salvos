-- Reaplica bases y permisos (volumenes MySQL creados antes de db_foro).
-- Seguro ejecutar varias veces. Usado por encender-todo.bat y scripts/fix-docker-mysql.ps1

CREATE DATABASE IF NOT EXISTS db_iam CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_pets CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_reports CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_geo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_media CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_matching CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_capacity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_audit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_foro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS sanosysalvos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'sanos'@'%' IDENTIFIED BY 'sanos_pwd';
CREATE USER IF NOT EXISTS 'sanos'@'localhost' IDENTIFIED BY 'sanos_pwd';

GRANT ALL PRIVILEGES ON db_iam.* TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_pets.* TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_reports.* TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_geo.* TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_media.* TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_matching.* TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_capacity.* TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_audit.* TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON db_foro.* TO 'sanos'@'%';
GRANT ALL PRIVILEGES ON sanosysalvos.* TO 'sanos'@'%';

GRANT ALL PRIVILEGES ON db_iam.* TO 'sanos'@'localhost';
GRANT ALL PRIVILEGES ON db_pets.* TO 'sanos'@'localhost';
GRANT ALL PRIVILEGES ON db_reports.* TO 'sanos'@'localhost';
GRANT ALL PRIVILEGES ON db_geo.* TO 'sanos'@'localhost';
GRANT ALL PRIVILEGES ON db_media.* TO 'sanos'@'localhost';
GRANT ALL PRIVILEGES ON db_matching.* TO 'sanos'@'localhost';
GRANT ALL PRIVILEGES ON db_capacity.* TO 'sanos'@'localhost';
GRANT ALL PRIVILEGES ON db_audit.* TO 'sanos'@'localhost';
GRANT ALL PRIVILEGES ON db_foro.* TO 'sanos'@'localhost';
GRANT ALL PRIVILEGES ON sanosysalvos.* TO 'sanos'@'localhost';

FLUSH PRIVILEGES;
