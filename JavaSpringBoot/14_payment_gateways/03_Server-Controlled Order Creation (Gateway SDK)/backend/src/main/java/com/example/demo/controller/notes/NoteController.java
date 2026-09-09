package com.example.demo.controller.notes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.notes.NoteDto.*;
import com.example.demo.services.notes.NoteService;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping("/update/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable("id") Long noteId,
            @Valid @RequestBody UpdateNoteRequest request, 
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        NoteResponse response = noteService.updateNoteMetadata(userId, noteId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload-notes-asset", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoteResponse> notesAssetUpload(
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        NoteResponse response = noteService.uploadNoteAsset(file, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNoteById(
            @PathVariable Long id,
            Authentication authentication) {
        Long currentUserId = extractUserId(authentication);
        return ResponseEntity.ok(noteService.getNoteById(id, currentUserId));
    }

    @GetMapping
    public ResponseEntity<Page<NoteResponse>> getAllPublishedNotes(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Long currentUserId = extractUserId(authentication);
        return ResponseEntity.ok(noteService.getAllPublishedNotes(currentUserId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<NoteResponse>> searchNotes(
            @RequestParam String query,
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        Long currentUserId = extractUserId(authentication);
        return ResponseEntity.ok(noteService.searchNotes(query, currentUserId, pageable));
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

    /**
     * Helper method to safely extract user ID from Authentication context.
     * Returns null if user is unauthenticated/anonymous.
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return Long.parseLong(authentication.getPrincipal().toString());
    }
}