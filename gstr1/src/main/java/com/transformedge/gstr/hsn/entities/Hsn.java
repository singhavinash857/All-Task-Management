package com.transformedge.gstr.hsn.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Hsn {
	private List<Datas> data = new ArrayList<Datas>();
}
