-- =====================================================================
-- Foro comunitario — Base de datos db_foro
-- Importar en phpMyAdmin (XAMPP) o ejecutar tras db/init.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS db_foro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_foro;

CREATE TABLE IF NOT EXISTS hilos_foro (
    id_hilo BIGINT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(200) NOT NULL,
    categoria VARCHAR(32) NOT NULL,
    id_usuario BIGINT NULL,
    nombre_autor VARCHAR(120) NULL,
    fecha_creacion DATETIME(6) NULL,
    fecha_actualizacion DATETIME(6) NULL,
    PRIMARY KEY (id_hilo),
    INDEX idx_hilos_categoria (categoria),
    INDEX idx_hilos_actualizacion (fecha_actualizacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS mensajes_foro (
    id_mensaje BIGINT NOT NULL AUTO_INCREMENT,
    id_hilo BIGINT NOT NULL,
    contenido TEXT NOT NULL,
    id_usuario BIGINT NULL,
    nombre_autor VARCHAR(120) NULL,
    fecha_creacion DATETIME(6) NULL,
    PRIMARY KEY (id_mensaje),
    INDEX idx_mensajes_hilo (id_hilo),
    CONSTRAINT fk_mensajes_hilo FOREIGN KEY (id_hilo) REFERENCES hilos_foro (id_hilo) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
