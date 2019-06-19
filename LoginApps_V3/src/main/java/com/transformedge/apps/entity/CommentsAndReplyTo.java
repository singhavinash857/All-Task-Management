package com.transformedge.apps.entity;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="Comments_Reply_To")
public class CommentsAndReplyTo {
	
	@Id
	private String replyId;
	
	private String commentId;
	private String taskId;
	
	private String commentedBy;
	private String comment;
	
	private String replyTo;
	
	private Date commentDate;
	private String commentTime;
	
	private boolean hashReply = false;
}
