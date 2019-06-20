package com.transformedge.gstr.request;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Actions {
	
    @JsonProperty("ActionType")
	private String actionType;
	
    @JsonProperty("Details")
	private List<Details> details = new ArrayList<Details>();

}
