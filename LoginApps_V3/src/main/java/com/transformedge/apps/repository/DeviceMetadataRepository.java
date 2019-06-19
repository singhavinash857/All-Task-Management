package com.transformedge.apps.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transformedge.apps.entity.DeviceMetadata;

public interface DeviceMetadataRepository extends JpaRepository<DeviceMetadata, Long>{
	List<DeviceMetadata> findByUserId(Long userId);
}
