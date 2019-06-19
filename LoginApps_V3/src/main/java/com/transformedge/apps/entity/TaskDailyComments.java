package com.transformedge.apps.entity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
@Entity
@Table(name="taskDailyComments")
public class TaskDailyComments {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	private Date taskCommentDate;
	private String taskWorkingHours;
	private String taskComments;
	private String taskAssignedBy;
	private String taskCommentedBy;
	private String taskStatus;
	
	@Transient
	@ManyToMany(mappedBy = "taskDailyComments")
	private List<Task> task = new ArrayList<>();
}
