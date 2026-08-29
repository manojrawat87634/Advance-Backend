package com.example.demo.repo.notes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.models.notes.Note;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    Optional<Note> findByIdAndIsDeletedFalse(Long id);
    Optional<Note> findByMediaAssetId(Long mediaAssetId);
    List<Note> findByUploaderIdAndIsDeletedFalse(Long uploaderId);
    Page<Note> findByIsPublishedTrueAndIsDeletedFalse(Pageable pageable);
    @Query("SELECT n FROM Note n WHERE n.isPublished = true AND n.isDeleted = false " +
           "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Note> searchPublishedNotes(@Param("keyword") String keyword, Pageable pageable);
}