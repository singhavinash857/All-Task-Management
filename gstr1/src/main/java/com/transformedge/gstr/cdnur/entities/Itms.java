package com.transformedge.gstr.cdnur.entities;

import lombok.Data;

@Data
public class Itms {
	private int num;
	private Itm_det itm_det = new Itm_det();
}



