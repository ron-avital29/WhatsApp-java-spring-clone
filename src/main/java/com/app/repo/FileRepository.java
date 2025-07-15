package com.app.repo;

import com.app.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * FileRepository provides access to file data in the database.
 * It extends JpaRepository to provide CRUD operations for File entities.
 */
@Repository
public interface FileRepository extends JpaRepository<File, Long> {
}