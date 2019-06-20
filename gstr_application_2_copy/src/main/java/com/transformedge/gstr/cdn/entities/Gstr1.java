package com.transformedge.gstr.cdn.entities;
import java.util.List;

import lombok.Data;

@Data
public class Gstr1 {
	private String gstin;
	private String fp;
	private List<Cdn> cdn;
}
