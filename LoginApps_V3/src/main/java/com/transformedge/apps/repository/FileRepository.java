package com.transformedge.apps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.transformedge.apps.entity.FileDetailsDb;

@Repository
public interface FileRepository extends JpaRepository<FileDetailsDb, String>{

}
