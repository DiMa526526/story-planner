package com.diana.storyplanner.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с файлами (загрузка, валидация, удаление).
 * Обеспечивает безопасное хранение файлов на сервере с проверкой
 * типа и размера. Поддерживает создание миниатюр для изображений.
 */
@Service
public class FileStorageService {

    /**
     * Директория для хранения загруженных файлов.
     * Значение берется из application.properties
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Список разрешенных MIME-типов для загрузки.
     * Поддерживаются только изображения.
     */
    private final List<String> allowedContentTypes = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /**
     * Максимальный размер файла (5MB в байтах).
     */
    private final long maxFileSize = 5 * 1024 * 1024;

    /**
     * Сохранение загруженного файла на диск.
     *
     * @param file загруженный MultipartFile
     * @param subdirectory поддиректория внутри uploads (например, "images")
     * @return URL для доступа к файлу
     * @throws IOException при ошибках ввода-вывода
     */
    public String storeFile(MultipartFile file, String subdirectory) throws IOException {
        validateFile(file);

        // Создаем директорию если не существует
        Path uploadPath = Paths.get(uploadDir, subdirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Генерируем уникальное имя файла (сохраняем оригинальное расширение)
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // Сохраняем файл на диск
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Если это изображение, создаем миниатюру (превью) для быстрого отображения
        if (file.getContentType().startsWith("image/")) {
            createThumbnail(filePath, uploadPath.resolve("thumb_" + fileName));
        }

        // Возвращаем URL для доступа к файлу
        return "/uploads/" + subdirectory + "/" + fileName;
    }

    /**
     * Валидация загруженного файла.
     * Проверяет: не пустой ли файл, размер, тип содержимого.
     *
     * @param file загруженный MultipartFile
     * @throws IllegalArgumentException если файл не проходит валидацию
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пуст");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Файл превышает максимальный размер (5MB)");
        }

        if (!allowedContentTypes.contains(file.getContentType())) {
            throw new IllegalArgumentException("Неподдерживаемый тип файла. Разрешены: JPG, PNG, GIF, WEBP");
        }
    }

    /**
     * Создание миниатюры (превью) изображения.
     * Использует библиотеку Thumbnailator для изменения размера.
     *
     * @param sourcePath путь к оригинальному файлу
     * @param thumbnailPath путь для сохранения миниатюры
     * @throws IOException при ошибках обработки изображения
     */
    private void createThumbnail(Path sourcePath, Path thumbnailPath) throws IOException {
        try {
            Thumbnails.of(sourcePath.toFile())
                    .size(200, 200)
                    .keepAspectRatio(true)
                    .toFile(thumbnailPath.toFile());
        } catch (IOException e) {
            // Логируем ошибку, но не прерываем выполнение основного процесса
            System.err.println("Не удалось создать миниатюру: " + e.getMessage());
        }
    }

    /**
     * Удаление файла с диска по его URL.
     *
     * @param fileUrl URL файла (например, /uploads/images/file.jpg)
     * @return true если файл успешно удален, false если файл не найден
     */
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
                // Убираем префикс /uploads/ для получения относительного пути
                Path filePath = Paths.get(uploadDir, fileUrl.substring(9));
                return Files.deleteIfExists(filePath);
            }
            return false;
        } catch (IOException e) {
            System.err.println("Ошибка удаления файла: " + e.getMessage());
            return false;
        }
    }
}