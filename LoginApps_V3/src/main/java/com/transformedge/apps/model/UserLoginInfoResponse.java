package com.transformedge.apps.model;

import java.util.ArrayList;
import java.util.List;

import com.transformedge.apps.entity.UserLoginInfo;

import lombok.Data;

@Data
public class UserLoginInfoResponse {
	private int pageNum;
	private int pageSize;
	private int toalPages;
	private long totalCounts;
	private boolean hashMore;
	private List<UserLoginInfo> values = new ArrayList<>();
}
