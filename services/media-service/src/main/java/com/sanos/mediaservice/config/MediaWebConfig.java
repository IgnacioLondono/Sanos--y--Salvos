package com.sanos.mediaservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties(MediaStorageProperties.class)
public class MediaWebConfig implements WebMvcConfigurer {

    private final MediaStorageProperties properties;

    public MediaWebConfig(MediaStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
        try {
            java.nio.file.Files.createDirectories(uploadPath);
        } catch (java.io.IOException ignored) {
            /* directorio se crea al subir */
        }
        String location = "file:" + uploadPath.toString().replace("\\", "/") + "/";
        registry.addResourceHandler(properties.getPublicBasePath() + "/**")
                .addResourceLocations(location);
    }
}
