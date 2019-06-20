package com.transformedge.gstr.docissue.entities;

import java.util.List;
import lombok.Data;

@Data
public class DocDet {
		private int doc_num;
		private List<Docs> docs;
}
