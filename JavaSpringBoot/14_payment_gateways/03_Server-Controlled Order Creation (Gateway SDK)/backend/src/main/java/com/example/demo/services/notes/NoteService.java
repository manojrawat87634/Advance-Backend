package com.example.demo.services.notes;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.image.MediaAssetResponse;
import com.example.demo.dto.notes.NoteDto.CreateNoteRequest;
import com.example.demo.dto.notes.NoteDto.NoteResponse;
import com.example.demo.dto.notes.NoteDto.UpdateNoteRequest;
import com.example.demo.models.notes.Note;
import com.example.demo.repo.notes.NoteRepository;
import com.example.demo.services.media.*;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final MediaService mediaService;
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
   @Transactional
public NoteResponse uploadNoteAsset(MultipartFile file, Long userId) {

    // 1. Upload the file to MinIO via MediaService
    // Adjust "NOTES_APP" and "NOTE_PDF" to match your system's client app and entity type constants
    MediaAssetResponse mediaResponse = mediaService.uploadDirectlyToMinio(
            file, 
            "NOTES_APP", 
            "NOTE_PDF", 
            null, 
            userId
    );

    // 2. Build Note using the returned media asset ID
    Note note = Note.builder()
            .mediaAssetId(mediaResponse.id()) // or mediaResponse.getId() depending on your DTO
            .title(null) // Default title until updated
            .description(null)
            .priceInSubunits(null)
            .currency(null)
            .isPublished(false)     // Set to false until details are completed
            .isDeleted(false)
            .build();

    // 3. Save and return mapped response
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