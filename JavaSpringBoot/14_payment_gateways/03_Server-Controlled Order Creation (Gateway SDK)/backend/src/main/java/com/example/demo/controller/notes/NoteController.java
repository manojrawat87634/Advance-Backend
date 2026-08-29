package com.example.demo.controller.notes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.notes.NoteDto.*;
import com.example.demo.services.notes.NoteService;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(
            @RequestHeader("X-User-Id") Long uploaderId,
            @Valid @RequestBody CreateNoteRequest request) {
        NoteResponse response = noteService.createNote(uploaderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    @GetMapping
    public ResponseEntity<Page<NoteResponse>> getAllPublishedNotes(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(noteService.getAllPublishedNotes(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<NoteResponse>> searchNotes(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(noteService.searchNotes(query, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long uploaderId,
            @RequestBody UpdateNoteRequest request) {
        return ResponseEntity.ok(noteService.updateNote(id, uploaderId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long uploaderId) {
        noteService.deleteNote(id, uploaderId);
        return ResponseEntity.noContent().build();
    }
}