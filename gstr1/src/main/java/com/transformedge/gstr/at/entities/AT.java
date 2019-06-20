package com.transformedge.gstr.at.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class AT {
	private String pos;
	private String sply_ty;
	private List<Itms> itms = new ArrayList<Itms>();
}
