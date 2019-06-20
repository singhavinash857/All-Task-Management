package com.transformedge.gstr.cdn.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Cdn {
	private String ctin;
	private List<Nt> nt = new ArrayList<Nt>();
}
