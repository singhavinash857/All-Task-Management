package com.transformedge.gstr.b2bur.entities;

import lombok.Data;

@Data
public class Items {

	private int num;

	Itemdetails itm_det = new Itemdetails();
	Itcdetails itc = new Itcdetails();

	
}
