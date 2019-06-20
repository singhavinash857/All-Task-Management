package com.transformedge.gstr.b2b.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Inv {
	private String inum;
	private String idt;
	private double val;
	private String rchrg;
	private String pos;
	private String inv_typ;
	List<Itms> itms = new ArrayList<Itms>();
	
}
