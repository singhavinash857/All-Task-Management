package com.transformedge.gstr.exceptions;

import lombok.Data;

@Data
public class GstrFailedInvoice {
		private String gstin;
		private String actionType;
}
