package com.transformedge.gstr.b2b.entities;

import lombok.Data;

@Data
public class Itms {
 
	private int num;
	ItemDetails itm_det = new ItemDetails();
	ItcDetails itc = new ItcDetails();
}
