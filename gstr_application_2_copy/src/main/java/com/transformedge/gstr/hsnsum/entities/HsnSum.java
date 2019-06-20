package com.transformedge.gstr.hsnsum.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class HsnSum {
	List<Det> det = new ArrayList<Det>();
}
