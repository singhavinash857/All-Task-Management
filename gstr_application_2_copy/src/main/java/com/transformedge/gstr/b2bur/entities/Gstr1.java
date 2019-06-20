package com.transformedge.gstr.b2bur.entities;
import java.util.List;

import lombok.Data;

@Data
public class Gstr1 {
	private String gstin;
	private String fp;
	private List<B2bur> b2bur;
}
