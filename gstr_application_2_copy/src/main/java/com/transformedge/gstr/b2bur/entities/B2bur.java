package com.transformedge.gstr.b2bur.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class B2bur {
	private List<Inv> inv = new ArrayList<Inv>();
}
