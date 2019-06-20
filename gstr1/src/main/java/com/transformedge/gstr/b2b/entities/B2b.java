package com.transformedge.gstr.b2b.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class B2b   {
	private String ctin;
	List<Inv> inv = new ArrayList<Inv>();
}
