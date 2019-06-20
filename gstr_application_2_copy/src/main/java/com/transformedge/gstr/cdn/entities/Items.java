package com.transformedge.gstr.cdn.entities;

import lombok.Data;

@Data
public class Items {
	private int num;
	private Itemdet itm_det = new Itemdet();
	private Itcdet itc = new Itcdet();
}
