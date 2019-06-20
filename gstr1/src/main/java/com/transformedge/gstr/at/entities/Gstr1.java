package com.transformedge.gstr.at.entities;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Gstr1 {
	private String gstin;
	private String fp;
	private double gt;
	private double cur_gt;
	private List<AT> at = new ArrayList<>();
}
