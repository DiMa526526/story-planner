package com.diana.storyplanner.controller;

import com.diana.storyplanner.dto.request.CharacterRequest;
import com.diana.storyplanner.dto.response.CharacterResponse;
import com.diana.storyplanner.service.CharacterService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления персонажами.
 * Все операции привязаны к конкретной истории (storyId).
 */
@RestController
@RequestMapping("/stories/{storyId}/characters")
@RequiredArgsConstructor
@Slf4j
public class CharacterController {

    private final CharacterService characterService;

    /**
     * Создание нового персонажа в указанной истории.
     */
    @PostMapping
    public ResponseEntity<CharacterResponse> createCharacter(
            @PathVariable Long storyId,
            @Valid @RequestBody CharacterRequest request
    ) {
        log.debug("Создание персонажа в истории {}: {}", storyId, request.getName());
        CharacterResponse response = characterService.createCharacter(storyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение всех персонажей указанной истории.
     */
    @GetMapping
    public ResponseEntity<List<CharacterResponse>> getCharactersByStory(@PathVariable Long storyId) {
        log.debug("Получение всех персонажей истории: {}", storyId);
        List<CharacterResponse> characters = characterService.getCharactersByStory(storyId);
        return ResponseEntity.ok(characters);
    }

    /**
     * Получение персонажа по ID.
     */
    @GetMapping("/{characterId}")
    public ResponseEntity<CharacterResponse> getCharacterById(@PathVariable Long characterId) {
        log.debug("Получение персонажа по ID: {}", characterId);
        CharacterResponse character = characterService.getCharacterById(characterId);
        return ResponseEntity.ok(character);
    }

    /**
     * Обновление данных персонажа.
     */
    @PutMapping("/{characterId}")
    public ResponseEntity<CharacterResponse> updateCharacter(
            @PathVariable Long characterId,
            @Valid @RequestBody CharacterRequest request
    ) {
        log.debug("Обновление персонажа с ID: {}", characterId);
        CharacterResponse response = characterService.updateCharacter(characterId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление персонажа.
     */
    @DeleteMapping("/{characterId}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long characterId) {
        log.debug("Удаление персонажа с ID: {}", characterId);
        characterService.deleteCharacter(characterId);
        return ResponseEntity.noContent().build();
    }
}