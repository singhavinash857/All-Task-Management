package com.transformedge.apps.model;

import java.util.ArrayList;
import java.util.List;

import com.transformedge.apps.entity.Employee;

import lombok.Data;

@Data
public class EmployeeResponse {
	private int pageNum;
	private int pageSize;
	private int toalPages;
	private long totalCounts;
	private boolean hashMore;
	private List<Employee> values = new ArrayList<>();
}
