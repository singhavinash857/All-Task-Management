package com.transformedge.gstr.b2bur.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Inv {

	private String chkSUM;
	private String inum;
	private String idt;
	private double val;
	private String sply_ty;
	private String pos;
	private List<Items> itms = new ArrayList<Items>();
	
}
