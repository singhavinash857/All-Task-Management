package com.transformedge.apps.entity;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="projects_details")
public class Projects {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="project_id", nullable=false, updatable=false)
	private long projectId;
	
	@Column(name="project_name", nullable=false, updatable=true)
    private String projectName;
	
	@Column(name="project_assigned_by", nullable=false, updatable=true)
    private String projectAssignedBy;
	
	@Column(name="project_description", nullable=false, updatable=true)
    private String projectDescription;
	
	@Column(name="project_task", nullable=false, updatable=true)
    private String projectTaskOrSubTask;
	
	@Column(name="project_duration", nullable=false, updatable=true)
    private String projectHours;
	
	@Column(name="project_status", nullable=false, updatable=true)
    private String projectStatus;
}
