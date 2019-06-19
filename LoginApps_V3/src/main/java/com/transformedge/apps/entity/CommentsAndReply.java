package com.transformedge.apps.entity;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="Comments")
public class CommentsAndReply {

	@Id
	private String commentId;
	
	private String parentId;
	
	private String taskId;
	private String commentedBy;
	private String comment;
	private Date commentDate;
	private String commentTime;
	private boolean hashReply = false;
}	
