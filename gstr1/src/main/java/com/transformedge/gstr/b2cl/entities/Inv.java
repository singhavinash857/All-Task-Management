package com.transformedge.gstr.b2cl.entities;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Inv {
	private String inum;
	private String idt;
	private double val;
	private String etin;
	private List<Itms> itms = new ArrayList<Itms>();
}
