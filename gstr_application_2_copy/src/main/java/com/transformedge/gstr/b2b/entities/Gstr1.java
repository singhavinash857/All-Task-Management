package com.transformedge.gstr.b2b.entities;
import java.util.List;

import lombok.Data;

@Data
public class Gstr1 {
	private String gstin;
	private String fp;
	private List<B2b> b2b;
}
