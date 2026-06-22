package com.diana.storyplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа после загрузки файла.
 * Содержит информацию о загруженном файле: URL для доступа,
 * оригинальное имя, размер и тип контента.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /**
     * URL для доступа к загруженному файлу.
     * Пример: /uploads/images/uuid.jpg
     */
    private String url;

    /**
     * Оригинальное имя файла (которое было у пользователя).
     */
    private String fileName;

    /**
     * Размер файла в байтах.
     */
    private long size;

    /**
     * MIME-тип файла (например, image/jpeg).
     */
    private String contentType;
}