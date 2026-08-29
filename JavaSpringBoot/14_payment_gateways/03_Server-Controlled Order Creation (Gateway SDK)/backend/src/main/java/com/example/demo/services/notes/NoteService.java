package com.example.demo.services.notes;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.notes.NoteDto.CreateNoteRequest;
import com.example.demo.dto.notes.NoteDto.NoteResponse;
import com.example.demo.dto.notes.NoteDto.UpdateNoteRequest;
import com.example.demo.models.notes.Note;
import com.example.demo.repo.notes.NoteRepository;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    @Transactional
    public NoteResponse createNote(Long uploaderId, CreateNoteRequest request) {
        Note note = Note.builder()
                .uploaderId(uploaderId)
                .mediaAssetId(request.getMediaAssetId())
                .title(request.getTitle())
                .description(request.getDescription())
                .priceInSubunits(request.getPriceInSubunits())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : true)
                .isDeleted(false)
                .build();

        Note savedNote = noteRepository.save(note);
        return mapToResponse(savedNote);
    }

    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long noteId) {
        Note note = noteRepository.findByIdAndIsDeletedFalse(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));
        return mapToResponse(note);
    }

    @Transactional(readOnly = true)
    public Page<NoteResponse> getAllPublishedNotes(Pageable pageable) {
        return noteRepository.findByIsPublishedTrueAndIsDeletedFalse(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<NoteResponse> searchNotes(String keyword, Pageable pageable) {
        return noteRepository.searchPublishedNotes(keyword, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public NoteResponse updateNote(Long noteId, Long uploaderId, UpdateNoteRequest request) {
        Note note = noteRepository.findByIdAndIsDeletedFalse(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));

        if (!note.getUploaderId().equals(uploaderId)) {
            throw new RuntimeException("Unauthorized to update this note");
        }

        if (request.getTitle() != null) note.setTitle(request.getTitle());
        if (request.getDescription() != null) note.setDescription(request.getDescription());
        if (request.getPriceInSubunits() != null) note.setPriceInSubunits(request.getPriceInSubunits());
        if (request.getCurrency() != null) note.setCurrency(request.getCurrency());
        if (request.getIsPublished() != null) note.setIsPublished(request.getIsPublished());

        return mapToResponse(noteRepository.save(note));
    }

    @Transactional
    public void deleteNote(Long noteId, Long uploaderId) {
        Note note = noteRepository.findByIdAndIsDeletedFalse(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));

        if (!note.getUploaderId().equals(uploaderId)) {
            throw new RuntimeException("Unauthorized to delete this note");
        }

        note.setIsDeleted(true);
        noteRepository.save(note);
    }

    private NoteResponse mapToResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .uploaderId(note.getUploaderId())
                .mediaAssetId(note.getMediaAssetId())
                .title(note.getTitle())
                .description(note.getDescription())
                .priceInSubunits(note.getPriceInSubunits())
                .currency(note.getCurrency())
                .isPublished(note.getIsPublished())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}