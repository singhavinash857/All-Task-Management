package com.transformedge.gstr.impg.entities;

import java.util.List;

import lombok.Data;

@Data
public class Gstr1 {
	private String gstin;
	private String fp;
	private List<Impg> imp_g;
}
