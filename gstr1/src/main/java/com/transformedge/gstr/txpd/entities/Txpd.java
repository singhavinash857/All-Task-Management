package com.transformedge.gstr.txpd.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Txpd {
	private String pos;
	private String sply_ty;
	private List<Itms> itms = new ArrayList<Itms>();
}
