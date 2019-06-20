package com.transformedge.gstr.cdnur.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Cdnur {
	private String rtin;
	private String ntty;
	private String nt_num;
    private String nt_dt;
    private String rsn;
    private String p_gst;
    private String  inum;
    private String idt;
    private double val;
	private List<Items> itms = new ArrayList<Items>();

}
