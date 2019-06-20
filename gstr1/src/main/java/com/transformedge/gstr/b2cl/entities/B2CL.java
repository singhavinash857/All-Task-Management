package com.transformedge.gstr.b2cl.entities;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class B2CL{
	private String pos;
	List<Inv> inv = new ArrayList<Inv>();
}
