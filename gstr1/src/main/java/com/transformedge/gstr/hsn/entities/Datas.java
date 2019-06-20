package com.transformedge.gstr.hsn.entities;

import lombok.Data;

@Data
public class Datas {
	
	private int num;
    private String hsn_sc;
    private String desc;
    private String uqc;
    private double qty;
    private double val;
    private double txval;
    private double iamt;
    private double csamt;

}
