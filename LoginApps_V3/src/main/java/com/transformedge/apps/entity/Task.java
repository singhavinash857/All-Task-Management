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
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property="taskId")
@Data
@Entity
@Table(name="task")
public class Task {
	
	  @Id
	  @GeneratedValue(strategy=GenerationType.IDENTITY)
	  private long taskId;
	  
	  @NotNull(message = "{task.taskName.notNull}")
	  private String taskName;
	  
	  @NotNull(message = "{task.taskType.notNull}")
	  private String taskType;
	  
	  @NotNull(message = "{task.assignmentType.notNull}")
	  private String taskAssignmentType;
	  
	  @NotNull(message = "{task.taskDescription.notNull}")
	  private String taskDescription;
	  private String taskPriority;
	  
	  private Date taskAssignedDate;
	  
	  @NotNull(message = "{task.taskSupervisor.notNull}")
	  private String taskSupervisor;
	  private String taskStatus;
	  
	  private String taskAssigner;
	  private boolean taskDone = false;
	  
	  @Transient
	  @ManyToMany(mappedBy = "tasks")
	  private List<Employee> employee = new ArrayList<>();
	  
	  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	  private List<TaskDailyComments> taskDailyComments = new ArrayList<>();
}
