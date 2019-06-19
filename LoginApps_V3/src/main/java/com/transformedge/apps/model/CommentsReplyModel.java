package com.transformedge.apps.model;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CommentsReplyModel {
	
	@NotNull(message = "{user.comments.notNull}")
	private String comment;
}
