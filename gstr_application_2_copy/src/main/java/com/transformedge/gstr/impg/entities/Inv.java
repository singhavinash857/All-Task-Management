package com.transformedge.gstr.impg.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Inv {
	private String is_sez;
	private String stin;
	private int boe_num;
	private String boe_dt;
	private double boe_val;
	private String port_code;
	private List<Items> itms = new ArrayList<Items>();
	
}
