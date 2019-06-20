package com.transformedge.gstr.cdnr.entities;

import java.util.List;

import lombok.Data;

@Data
public class CDNR {
	private String ctin;
	private List<NT> nt;
}
