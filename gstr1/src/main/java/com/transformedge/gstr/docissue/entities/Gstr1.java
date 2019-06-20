package com.transformedge.gstr.docissue.entities;
import lombok.Data;

@Data
public class Gstr1 {
	private String gstin;
	private String fp;
	private double gt;
	private double cur_gt;
	private DocIssue doc_issue;
}
