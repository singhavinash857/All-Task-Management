package com.transformedge.gstr.processors.savegstr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformedge.gstr.at.entities.AT;
import com.transformedge.gstr.b2b.entities.B2b;
import com.transformedge.gstr.b2b.entities.GstValue;
import com.transformedge.gstr.b2b.entities.Gstr1;
import com.transformedge.gstr.b2b.entities.Inv;
import com.transformedge.gstr.b2b.entities.Itm_det;
import com.transformedge.gstr.b2b.entities.Itms;
import com.transformedge.gstr.b2cl.entities.B2CL;
import com.transformedge.gstr.b2cs.entities.B2CS;
import com.transformedge.gstr.cdnr.entities.CDNR;
import com.transformedge.gstr.cdnr.entities.NT;
import com.transformedge.gstr.cdnur.entities.CDNUR;
import com.transformedge.gstr.configuration.GstrColumnsCofiguration;
import com.transformedge.gstr.configuration.GstrColumnsCofiguration.Gstr;
import com.transformedge.gstr.configuration.TableMetadataConfiguration;
import com.transformedge.gstr.docissue.entities.DocDet;
import com.transformedge.gstr.docissue.entities.DocIssue;
import com.transformedge.gstr.docissue.entities.Docs;
import com.transformedge.gstr.exp.entities.Exp;
import com.transformedge.gstr.hsn.entities.Datas;
import com.transformedge.gstr.hsn.entities.Hsn;
import com.transformedge.gstr.nil.entities.NIL;
import com.transformedge.gstr.txpd.entities.Txpd;

@Component
public class SaveGstrProcessor implements Processor {
	//private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TableMetadataConfiguration tableMetadataConfiguration;

	@Autowired
	private GstrColumnsCofiguration gstrColumnsCofiguration;

	Map<String, String> gstrColumns = null;

	GstrColumnsCofiguration.Gstr gstrColumnsObj = null;
	@Override
	public void process(Exchange exchange) throws Exception {
		gstrColumnsObj = gstrColumnsCofiguration.findByName("saveGSTR");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> received = exchange.getIn().getBody(List.class);
		TableMetadataConfiguration.Table gstrTable = tableMetadataConfiguration.findByName("gstrTable");
		List<String> gstrTableColumns = gstrTable.getColumns();

		gstrColumns = gstrColumnsObj.getColumns();
		HashMap<String, GstValue> gstrInvoiceMap = new HashMap<>();
		GstValue  gstValue = null;

		com.transformedge.gstr.b2cs.entities.GstValue gstValueB2cs = null;
		HashMap<String, com.transformedge.gstr.b2cs.entities.GstValue> gstrInvoiceB2csMap = new HashMap<>();

		com.transformedge.gstr.b2cl.entities.GstValue gstValueB2cl = null;
		HashMap<String, com.transformedge.gstr.b2cl.entities.GstValue> gstrInvoiceB2clMap = new HashMap<>();

		com.transformedge.gstr.at.entities.GstValue gstValueAT = null;
		HashMap<String, com.transformedge.gstr.at.entities.GstValue> gstrInvoiceAtMap = new HashMap<>();

		com.transformedge.gstr.txpd.entities.GstValue gstValueTxpd = null;
		HashMap<String, com.transformedge.gstr.txpd.entities.GstValue> gstrInvoiceTxpdMap = new HashMap<>();

		com.transformedge.gstr.exp.entities.GstValue gstValueExp = null;
		HashMap<String, com.transformedge.gstr.exp.entities.GstValue> gstrInvoiceExpMap = new HashMap<>();

		com.transformedge.gstr.hsn.entities.GstValue gstValueHsn = null;
		HashMap<String, com.transformedge.gstr.hsn.entities.GstValue> gstrInvoiceHsnMap = new HashMap<>();

		com.transformedge.gstr.cdnur.entities.GstValue gstValueCdnur = null;
		HashMap<String, com.transformedge.gstr.cdnur.entities.GstValue> gstrInvoiceCdnurMap = new HashMap<>();

		com.transformedge.gstr.cdnr.entities.GstValue gstValueCdnr = null;
		HashMap<String, com.transformedge.gstr.cdnr.entities.GstValue> gstrInvoiceCdnrMap = new HashMap<>();

		com.transformedge.gstr.nil.entities.GstValue gstValueNil = null;
		HashMap<String, com.transformedge.gstr.nil.entities.GstValue> gstrInvoiceNilMap = new HashMap<>();

		com.transformedge.gstr.docissue.entities.GstValue gstValueDocIssue = null;
		HashMap<String, com.transformedge.gstr.docissue.entities.GstValue> gstrInvoiceDocIssueMap = new HashMap<>();

		for(Map<String, Object> mapRow : received){
			Object identifierColumn = mapRow.get(gstrTable.getIdentifierColumn());
			Object ctinNumber = mapRow.get(gstrColumnsObj.getColumnsValue("THIRD_PARTY_REG_NUM"));
			Object inum = mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_NUMBER"));
			Object sectionCode = mapRow.get(gstrColumnsObj.getColumnsValue("SECTION_CODE"));
			Object pos = mapRow.get(gstrColumnsObj.getColumnsValue("STATE"));
			Object exp_type = mapRow.get(gstrColumnsObj.getColumnsValue("EXPORT_TYPE"));
			Object hsnCode = mapRow.get(gstrColumnsObj.getColumnsValue("HSN_CODE"));
			Object supply_type = mapRow.get(gstrColumnsObj.getColumnsValue("SUPP_TYPE"));
			
			Object num = mapRow.get(gstrColumnsObj.getColumnsValue("SERIAL_NO"));
			Object doc_num = mapRow.get(gstrColumnsObj.getColumnsValue("ATTRIBUTE2"));
			

			switch(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase())){
			case "B2B" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("B2B")){
					gstValue =  gstrInvoiceMap.getOrDefault(identifierColumn, null);
					B2b b2b = null;
					Inv inv = null;
					if(gstValue != null){
						b2b = gstValue.getGstValue().getB2b().stream().filter(b -> b.getCtin().equals(ctinNumber)).findAny()
								.orElse(null);
						if(b2b != null){
							inv = b2b.getInv().stream().filter(i -> i.getInum().equals(inum)).findAny().orElse(null);
							if(inv != null){
								Itms itms = new Itms();
								addItemsForGstin(itms,mapRow);
								inv.getItms().add(itms);
							}else{
								/* if new inum for this gstin and ctin...*/
								inv = new Inv();
								addInvoiceForGstin(inv,mapRow);
								b2b.getInv().add(inv);
							}
						}else{
							/* if ctin number is new to this gstin....*/
							b2b = new B2b();
							addB2bToGstin(b2b,mapRow);
							gstValue.getGstValue().getB2b().add(b2b);
						}
					}else{
						gstValue = new GstValue();
						Gstr1 gstr1 = new Gstr1();
						generateJson(gstr1,mapRow,gstrColumnsObj);
						gstValue.setGstValue(gstr1);
						gstrInvoiceMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValue);
					}
				}
				ObjectMapper mapper = new ObjectMapper();
				String jsonInStringB2b = mapper.writeValueAsString(gstValue);
				System.out.println("jsonInStringB2b ::"+jsonInStringB2b);
				exchange.setProperty("proccessCode", "B2B");
				break;
			case "B2CS" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("B2CS")){
					System.out.println("B2CS");
					gstValueB2cs =  gstrInvoiceB2csMap.getOrDefault(identifierColumn, null);
					if(gstValueB2cs != null){
						B2CS b2cs = new B2CS();
						addB2CsToGstin(b2cs, mapRow);
						gstValueB2cs.getGstValue().getB2cs().add(b2cs);
					}else{
						gstValueB2cs = new com.transformedge.gstr.b2cs.entities.GstValue();
						com.transformedge.gstr.b2cs.entities.Gstr1 gstr1 = new com.transformedge.gstr.b2cs.entities.Gstr1();
						addGstinToB2cs(gstr1,mapRow,gstrColumnsObj);
						gstValueB2cs.setGstValue(gstr1);
						gstrInvoiceB2csMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueB2cs);
					}
				}
				ObjectMapper mapperb2cs = new ObjectMapper();
				String jsonInStringB2cs = mapperb2cs.writeValueAsString(gstValueB2cs);
				System.out.println("jsonInStringB2cs ::"+jsonInStringB2cs);
				exchange.setProperty("proccessCode", "B2CS");
				break; 
			case "B2CL" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("B2CL")){
					System.out.println("B2CL");
					gstValueB2cl =  gstrInvoiceB2clMap.getOrDefault(identifierColumn, null);
					B2CL b2cl = null;
					com.transformedge.gstr.b2cl.entities.Inv inv = null;

					if(gstValueB2cl != null){
						b2cl = gstValueB2cl.getGstValue().getB2cl().stream().filter(b -> b.getPos().equals(pos)).findAny()
								.orElse(null);
						if(b2cl != null){
							inv = b2cl.getInv().stream().filter(i -> i.getInum().equals(inum)).findAny().orElse(null);
							if(inv != null){
								com.transformedge.gstr.b2cl.entities.Itms itms = new com.transformedge.gstr.b2cl.entities.Itms();
								addB2clItemsForGstin(itms,mapRow);
								inv.getItms().add(itms);
							}else{
								inv = new com.transformedge.gstr.b2cl.entities.Inv();
								addB2clInvoiceForGstin(inv,mapRow);
								b2cl.getInv().add(inv);
							}
						}else{
							/* if pos number is new to this gstin....*/
							b2cl = new B2CL();
							addB2CLToGstin(b2cl,mapRow);
							gstValueB2cl.getGstValue().getB2cl().add(b2cl);
						}
					}else{
						gstValueB2cl = new com.transformedge.gstr.b2cl.entities.GstValue();
						com.transformedge.gstr.b2cl.entities.Gstr1 gstr1 = new com.transformedge.gstr.b2cl.entities.Gstr1();
						addGstinToB2cl(gstr1,mapRow,gstrColumnsObj);
						gstValueB2cl.setGstValue(gstr1);
						gstrInvoiceB2clMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueB2cl);
					}
				}
				ObjectMapper mapperb2cl = new ObjectMapper();
				String jsonInStringB2cl = mapperb2cl.writeValueAsString(gstValueB2cl);
				System.out.println("jsonInStringB2cl ::"+jsonInStringB2cl);
				exchange.setProperty("proccessCode", "B2CL");
				break;
			case "AT" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("AT")){
					System.out.println("AT");
					gstValueAT =  gstrInvoiceAtMap.getOrDefault(identifierColumn, null);
					AT at = null;
					if(gstValueAT != null){
						at = gstValueAT.getGstValue().getAt().stream().filter(b -> b.getPos().equals(pos)).findAny()
								.orElse(null);
						if(at != null){
							com.transformedge.gstr.at.entities.Itms itms = new com.transformedge.gstr.at.entities.Itms();
							addAtItemsForGstin(itms, mapRow);
							at.getItms().add(itms);
						}else{
							at = new AT();
							addATToGstin(at, mapRow);
							gstValueAT.getGstValue().getAt().add(at);
						}
					}else{
						gstValueAT = new com.transformedge.gstr.at.entities.GstValue();
						com.transformedge.gstr.at.entities.Gstr1 gstr1 = new com.transformedge.gstr.at.entities.Gstr1();
						addGstinToAt(gstr1,mapRow,gstrColumnsObj);
						gstValueAT.setGstValue(gstr1);
						gstrInvoiceAtMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueAT);
					}
				}
				ObjectMapper mapperAt = new ObjectMapper();
				String jsonInStringAt = mapperAt.writeValueAsString(gstValueAT);
				System.out.println("jsonInStringAt ::"+jsonInStringAt);
				exchange.setProperty("proccessCode", "AT");
				break;
			case "TXPD" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("TXPD")){
					System.out.println("TXPD");
					gstValueTxpd =  gstrInvoiceTxpdMap.getOrDefault(identifierColumn, null);
					Txpd txpd = null;
					if(gstValueTxpd != null){
						txpd = gstValueTxpd.getGstValue().getTxpd().stream().filter(b -> b.getPos().equals(pos)).findAny()
								.orElse(null);
						if(txpd != null){
							com.transformedge.gstr.txpd.entities.Itms itms = new com.transformedge.gstr.txpd.entities.Itms();
							addTxpdItemsForGstin(itms, mapRow);
							txpd.getItms().add(itms);
						}else{
							txpd = new Txpd();
							addTxpdToGstin(txpd, mapRow);
							gstValueTxpd.getGstValue().getTxpd().add(txpd);
						}
					}else{
						gstValueTxpd = new com.transformedge.gstr.txpd.entities.GstValue();
						com.transformedge.gstr.txpd.entities.Gstr1 gstr1 = new com.transformedge.gstr.txpd.entities.Gstr1();
						addGstinToTxpd(gstr1,mapRow,gstrColumnsObj);
						gstValueTxpd.setGstValue(gstr1);
						gstrInvoiceTxpdMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueTxpd);
					}
				}
				ObjectMapper mapperTxpd = new ObjectMapper();
				String jsonInStringTxpd = mapperTxpd.writeValueAsString(gstValueTxpd);
				System.out.println("jsonInStringTxpd ::"+jsonInStringTxpd);
				exchange.setProperty("proccessCode", "TXPD");
				break;
			case "EXP" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("EXP")){
					gstValueExp =  gstrInvoiceExpMap.getOrDefault(identifierColumn, null);
					Exp exp = null;
					com.transformedge.gstr.exp.entities.Inv inv = null;
					if(gstValueExp != null){
						exp = gstValueExp.getGstValue().getExp().stream().filter(b -> b.getExp_typ().equals(exp_type)).findAny()
								.orElse(null);
						if(exp != null){
							inv = exp.getInv().stream().filter(i -> i.getInum().equals(inum)).findAny().orElse(null);
							if(inv != null){
								com.transformedge.gstr.exp.entities.Itms itms = new com.transformedge.gstr.exp.entities.Itms();
								addExpItemsForGstin(itms, mapRow);
								inv.getItms().add(itms);

							}else{
								inv = new com.transformedge.gstr.exp.entities.Inv();
								addExpInvoiceForGstin(inv, mapRow);
								exp.getInv().add(inv);
							}
						}else{
							exp = new Exp();
							addExpToGstin(exp, mapRow);
							gstValueExp.getGstValue().getExp().add(exp);
						}
					}else{
						gstValueExp = new com.transformedge.gstr.exp.entities.GstValue();
						com.transformedge.gstr.exp.entities.Gstr1 gstr1 = new com.transformedge.gstr.exp.entities.Gstr1();
						addGstinToExp(gstr1,mapRow,gstrColumnsObj);
						gstValueExp.setGstValue(gstr1);
						gstrInvoiceExpMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueExp);
					}
				}
				ObjectMapper mapperExp = new ObjectMapper();
				String jsonInStringExp = mapperExp.writeValueAsString(gstValueExp);
				System.out.println("jsonInStringExp ::"+jsonInStringExp);
				exchange.setProperty("proccessCode", "EXP");
				break;
			case "HSN" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("HSN")){
					gstValueHsn =  gstrInvoiceHsnMap.getOrDefault(identifierColumn, null);
					Datas datas = null;
					if(gstValueHsn != null){
						datas = gstValueHsn.getGstValue().getHsn().getData().stream().filter(b -> b.getHsn_sc().equals(hsnCode)).findAny()
								.orElse(null);
						if(datas != null){

						}else{
							datas = new Datas();
							addDataToHsnGstin(datas, mapRow);
							gstValueHsn.getGstValue().getHsn().getData().add(datas);
						}
					}else{
						gstValueHsn = new com.transformedge.gstr.hsn.entities.GstValue();
						com.transformedge.gstr.hsn.entities.Gstr1 gstr1 = new com.transformedge.gstr.hsn.entities.Gstr1();
						addGstinToHsn(gstr1,mapRow,gstrColumnsObj);
						gstValueHsn.setGstValue(gstr1);
						gstrInvoiceHsnMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueHsn);

					}
				}
				ObjectMapper mapperHsn = new ObjectMapper();
				String jsonInStringHsn = mapperHsn.writeValueAsString(gstValueHsn);
				System.out.println("jsonInStringHsn ::"+jsonInStringHsn);
				exchange.setProperty("proccessCode", "HSN");
				break;
			case "CDNUR" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("CDNUR")){
					gstValueCdnur =  gstrInvoiceCdnurMap.getOrDefault(identifierColumn, null);
					CDNUR cDNUR = null;
					if(gstValueCdnur != null){
						cDNUR = gstValueCdnur.getGstValue().getCdnur().stream().filter(b -> b.getInum().equals(inum)).findAny()
								.orElse(null);
						if(cDNUR != null){
							com.transformedge.gstr.cdnur.entities.Itms itms = new com.transformedge.gstr.cdnur.entities.Itms();
							addCdnurItemsForGstin(itms, mapRow);
							cDNUR.getItms().add(itms);  
						}else{
							cDNUR = new CDNUR();
							addCdnurToGstin(cDNUR, mapRow);
							gstValueCdnur.getGstValue().getCdnur().add(cDNUR);
						}
					}else{
						gstValueCdnur = new com.transformedge.gstr.cdnur.entities.GstValue();
						com.transformedge.gstr.cdnur.entities.Gstr1 gstr1 = new com.transformedge.gstr.cdnur.entities.Gstr1();
						addGstinToCdnur(gstr1,mapRow,gstrColumnsObj);
						gstValueCdnur.setGstValue(gstr1);
						gstrInvoiceCdnurMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueCdnur);
					}
				}
				ObjectMapper mapperCdnur = new ObjectMapper();
				String jsonInStringCdnur = mapperCdnur.writeValueAsString(gstValueCdnur);
				System.out.println("jsonInStringCdnur ::"+jsonInStringCdnur);
				exchange.setProperty("proccessCode", "CDNUR");
				break;
			case "CDNR" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("CDNR")){
					gstValueCdnr =  gstrInvoiceCdnrMap.getOrDefault(identifierColumn, null);
					CDNR cDNR = null;
					NT nt = null;
					if(gstValueCdnr != null){
						cDNR = gstValueCdnr.getGstValue().getCdnr().stream().filter(b -> b.getCtin().equals(ctinNumber)).findAny()
								.orElse(null);
						if(cDNR != null){
							nt = cDNR.getNt().stream().filter(b -> b.getInum().equals(inum)).findAny().orElse(null);
							if(nt != null){
								com.transformedge.gstr.cdnr.entities.Itms itms = new com.transformedge.gstr.cdnr.entities.Itms();
								addCdnrItemsForGstin(itms,mapRow);
								nt.getItms().add(itms);
							}else{
								nt = new NT();
								addNTToCdnr(nt, mapRow);
								cDNR.getNt().add(nt);
							}
						}else{
							cDNR = new CDNR();
							addCdnrToGstin(cDNR, mapRow);
							gstValueCdnr.getGstValue().getCdnr().add(cDNR); 
						}
					}else{
						gstValueCdnr = new com.transformedge.gstr.cdnr.entities.GstValue();
						com.transformedge.gstr.cdnr.entities.Gstr1 gstr1 = new com.transformedge.gstr.cdnr.entities.Gstr1();
						addGstinToCdnr(gstr1,mapRow,gstrColumnsObj);
						gstValueCdnr.setGstValue(gstr1);
						gstrInvoiceCdnrMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueCdnr);

					}
				}
				ObjectMapper mapperCdnr = new ObjectMapper();
				String jsonInStringCdnr = mapperCdnr.writeValueAsString(gstValueCdnr);
				System.out.println("jsonInStringCdnr ::"+jsonInStringCdnr);
				exchange.setProperty("proccessCode", "CDNR");
				break;
			case "NIL" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("NIL")){
					gstValueNil =  gstrInvoiceNilMap.getOrDefault(identifierColumn, null);
					NIL nil = null;
					com.transformedge.gstr.nil.entities.Inv inv = null; 
					if(gstValueNil != null){
						inv = gstValueNil.getGstValue().getNil().getInv().stream().filter(b -> b.getSply_ty().equals(supply_type)).findAny()
								.orElse(null);
						if(inv != null){

						}else{
							inv = new com.transformedge.gstr.nil.entities.Inv();
							addInvToNilForGstin(inv, mapRow);
							gstValueNil.getGstValue().getNil().getInv().add(inv);
						}
					}else{
						gstValueNil = new com.transformedge.gstr.nil.entities.GstValue();
						com.transformedge.gstr.nil.entities.Gstr1 gstr1 = new com.transformedge.gstr.nil.entities.Gstr1();
						addGstinToNil(gstr1,mapRow);
						gstValueNil.setGstValue(gstr1);
						gstrInvoiceNilMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueNil);
					}
				}
				ObjectMapper mapperNil = new ObjectMapper();
				String jsonInStringNil = mapperNil.writeValueAsString(gstValueNil);
				System.out.println("jsonInStringNil ::"+jsonInStringNil);
				exchange.setProperty("proccessCode", "NIL");
				break;
			case "DOC_ISSUE" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("DOC_ISSUE")){
					gstValueDocIssue =  gstrInvoiceDocIssueMap.getOrDefault(identifierColumn, null);
					DocDet docDet = null;
					Docs docs = null;
					if(gstValueDocIssue != null){
						docDet = gstValueDocIssue.getGstValue().getDoc_issue().getDoc_det().stream().filter(b -> Integer.toString(b.getDoc_num()).equals(doc_num)).findAny()
								.orElse(null);
						if(docDet != null){
							docs = docDet.getDocs().stream().filter(b -> Integer.toString(b.getNum()).equals(num)).findAny()
									.orElse(null);
							if(docs != null){
								
							}else{
								docs = new Docs();
								addDocsToDocDet(docs, mapRow);
								docDet.getDocs().add(docs);
							}
						}else{
							docDet = new DocDet();
							addDocDetToDocIssue(docDet, mapRow);
							gstValueDocIssue.getGstValue().getDoc_issue().getDoc_det().add(docDet);
						}
					}else{
						gstValueDocIssue = new com.transformedge.gstr.docissue.entities.GstValue();
						com.transformedge.gstr.docissue.entities.Gstr1 gstr1 = new com.transformedge.gstr.docissue.entities.Gstr1();
						addGstinToDocIssue(gstr1,mapRow);
						gstValueDocIssue.setGstValue(gstr1);
						gstrInvoiceDocIssueMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueDocIssue);
					
					}
				}
				ObjectMapper mapperDocIssue = new ObjectMapper();
				String jsonInStringDocIssue = mapperDocIssue.writeValueAsString(gstValueDocIssue);
				System.out.println("jsonInStringDocIssue ::"+jsonInStringDocIssue);
				exchange.setProperty("proccessCode", "DOC_ISSUE");
				break;
			default: 
				break;
			}
		}
		if(gstValue != null){
			Collection<GstValue> gstrInvoices = gstrInvoiceMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueB2cs != null){
			Collection<com.transformedge.gstr.b2cs.entities.GstValue> gstrInvoices = gstrInvoiceB2csMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueB2cl != null){
			Collection<com.transformedge.gstr.b2cl.entities.GstValue> gstrInvoices = gstrInvoiceB2clMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueAT != null){
			Collection<com.transformedge.gstr.at.entities.GstValue> gstrInvoices = gstrInvoiceAtMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueTxpd != null){
			Collection<com.transformedge.gstr.txpd.entities.GstValue> gstrInvoices = gstrInvoiceTxpdMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueExp != null){
			Collection<com.transformedge.gstr.exp.entities.GstValue> gstrInvoices = gstrInvoiceExpMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueHsn != null){
			Collection<com.transformedge.gstr.hsn.entities.GstValue> gstrInvoices = gstrInvoiceHsnMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueCdnur != null){
			Collection<com.transformedge.gstr.cdnur.entities.GstValue> gstrInvoices = gstrInvoiceCdnurMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueCdnr != null){
			Collection<com.transformedge.gstr.cdnr.entities.GstValue> gstrInvoices = gstrInvoiceCdnrMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueNil != null){
			Collection<com.transformedge.gstr.nil.entities.GstValue> gstrInvoices = gstrInvoiceNilMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueDocIssue != null){
			Collection<com.transformedge.gstr.docissue.entities.GstValue> gstrInvoices = gstrInvoiceDocIssueMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else{
			logger.error("Please check your SaveGstrProcessor exchange body");
		}

	}

	private void addB2bToGstin(B2b b2b, Map<String, Object> mapRow) {
		Object ctinNum = mapRow.get(gstrColumnsObj.getColumnsValue("THIRD_PARTY_REG_NUM"));
		b2b.setCtin(ctinNum.toString());

		Inv inv = new Inv();
		addInvoiceForGstin(inv,mapRow);
		List<Inv> invoiceList = new ArrayList<Inv>();
		invoiceList.add(inv);

		b2b.setInv(invoiceList);
	}


	private void addInvoiceForGstin(Inv inv, Map<String, Object> mapRow) {
		Object texInvoiceNumber = mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_NUMBER"));
		inv.setInum(texInvoiceNumber.toString());

		Object taxInvoiceDate = mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_DATE"));
		inv.setIdt(taxInvoiceDate.toString());

		Object taxInvoiceValue = mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_VALUE"));
		inv.setVal(Double.parseDouble(taxInvoiceValue.toString()));

		Object revCharge = mapRow.get(gstrColumnsObj.getColumnsValue("REV_CHARGE"));
		inv.setRchrg(revCharge.toString());

		Object state = mapRow.get(gstrColumnsObj.getColumnsValue("STATE"));
		inv.setPos(state.toString());

		Object invType = mapRow.get(gstrColumnsObj.getColumnsValue("INV_TYPE"));
		inv.setInv_typ(invType.toString());

		List<Itms> items = new ArrayList<Itms>();
		Itms item = new Itms();
		addItemsForGstin(item,mapRow);
		items.add(item);
		inv.setItms(items);
	}

	private void addItemsForGstin(Itms itms, Map<String, Object> mapRow) {
		Object serialNum = mapRow.get(gstrColumnsObj.getColumnsValue("SERIAL_NO"));
		itms.setNum(Integer.parseInt(serialNum.toString()));

		Itm_det itm_det = new Itm_det();

		Object taxRate = mapRow.get(gstrColumnsObj.getColumnsValue("TAX_RATE"));
		itm_det.setRt(Double.parseDouble(taxRate.toString()));

		Object taxableAmount = mapRow.get(gstrColumnsObj.getColumnsValue("TAXABLE_AMT"));
		itm_det.setTxval(Double.parseDouble(taxableAmount.toString()));

		Object igst = mapRow.get(gstrColumnsObj.getColumnsValue("IGST"));
		itm_det.setIamt(Double.parseDouble(igst.toString()));

		Object samt = mapRow.get(gstrColumnsObj.getColumnsValue("SGST"));
		itm_det.setSamt(Double.parseDouble(samt.toString()));

		Object cgst = mapRow.get(gstrColumnsObj.getColumnsValue("CGST"));
		itm_det.setCamt(Double.parseDouble(cgst.toString()));

		Object cess = mapRow.get(gstrColumnsObj.getColumnsValue("CESS"));
		itm_det.setCsamt(Double.parseDouble(cess.toString()));

		itms.setItm_det(itm_det);
	}

	private void generateJson(Gstr1 gstr1, Map<String, Object> mapRow, Gstr gstrColumnsObj) {

		addGstinNumber(gstr1,mapRow,gstrColumnsObj);

		B2b b2b = new B2b();
		addB2bToGstin(b2b, mapRow);

		List<B2b> b2bDataList = new ArrayList<>();
		b2bDataList.add(b2b);

		gstr1.setB2b(b2bDataList);
	}


	private void addGstinNumber(Gstr1 gstr1, Map<String, Object> mapRow, Gstr gstrColumnsObj2) {
		Object gstIn = mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM"));
		gstr1.setGstin(gstIn.toString());

		Object periodName = mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME"));
		gstr1.setFp(periodName.toString());

		gstr1.setGt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("GROSS_TURNOVER")).toString()));

		Object currentGrossTurnOver = mapRow.get(gstrColumnsObj.getColumnsValue("CURRENT_GROSS_TURNOVER"));
		gstr1.setCur_gt(Double.parseDouble(currentGrossTurnOver.toString()));
	}

	/*=========================== B2CS =========================== */
	private void addGstinToB2cs(com.transformedge.gstr.b2cs.entities.Gstr1 gstr1, Map<String, Object> mapRow,
			Gstr gstrColumnsObj2) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
		gstr1.setGt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("GROSS_TURNOVER")).toString()));
		gstr1.setCur_gt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CURRENT_GROSS_TURNOVER")).toString()));

		B2CS b2cs = new B2CS();
		addB2CsToGstin(b2cs, mapRow);

		List<B2CS> b2csDataList = new ArrayList<>();
		b2csDataList.add(b2cs);

		gstr1.setB2cs(b2csDataList);
	}


	private void addB2CsToGstin(B2CS b2cs, Map<String, Object> mapRow) {

		b2cs.setSply_ty(mapRow.get(gstrColumnsObj.getColumnsValue("SUPP_TYPE")).toString());
		b2cs.setRt((long)mapRow.get(gstrColumnsObj.getColumnsValue("TAX_RATE")));
		b2cs.setTyp(mapRow.get(gstrColumnsObj.getColumnsValue("ATTRIBUTE1")).toString());
		b2cs.setEtin(mapRow.get(gstrColumnsObj.getColumnsValue("ECOM_OPERATOR")).toString());
		b2cs.setPos(mapRow.get(gstrColumnsObj.getColumnsValue("STATE")).toString());
		b2cs.setTxval(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAXABLE_AMT")).toString()));

		b2cs.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
		b2cs.setCamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CGST")).toString()));
		b2cs.setSamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("SGST")).toString()));
		b2cs.setCsamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CESS")).toString()));
	}

	/*================================ B2CL ===============================*/
	private void addGstinToB2cl(com.transformedge.gstr.b2cl.entities.Gstr1 gstr1, Map<String, Object> mapRow,
			Gstr gstrColumnsObj2) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
		gstr1.setGt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("GROSS_TURNOVER")).toString()));
		gstr1.setCur_gt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CURRENT_GROSS_TURNOVER")).toString()));

		B2CL b2cl = new B2CL();
		addB2CLToGstin(b2cl, mapRow);

		List<B2CL> b2csDataList = new ArrayList<B2CL>();
		b2csDataList.add(b2cl);

		gstr1.setB2cl(b2csDataList);
	}

	private void addB2CLToGstin(B2CL b2cl, Map<String, Object> mapRow) {
		Object pos = mapRow.get(gstrColumnsObj.getColumnsValue("STATE"));
		b2cl.setPos(pos.toString());

		com.transformedge.gstr.b2cl.entities.Inv inv = new com.transformedge.gstr.b2cl.entities.Inv();

		addB2clInvoiceForGstin(inv,mapRow);

		List<com.transformedge.gstr.b2cl.entities.Inv> invoiceList = new ArrayList<>();
		invoiceList.add(inv);

		b2cl.setInv(invoiceList);
	}

	private void addB2clInvoiceForGstin(com.transformedge.gstr.b2cl.entities.Inv inv, Map<String, Object> mapRow) {
		inv.setInum(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_NUMBER")).toString());
		inv.setIdt(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_DATE")).toString());
		inv.setVal(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_VALUE")).toString()));
		inv.setEtin(mapRow.get(gstrColumnsObj.getColumnsValue("ECOM_OPERATOR")).toString());

		List<com.transformedge.gstr.b2cl.entities.Itms> items = new ArrayList<>();

		com.transformedge.gstr.b2cl.entities.Itms item = new com.transformedge.gstr.b2cl.entities.Itms();

		addB2clItemsForGstin(item,mapRow);

		items.add(item);
		inv.setItms(items);		
	}

	private void addB2clItemsForGstin(com.transformedge.gstr.b2cl.entities.Itms item, Map<String, Object> mapRow) {
		item.setNum(((Long)mapRow.get(gstrColumnsObj.getColumnsValue("SERIAL_NO"))).intValue());

		com.transformedge.gstr.b2cl.entities.Itm_det itm_det = new com.transformedge.gstr.b2cl.entities.Itm_det();

		itm_det.setRt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_RATE")).toString()));
		itm_det.setTxval(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAXABLE_AMT")).toString()));
		itm_det.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
		itm_det.setCsamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CESS")).toString()));

		item.setItm_det(itm_det);
	}

	/* ================================ AT ========================= */
	private void addGstinToAt(com.transformedge.gstr.at.entities.Gstr1 gstr1, Map<String, Object> mapRow,
			Gstr gstrColumnsObj2) {

		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
		gstr1.setGt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("GROSS_TURNOVER")).toString()));
		gstr1.setCur_gt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CURRENT_GROSS_TURNOVER")).toString()));

		AT at = new AT();
		addATToGstin(at, mapRow);

		List<AT> ATDataList = new ArrayList<AT>();
		ATDataList.add(at);

		gstr1.setAt(ATDataList);
	}

	private void addATToGstin(AT at, Map<String, Object> mapRow) {
		at.setPos(mapRow.get(gstrColumnsObj.getColumnsValue("STATE")).toString());
		at.setSply_ty(mapRow.get(gstrColumnsObj.getColumnsValue("SUPP_TYPE")).toString());

		com.transformedge.gstr.at.entities.Itms itms = new com.transformedge.gstr.at.entities.Itms();
		List<com.transformedge.gstr.at.entities.Itms> items = new ArrayList<>();

		addAtItemsForGstin(itms,mapRow);

		items.add(itms);
		at.setItms(items);
	}

	private void addAtItemsForGstin(com.transformedge.gstr.at.entities.Itms itms, Map<String, Object> mapRow) {
		itms.setRt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_RATE")).toString()));
		itms.setAd_amt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ADV_RECEIPT_AMT")).toString()));
		itms.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
		itms.setCsamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CESS")).toString()));
	}

	/* =============================== TXPD =============================== */
	private void addGstinToTxpd(com.transformedge.gstr.txpd.entities.Gstr1 gstr1, Map<String, Object> mapRow,
			Gstr gstrColumnsObj2) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
		gstr1.setGt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("GROSS_TURNOVER")).toString()));
		gstr1.setCur_gt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CURRENT_GROSS_TURNOVER")).toString()));

		Txpd txpd = new Txpd();
		addTxpdToGstin(txpd, mapRow);

		List<Txpd> txpdDataList = new ArrayList<Txpd>();
		txpdDataList.add(txpd);

		gstr1.setTxpd(txpdDataList);		
	}

	private void addTxpdToGstin(Txpd txpd, Map<String, Object> mapRow) {
		txpd.setPos(mapRow.get(gstrColumnsObj.getColumnsValue("STATE")).toString());
		txpd.setSply_ty(mapRow.get(gstrColumnsObj.getColumnsValue("SUPP_TYPE")).toString());

		com.transformedge.gstr.txpd.entities.Itms itms = new com.transformedge.gstr.txpd.entities.Itms();
		List<com.transformedge.gstr.txpd.entities.Itms> items = new ArrayList<>();
		addTxpdItemsForGstin(itms,mapRow);

		items.add(itms);
		txpd.setItms(items);		
	}

	private void addTxpdItemsForGstin(com.transformedge.gstr.txpd.entities.Itms itms, Map<String, Object> mapRow) {
		itms.setRt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_RATE")).toString()));
		itms.setAd_amt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ADV_RECEIPT_AMT")).toString()));
		itms.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
		itms.setCsamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CESS")).toString()));
	}

	/* ========================== EXP ========================== */
	private void addGstinToExp(com.transformedge.gstr.exp.entities.Gstr1 gstr1, Map<String, Object> mapRow,
			Gstr gstrColumnsObj2) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
		gstr1.setGt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("GROSS_TURNOVER")).toString()));
		gstr1.setCur_gt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CURRENT_GROSS_TURNOVER")).toString()));

		Exp exp = new Exp();
		addExpToGstin(exp, mapRow);

		List<Exp> expDataList = new ArrayList<Exp>();
		expDataList.add(exp);

		gstr1.setExp(expDataList);			
	}

	private void addExpToGstin(Exp exp, Map<String, Object> mapRow) {
		exp.setExp_typ(mapRow.get(gstrColumnsObj.getColumnsValue("EXPORT_TYPE")).toString());

		com.transformedge.gstr.exp.entities.Inv inv = new com.transformedge.gstr.exp.entities.Inv();
		addExpInvoiceForGstin(inv,mapRow);

		List<com.transformedge.gstr.exp.entities.Inv> invoiceList = new ArrayList<>();
		invoiceList.add(inv);

		exp.setInv(invoiceList);
	}

	private void addExpInvoiceForGstin(com.transformedge.gstr.exp.entities.Inv inv, Map<String, Object> mapRow) {
		inv.setInum(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_NUMBER")).toString());
		inv.setIdt(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_DATE")).toString());
		inv.setVal(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_VALUE")).toString()));

		inv.setSbpcode(mapRow.get(gstrColumnsObj.getColumnsValue("SHIPPABLE_PORT_CODE")).toString());
		inv.setSbnum(mapRow.get(gstrColumnsObj.getColumnsValue("SHIPPABLE_BILL_NO")).toString());
		inv.setSbdt(mapRow.get(gstrColumnsObj.getColumnsValue("SHIPPABLE_BILL_DATE")).toString());

		List<com.transformedge.gstr.exp.entities.Itms> items = new ArrayList<>();
		com.transformedge.gstr.exp.entities.Itms item = new com.transformedge.gstr.exp.entities.Itms();
		addExpItemsForGstin(item,mapRow);
		items.add(item);
		inv.setItms(items);
	}

	private void addExpItemsForGstin(com.transformedge.gstr.exp.entities.Itms item, Map<String, Object> mapRow) {
		item.setTxval(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAXABLE_AMT")).toString()));
		item.setRt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_RATE")).toString()));
		item.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
	}

	/* ========================= HSN ============================= */
	private void addGstinToHsn(com.transformedge.gstr.hsn.entities.Gstr1 gstr1, Map<String, Object> mapRow,
			Gstr gstrColumnsObj2) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
		gstr1.setGt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("GROSS_TURNOVER")).toString()));
		gstr1.setCur_gt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CURRENT_GROSS_TURNOVER")).toString()));

		Hsn hsn = new Hsn();
		addHsnDataToGstin(hsn, mapRow);
		gstr1.setHsn(hsn);	
	}

	private void addHsnDataToGstin(Hsn hsn, Map<String, Object> mapRow) {

		Datas data = new Datas();
		addDataToHsnGstin(data,mapRow);

		List<Datas> hsnDataList = new ArrayList<>();
		hsnDataList.add(data);
		hsn.setData(hsnDataList);
	}

	private void addDataToHsnGstin(Datas data, Map<String, Object> mapRow) {
		data.setNum(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("SERIAL_NO")).toString()));
		data.setHsn_sc(mapRow.get(gstrColumnsObj.getColumnsValue("HSN_CODE")).toString());
		data.setDesc(mapRow.get(gstrColumnsObj.getColumnsValue("HSN_DESC")).toString());
		data.setUqc(mapRow.get(gstrColumnsObj.getColumnsValue("UOM")).toString());
		data.setQty(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("QTY")).toString()));
		data.setVal(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_VALUE")).toString()));
		data.setTxval(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAXABLE_AMT")).toString()));
		data.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
		data.setCsamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CESS")).toString()));
	}

	/* ==================================== CDNUR ======================== */
	private void addGstinToCdnur(com.transformedge.gstr.cdnur.entities.Gstr1 gstr1, Map<String, Object> mapRow,
			Gstr gstrColumnsObj2) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
		gstr1.setGt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("GROSS_TURNOVER")).toString()));
		gstr1.setCur_gt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CURRENT_GROSS_TURNOVER")).toString()));

		CDNUR cDNUR = new CDNUR();
		addCdnurToGstin(cDNUR, mapRow);

		List<CDNUR> cdnurDataList = new ArrayList<>();
		cdnurDataList.add(cDNUR);
		gstr1.setCdnur(cdnurDataList);
	}

	private void addCdnurToGstin(CDNUR cDNUR, Map<String, Object> mapRow) {

		cDNUR.setTyp(mapRow.get(gstrColumnsObj.getColumnsValue("ATTRIBUTE1")).toString());

		System.out.println("Note Type :::"+mapRow.get(gstrColumnsObj.getColumnsValue("NOTE_TYPE")));

		cDNUR.setNtty(mapRow.get(gstrColumnsObj.getColumnsValue("NOTE_TYPE")).toString());
		cDNUR.setNt_num(mapRow.get(gstrColumnsObj.getColumnsValue("TRX_NUMBER")).toString());
		cDNUR.setNt_dt(mapRow.get(gstrColumnsObj.getColumnsValue("TRX_DATE")).toString());
		cDNUR.setP_gst(mapRow.get(gstrColumnsObj.getColumnsValue("P_GST")).toString());
		cDNUR.setRsn(mapRow.get(gstrColumnsObj.getColumnsValue("RSN")).toString());
		cDNUR.setInum(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_NUMBER")).toString());
		cDNUR.setIdt(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_DATE")).toString());
		cDNUR.setVal(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_VALUE")).toString()));

		List<com.transformedge.gstr.cdnur.entities.Itms> items = new ArrayList<>();
		com.transformedge.gstr.cdnur.entities.Itms item = new com.transformedge.gstr.cdnur.entities.Itms();

		addCdnurItemsForGstin(item,mapRow);

		items.add(item);
		cDNUR.setItms(items);
	}

	private void addCdnurItemsForGstin(com.transformedge.gstr.cdnur.entities.Itms item, Map<String, Object> mapRow) {

		item.setNum(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("SERIAL_NO")).toString()));

		com.transformedge.gstr.cdnur.entities.Itm_det itm_det = new com.transformedge.gstr.cdnur.entities.Itm_det();

		itm_det.setRt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_RATE")).toString()));
		itm_det.setTxval(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAXABLE_AMT")).toString()));
		itm_det.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
		itm_det.setCsamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CESS")).toString()));

		item.setItm_det(itm_det);
	}

	/* ==============================CDNR ======================= */

	private void addGstinToCdnr(com.transformedge.gstr.cdnr.entities.Gstr1 gstr1, Map<String, Object> mapRow,
			Gstr gstrColumnsObj2) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
		gstr1.setGt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("GROSS_TURNOVER")).toString()));
		gstr1.setCur_gt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CURRENT_GROSS_TURNOVER")).toString()));

		CDNR cDNR = new CDNR();
		addCdnrToGstin(cDNR, mapRow);

		List<CDNR> cdnrDataList = new ArrayList<>();
		cdnrDataList.add(cDNR);
		gstr1.setCdnr(cdnrDataList);
	}

	private void addCdnrToGstin(CDNR cDNR, Map<String, Object> mapRow) {
		cDNR.setCtin(mapRow.get(gstrColumnsObj.getColumnsValue("THIRD_PARTY_REG_NUM")).toString());

		NT nt = new NT();
		addNTToCdnr(nt,mapRow);

		List<NT> ntCdnrList = new ArrayList<>();
		ntCdnrList.add(nt);
		cDNR.setNt(ntCdnrList);
	}

	private void addNTToCdnr(NT nt, Map<String, Object> mapRow) {
		nt.setNtty((char)mapRow.get(gstrColumnsObj.getColumnsValue("NOTE_TYPE")).toString().charAt(0));
		nt.setNt_num(mapRow.get(gstrColumnsObj.getColumnsValue("TRX_NUMBER")).toString());
		nt.setNt_dt(mapRow.get(gstrColumnsObj.getColumnsValue("TRX_DATE")).toString());
		nt.setP_gst(mapRow.get(gstrColumnsObj.getColumnsValue("P_GST")).toString());
		nt.setRsn(mapRow.get(gstrColumnsObj.getColumnsValue("RSN")).toString());
		nt.setInum(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_NUMBER")).toString());
		nt.setIdt(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_DATE")).toString());
		nt.setVal(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_VALUE")).toString()));

		List<com.transformedge.gstr.cdnr.entities.Itms> items = new ArrayList<>();
		com.transformedge.gstr.cdnr.entities.Itms item = new com.transformedge.gstr.cdnr.entities.Itms();

		addCdnrItemsForGstin(item,mapRow);

		items.add(item);
		nt.setItms(items);
	}

	private void addCdnrItemsForGstin(com.transformedge.gstr.cdnr.entities.Itms item, Map<String, Object> mapRow) {
		item.setNum(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("SERIAL_NO")).toString()));

		com.transformedge.gstr.cdnr.entities.Itm_det itm_det = new com.transformedge.gstr.cdnr.entities.Itm_det();

		itm_det.setRt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_RATE")).toString()));
		itm_det.setTxval(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAXABLE_AMT")).toString()));
		itm_det.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
		itm_det.setCsamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CESS")).toString()));

		item.setItm_det(itm_det);
	}

	/* ============================== NIL ======================= */
	private void addGstinToNil(com.transformedge.gstr.nil.entities.Gstr1 gstr1, Map<String, Object> mapRow) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
		gstr1.setGt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("GROSS_TURNOVER")).toString()));
		gstr1.setCur_gt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CURRENT_GROSS_TURNOVER")).toString()));

		NIL nil = new NIL();
		addNilDataToGstin(nil, mapRow);
		gstr1.setNil(nil);	

	}

	private void addNilDataToGstin(NIL nil, Map<String, Object> mapRow) {
		com.transformedge.gstr.nil.entities.Inv inv = new com.transformedge.gstr.nil.entities.Inv();
		addInvToNilForGstin(inv,mapRow);
		List<com.transformedge.gstr.nil.entities.Inv> invNilList = new ArrayList<>();
		invNilList.add(inv);
		nil.setInv(invNilList);
	}

	private void addInvToNilForGstin(com.transformedge.gstr.nil.entities.Inv inv, Map<String, Object> mapRow) {
		inv.setSply_ty(mapRow.get(gstrColumnsObj.getColumnsValue("SUPP_TYPE")).toString());
		inv.setNil_amt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("NIL_AMOUNT")).toString()));
		inv.setNgsup_amt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("NGSUP_AMOUNT")).toString()));
		inv.setExpt_amt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("EXPT_AMOUNT")).toString()));
	}
	
	/* ================================= DOC_ISSUE ============================= */
	private void addGstinToDocIssue(com.transformedge.gstr.docissue.entities.Gstr1 gstr1, Map<String, Object> mapRow) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
		gstr1.setGt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("GROSS_TURNOVER")).toString()));
		gstr1.setCur_gt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CURRENT_GROSS_TURNOVER")).toString()));

		DocIssue docIssue = new DocIssue();
		addDocIssueToGstin(docIssue, mapRow);
		gstr1.setDoc_issue(docIssue);	
	}

	private void addDocIssueToGstin(DocIssue docIssue, Map<String, Object> mapRow) {
			DocDet docDet = new DocDet();
			
			addDocDetToDocIssue(docDet,mapRow);
			
			List<DocDet> docDetList = new ArrayList<>();
			docDetList.add(docDet);
			docIssue.setDoc_det(docDetList);
	}

	private void addDocDetToDocIssue(DocDet docDet, Map<String, Object> mapRow) {
		docDet.setDoc_num(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("ATTRIBUTE2")).toString()));
		
		Docs docs = new Docs();
		addDocsToDocDet(docs,mapRow);
		
		List<Docs>  docsList = new ArrayList<>();
		docsList.add(docs);
		docDet.setDocs(docsList);
	}

	private void addDocsToDocDet(Docs docs, Map<String, Object> mapRow) {
		docs.setNum(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("SERIAL_NO")).toString()));
		docs.setFrom(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("ATTRIBUTE3")).toString()));
		docs.setTo(mapRow.get(gstrColumnsObj.getColumnsValue("ATTRIBUTE4")).toString());
		docs.setTotnum(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("ATTRIBUTE5")).toString()));
		docs.setCancel(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("ATTRIBUTE6")).toString()));
		docs.setNet_issue(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("ATTRIBUTE7")).toString()));
	}
}
