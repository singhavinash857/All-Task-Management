package com.transformedge.gstr.cdn.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Nt {
	private String nt_num;
	private String nt_dt;
	private String rsn;
	private String p_gst;
	private String inum;
	private String idt;
	private String ntty;
	private double val;
	private List<Items> itms = new ArrayList<Items>();

}

