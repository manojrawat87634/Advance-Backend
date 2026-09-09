package com.example.demo.services.notes;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.image.MediaAssetResponse;
import com.example.demo.dto.notes.NoteDto.NoteResponse;
import com.example.demo.dto.notes.NoteDto.UpdateNoteRequest;
import com.example.demo.models.notes.Note;
import com.example.demo.repo.notes.NoteRepository;
import com.example.demo.repo.payment.PaymentOrderRepository;
import com.example.demo.repo.payment.UserEntitlementRepository;
import com.example.demo.services.media.MediaService;

import jakarta.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final MediaService mediaService;
    private final UserEntitlementRepository userEntitlementRepository;
    private final PaymentOrderRepository paymentOrderRepository;

    @Transactional
    public NoteResponse updateNoteMetadata(Long uploaderId, Long noteId, UpdateNoteRequest request) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found with ID: " + noteId));

        if (!note.getUploaderId().equals(uploaderId)) {
            throw new AccessDeniedException("You are not allowed to update this note.");
        }

        if (request.getTitle() != null) {
            note.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            note.setDescription(request.getDescription());
        }
        if (request.getPriceInSubunits() != null) {
            note.setPriceInSubunits(request.getPriceInSubunits());
        }
        if (request.getCurrency() != null) {
            note.setCurrency(request.getCurrency());
        }
        if (request.getIsPublished() != null) {
            note.setIsPublished(request.getIsPublished());
        }

        Note updatedNote = noteRepository.save(note);
        return mapToResponse(updatedNote, uploaderId);
    }

    @Transactional
    public NoteResponse uploadNoteAsset(MultipartFile file, Long userId) {
        MediaAssetResponse mediaResponse = mediaService.uploadDirectlyToMinio(
                file,
                "NOTES_APP",
                "NOTE_PDF",
                userId.toString(),
                userId);

        // Adjust mediaResponse.id() to mediaResponse.getId() if MediaAssetResponse is a class instead of a record
        Long mediaId = mediaResponse.id();

        Note note = Note.builder()
                .mediaAssetId(mediaId)
                .title(null)
                .description(null)
                .priceInSubunits(null)
                .currency(null)
                .isPublished(false)
                .isDeleted(false)
                .uploaderId(userId)
                .build();

        Note savedNote = noteRepository.save(note);
        return mapToResponse(savedNote, userId);
    }

    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long noteId, Long currentUserId) {
        Note note = noteRepository.findByIdAndIsDeletedFalse(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));
        return mapToResponse(note, currentUserId);
    }

    @Transactional(readOnly = true)
    public Page<NoteResponse> getAllPublishedNotes(Long currentUserId, Pageable pageable) {
        return noteRepository.findByIsPublishedTrueAndIsDeletedFalse(pageable)
                .map(note -> mapToResponse(note, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<NoteResponse> searchNotes(String keyword, Long currentUserId, Pageable pageable) {
        return noteRepository.searchPublishedNotes(keyword, pageable)
                .map(note -> mapToResponse(note, currentUserId));
    }

    @Transactional
    public NoteResponse updateNote(Long noteId, Long uploaderId, UpdateNoteRequest request) {
        Note note = noteRepository.findByIdAndIsDeletedFalse(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));

        if (!note.getUploaderId().equals(uploaderId)) {
            throw new RuntimeException("Unauthorized to update this note");
        }

        if (request.getTitle() != null)
            note.setTitle(request.getTitle());
        if (request.getDescription() != null)
            note.setDescription(request.getDescription());
        if (request.getPriceInSubunits() != null)
            note.setPriceInSubunits(request.getPriceInSubunits());
        if (request.getCurrency() != null)
            note.setCurrency(request.getCurrency());
        if (request.getIsPublished() != null)
            note.setIsPublished(request.getIsPublished());

        return mapToResponse(noteRepository.save(note), uploaderId);
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

    private NoteResponse mapToResponse(Note note, Long currentUserId) {
        boolean isOwner = currentUserId != null && note.getUploaderId().equals(currentUserId);
        
        // Checks user_entitlements table directly using user_id and media_asset_id
        boolean isPurchased = isOwner || (currentUserId != null 
                && note.getMediaAssetId() != null 
                && userEntitlementRepository.existsByUserIdAndMediaAssetIdAndIsActiveTrue(currentUserId, note.getMediaAssetId()));

        String accessUrl = null;

        if (isPurchased && note.getMediaAssetId() != null) {
            try {
                MediaAssetResponse mediaAsset = mediaService.getAccessUrl(note.getMediaAssetId(), note.getUploaderId());
                // Adjust mediaAsset.accessUrl() to mediaAsset.getAccessUrl() if MediaAssetResponse is a class
                accessUrl = mediaAsset.accessUrl();
            } catch (Exception e) {
                accessUrl = null;
            }
        }

        return NoteResponse.builder()
                .id(note.getId())
                .uploaderId(note.getUploaderId())
                .mediaAssetId(note.getMediaAssetId())
                .title(note.getTitle())
                .description(note.getDescription())
                .priceInSubunits(note.getPriceInSubunits())
                .currency(note.getCurrency())
                .isPublished(note.getIsPublished())
                .isPurchased(isPurchased)
                .accessUrl(accessUrl)
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}