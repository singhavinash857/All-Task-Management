package com.transformedge.gstr.exp.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Exp {
		private String exp_typ;
		private List<Inv> inv = new ArrayList<Inv>();
}
