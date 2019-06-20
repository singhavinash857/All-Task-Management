package com.transformedge.gstr.b2bur.entities;

import java.io.Serializable;

import lombok.Data;

@Data
public class Itcdetails implements Serializable{
	private double tx_c;
	private double tx_s;
	private double tx_cs;
	private double tx_i;
	private String elg;
	
}
