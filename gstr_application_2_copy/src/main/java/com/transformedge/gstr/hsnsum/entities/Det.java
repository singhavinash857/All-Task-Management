package com.transformedge.gstr.hsnsum.entities;

import lombok.Data;

@Data
public class Det {

	private int num;
	private String hsn_sc;
	private String uqc;
	private double qty;
	private double val;
	private double txval;
	private double iamt;
	private double camt;
	private double samt;
	private double csamt;
	private String desc;
	
}
