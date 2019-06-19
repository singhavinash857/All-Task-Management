package com.transformedge.apps.model;

import java.util.ArrayList;
import java.util.List;

import com.transformedge.apps.entity.Task;

import lombok.Data;

@Data
public class TaskResponse {
	private int pageNum;
	private int pageSize;
	private int toalPages;
	private long totalCounts;
	private boolean hashMore;
	private List<Task> values = new ArrayList<>();
}
