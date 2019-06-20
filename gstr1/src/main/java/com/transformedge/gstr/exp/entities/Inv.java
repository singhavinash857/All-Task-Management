package com.transformedge.gstr.exp.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Inv {
	private String inum;
	private String idt;
	private double val;
	private String sbpcode;
	private String sbnum;
	private String sbdt;
	private List<Itms> itms = new ArrayList<Itms>();

}
