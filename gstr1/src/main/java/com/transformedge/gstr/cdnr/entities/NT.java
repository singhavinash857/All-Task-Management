package com.transformedge.gstr.cdnr.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class NT {
	private char ntty;
	private String nt_num;
	private String nt_dt;
	private String p_gst;
	private String rsn;
	private String inum;
	private String idt;
	private double val;
    private List<Itms> itms = new ArrayList<>();
}
