package com.example.onionstore.domain.category.repository;

import com.example.onionstore.domain.category.entity.Category;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE c.deleted = false ORDER BY c.name ASC")
    List<Category> findAllByDeletedFalse();

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND c.deleted = false")
    boolean existsByName(@Param("name") String name);
  
    @Query("SELECT c FROM Category c WHERE c.deleted = false AND c.name = :name")
    Optional<Category> findByName(@Param("name") String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Category> findForUpdateById(long id);
}
