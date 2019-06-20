package com.transformedge.gstr.b2cs.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

@SuppressWarnings("serial")
@Data
public class B2CS implements Serializable{
	private String sply_ty;
	private double rt;
	private String typ;
	private String etin;
	private String pos;
	private double txval;
	private double iamt;
	private double camt;
	private double samt;
	private double csamt;
}
