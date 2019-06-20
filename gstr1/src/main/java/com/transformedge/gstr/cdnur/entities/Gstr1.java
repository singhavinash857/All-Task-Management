package com.transformedge.gstr.cdnur.entities;
import java.util.List;

import lombok.Data;

@Data
public class Gstr1 {
	private String gstin;
	private String fp;
	private double gt;
	private double cur_gt;
	private List<CDNUR> cdnur;
}
