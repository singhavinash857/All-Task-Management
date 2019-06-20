package com.transformedge.gstr.models;

import java.util.List;

import com.transformedge.gstr.b2b.entities.B2b;
import com.transformedge.gstr.request.AbstractRequest;

import lombok.Getter;
import lombok.Setter;


public class GstrValue extends AbstractRequest{
	@Setter
	@Getter
	List<B2b> b2bItems;
}
