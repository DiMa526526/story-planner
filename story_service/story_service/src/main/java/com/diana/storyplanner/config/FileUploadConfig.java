package com.diana.storyplanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация для работы с загруженными файлами.
 * Настраивает статическую раздачу файлов из директории uploads.
 * Позволяет обращаться к загруженным изображениям по URL /uploads/**
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    /**
     * Директория для хранения загруженных файлов.
     * Значение берется из application.properties
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Настройка обработчика ресурсов для статической раздачи файлов.
     * Связывает URL-шаблон /uploads/** с физической директорией на диске.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}