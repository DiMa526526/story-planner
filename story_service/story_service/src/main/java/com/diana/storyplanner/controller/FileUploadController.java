package com.diana.storyplanner.controller;

import com.diana.storyplanner.dto.response.FileUploadResponse;
import com.diana.storyplanner.service.FileStorageService;
import com.diana.storyplanner.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Контроллер для загрузки файлов на сервер.
 * Обеспечивает загрузку изображений для обложек историй и аватаров персонажей.
 * Требует JWT аутентификацию (кроме доступа к уже загруженным файлам).
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final JwtService jwtService;

    /**
     * Загрузка одного файла (изображения).
     * Принимает multipart/form-data с полем "image".
     *
     * @param file загружаемый файл (MultipartFile)
     * @param authHeader заголовок Authorization с JWT токеном
     * @return ResponseEntity с информацией о загруженном файле или сообщением об ошибке
     */
    @PostMapping
    public ResponseEntity<?> uploadFile(
            @RequestParam("image") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            // Валидация JWT токена
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Попытка загрузки файла без авторизации");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Необходима авторизация");
            }

            String token = authHeader.substring(7);
            if (!jwtService.validateToken(token)) {
                log.warn("Попытка загрузки файла с недействительным токеном");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Недействительный токен");
            }

            // Определяем поддиректорию для хранения (можно расширить в будущем)
            String subdirectory = "images";

            String fileUrl = fileStorageService.storeFile(file, subdirectory);

            FileUploadResponse response = FileUploadResponse.builder()
                    .url(fileUrl)
                    .fileName(file.getOriginalFilename())
                    .size(file.getSize())
                    .contentType(file.getContentType())
                    .build();

            log.info("Файл успешно загружен: fileName={}, size={}, url={}",
                    file.getOriginalFilename(), file.getSize(), fileUrl);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Ошибка валидации файла: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка загрузки файла: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка загрузки файла: " + e.getMessage());
        }
    }

    /**
     * Удаление файла с сервера.
     *
     * @param url URL файла для удаления
     * @return ResponseEntity с результатом операции
     */
    @DeleteMapping
    public ResponseEntity<?> deleteFile(@RequestParam String url) {
        try {
            boolean deleted = fileStorageService.deleteFile(url);
            if (deleted) {
                log.info("Файл удален: {}", url);
                return ResponseEntity.ok().body("Файл удален");
            } else {
                log.warn("Файл не найден: {}", url);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Ошибка удаления файла: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка удаления файла");
        }
    }
}