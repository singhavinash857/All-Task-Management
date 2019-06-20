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
import com.transformedge.gstr.b2b.entities.B2b;
import com.transformedge.gstr.b2b.entities.GstValue;
import com.transformedge.gstr.b2b.entities.Gstr1;
import com.transformedge.gstr.b2b.entities.Inv;
import com.transformedge.gstr.b2b.entities.ItcDetails;
import com.transformedge.gstr.b2b.entities.ItemDetails;
import com.transformedge.gstr.b2b.entities.Itms;
import com.transformedge.gstr.b2bur.entities.B2bur;
import com.transformedge.gstr.b2bur.entities.Itcdetails;
import com.transformedge.gstr.b2bur.entities.Itemdetails;
import com.transformedge.gstr.b2bur.entities.Items;
import com.transformedge.gstr.cdn.entities.Cdn;
import com.transformedge.gstr.cdn.entities.Itcdet;
import com.transformedge.gstr.cdn.entities.Itemdet;
import com.transformedge.gstr.cdn.entities.Nt;
import com.transformedge.gstr.cdnur.entities.Cdnur;
import com.transformedge.gstr.configuration.GstrColumnsCofiguration;
import com.transformedge.gstr.configuration.GstrColumnsCofiguration.Gstr;
import com.transformedge.gstr.configuration.TableMetadataConfiguration;
import com.transformedge.gstr.hsnsum.entities.Det;
import com.transformedge.gstr.hsnsum.entities.HsnSum;
import com.transformedge.gstr.impg.entities.Impg;

@Component
public class SaveGstrProcessor implements Processor {
	private static final Object ctinumber = null;

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
		System.out.println(" gstrColumns :::::"+gstrColumns);

		GstValue gstValueB2B = null;
		HashMap<String, GstValue> gstrInvoiceB2bMap = new HashMap<>();

		com.transformedge.gstr.b2bur.entities.GstValue gstValueB2Bur = null;
		HashMap<String, com.transformedge.gstr.b2bur.entities.GstValue> gstrInvoiceB2burMap = new HashMap<>();

		com.transformedge.gstr.hsnsum.entities.GstValue gstValueHsnSum = null;
		HashMap<String, com.transformedge.gstr.hsnsum.entities.GstValue> gstrInvoiceHsnSumMap = new HashMap<>();

		com.transformedge.gstr.impg.entities.GstValue gstValueImpg = null;
		HashMap<String, com.transformedge.gstr.impg.entities.GstValue> gstrInvoiceImpgMap = new HashMap<>();

		com.transformedge.gstr.cdn.entities.GstValue gstValueCdn = null;
		HashMap<String, com.transformedge.gstr.cdn.entities.GstValue> gstrInvoiceCdnMap = new HashMap<>();

		com.transformedge.gstr.cdnur.entities.GstValue gstValueCdnur = null;
		HashMap<String, com.transformedge.gstr.cdnur.entities.GstValue> gstrInvoiceCdnurMap = new HashMap<>();

		for(Map<String, Object> mapRow : received){
			Object identifierColumn = mapRow.get(gstrTable.getIdentifierColumn());

			Object sectionCode = mapRow.get(gstrColumnsObj.getColumnsValue("SOURCE_SECTION"));
			Object ctinNumber = mapRow.get(gstrColumnsObj.getColumnsValue("THIRD_PARTY_REG_NUM"));
			Object inum = mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_NUMBER"));
			Object hsnCode = mapRow.get(gstrColumnsObj.getColumnsValue("HSN_CODE"));

			switch(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase())){
			case "B2B" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("B2B")){
					gstValueB2B =  gstrInvoiceB2bMap.getOrDefault(identifierColumn, null);
					B2b b2b = null;
					Inv inv = null;
					if(gstValueB2B != null){
						b2b = gstValueB2B.getGstValue().getB2b().stream().filter(b -> b.getCtin().equals(ctinNumber)).findAny()
								.orElse(null);
						if(b2b != null){
							inv = b2b.getInv().stream().filter(i -> i.getInum().equals(inum)).findAny().orElse(null);
							if(inv != null){
								Itms itms = new Itms();
								addItemsToB2b(itms,mapRow);
								inv.getItms().add(itms);
							}else{
								inv = new Inv();
								addInvoiceForGstin(inv,mapRow);
								b2b.getInv().add(inv);
							}
						}else{
							b2b = new B2b();
							addB2bToGstin(b2b,mapRow);
							gstValueB2B.getGstValue().getB2b().add(b2b);
						}
					}else{
						gstValueB2B = new GstValue();
						Gstr1 gstr1 = new Gstr1();
						addGstinToB2b(gstr1,mapRow,gstrColumnsObj);
						gstValueB2B.setGstValue(gstr1);
						gstrInvoiceB2bMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueB2B);
					}
				}
				ObjectMapper mapper = new ObjectMapper();
				String jsonInStringB2b = mapper.writeValueAsString(gstValueB2B);
				System.out.println("jsonInStringB2b ::"+jsonInStringB2b);
				exchange.setProperty("proccessCode", "B2B");
				break;
			case "B2BUR" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("B2BUR")){
					gstValueB2Bur =  gstrInvoiceB2burMap.getOrDefault(identifierColumn, null);
					com.transformedge.gstr.b2bur.entities.Inv inv = null;
					B2bur b2bur = null;
					if(gstValueB2Bur != null){
						b2bur = gstValueB2Bur.getGstValue().getB2bur().get(0);
						if(b2bur != null){
							inv = b2bur.getInv().stream().filter(i -> i.getInum().equals(inum)).findAny().orElse(null);
							if(inv != null){
								Items itms = new Items();
								addB2burItemsToGsting(itms, mapRow);
								inv.getItms().add(itms);
							}else{
								inv = new com.transformedge.gstr.b2bur.entities.Inv();
								addB2burInvToGstin(inv,mapRow);
								b2bur.getInv().add(inv);
							}
						}
					}else{
						gstValueB2Bur = new com.transformedge.gstr.b2bur.entities.GstValue();
						com.transformedge.gstr.b2bur.entities.Gstr1 gstr1 = new com.transformedge.gstr.b2bur.entities.Gstr1();
						addGstinToB2bur(gstr1,mapRow);
						gstValueB2Bur.setGstValue(gstr1);
						gstrInvoiceB2burMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueB2Bur);
					}
				}

				ObjectMapper mapperB2bur = new ObjectMapper();
				String jsonInStringB2bur = mapperB2bur.writeValueAsString(gstValueB2Bur);
				System.out.println("jsonInStringB2bur ::"+jsonInStringB2bur);
				exchange.setProperty("proccessCode", "B2BUR");
				break;
			case "HSNSUM" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("HSNSUM")){
					gstValueHsnSum =  gstrInvoiceHsnSumMap.getOrDefault(identifierColumn, null);
					Det det = null;
					if(gstValueHsnSum != null){
						det =  gstValueHsnSum.getGstValue().getHsnsum().getDet().stream().filter(d -> d.getHsn_sc().equals(hsnCode)).findAny().orElse(null);
						if(det != null){

						}else{
							det = new Det();
							addDetoHsnSum(det,mapRow);
							gstValueHsnSum.getGstValue().getHsnsum().getDet().add(det);
						}
					}else{
						gstValueHsnSum = new com.transformedge.gstr.hsnsum.entities.GstValue();
						com.transformedge.gstr.hsnsum.entities.Gstr1 gstr1 = new com.transformedge.gstr.hsnsum.entities.Gstr1();
						addHsnSumToGstin(gstr1,mapRow);
						gstValueHsnSum.setGstValue(gstr1);
						gstrInvoiceHsnSumMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueHsnSum);
					}
				}
				ObjectMapper mapperHsnSum = new ObjectMapper();
				String jsonInStringHsnSum = mapperHsnSum.writeValueAsString(gstValueHsnSum);
				System.out.println("jsonInStringHsnSum ::"+jsonInStringHsnSum);
				exchange.setProperty("proccessCode", "HSNSUM");
				break;
			case "IMPG" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("IMPG")){
					gstValueImpg =  gstrInvoiceImpgMap.getOrDefault(identifierColumn, null);
					Impg impg = null;
					com.transformedge.gstr.impg.entities.Inv inv = null;
					if(gstValueImpg != null){
						impg = gstValueImpg.getGstValue().getImp_g().get(0);
						if(impg != null){
							inv = impg.getInv().stream().filter(i -> i.getStin().equals(ctinNumber)).findAny().orElse(null);
							if(inv != null){
								com.transformedge.gstr.impg.entities.Items itms = new com.transformedge.gstr.impg.entities.Items();
								addItemsToImpgInv(itms, mapRow);
								inv.getItms().add(itms);
							}else{
								inv  = new com.transformedge.gstr.impg.entities.Inv();
								addInvToImpg(inv, mapRow);
								impg.getInv().add(inv);
							}
						}
					}else{
						gstValueImpg = new com.transformedge.gstr.impg.entities.GstValue();
						com.transformedge.gstr.impg.entities.Gstr1 gstr1 = new com.transformedge.gstr.impg.entities.Gstr1();
						addImpgToGstin(gstr1,mapRow);
						gstValueImpg.setGstValue(gstr1);
						gstrInvoiceImpgMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueImpg);
					}
				}
				ObjectMapper mapperImpg = new ObjectMapper();
				String jsonInStringImpg = mapperImpg.writeValueAsString(gstValueImpg);
				System.out.println("jsonInStringImpg ::"+jsonInStringImpg);
				exchange.setProperty("proccessCode", "IMPG");
				break;
			case "CDN" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("CDN")){
					gstValueCdn =  gstrInvoiceCdnMap.getOrDefault(identifierColumn, null);
					Cdn cdn = null;
					Nt nt = null;
					if(gstValueCdn != null){
						cdn = gstValueCdn.getGstValue().getCdn().stream().filter(b -> b.getCtin().equals(ctinNumber)).findAny()
								.orElse(null);
						if(cdn != null){
							nt = cdn.getNt().stream().filter(i -> i.getInum().equals(inum)).findAny().orElse(null);
							if(nt != null){
								com.transformedge.gstr.cdn.entities.Items itms = new com.transformedge.gstr.cdn.entities.Items();
								addItemsToNt(itms,mapRow);
								nt.getItms().add(itms);
							}else{
								nt = new Nt();
								addNtToCtin(nt,mapRow);
								cdn.getNt().add(nt);
							}
						}else{
							cdn = new Cdn();
							addCdn(cdn,mapRow);
							gstValueCdn.getGstValue().getCdn().add(cdn);
						}
					}else{
						gstValueCdn = new com.transformedge.gstr.cdn.entities.GstValue();
						com.transformedge.gstr.cdn.entities.Gstr1 gstr1 = new com.transformedge.gstr.cdn.entities.Gstr1();
						addCdnToGstin(gstr1,mapRow);
						gstValueCdn.setGstValue(gstr1);
						gstrInvoiceCdnMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueCdn);
					}
				}
				ObjectMapper mapperCdn = new ObjectMapper();
				String jsonInStringCdn = mapperCdn.writeValueAsString(gstValueCdn);
				System.out.println("jsonInStringCdn ::"+jsonInStringCdn);
				exchange.setProperty("proccessCode", "CDN");
				break;
			case "CDNUR" :
				if(gstrColumnsObj.getComponent(sectionCode.toString().toUpperCase()).equalsIgnoreCase("CDNUR")){
					gstValueCdnur =  gstrInvoiceCdnurMap.getOrDefault(identifierColumn, null);
					Cdnur cdnur = null;
					if(gstValueCdnur != null){
						cdnur = gstValueCdnur.getGstValue().getCdnur().stream().filter(b -> b.getRtin().equals(ctinNumber)).findAny()
								.orElse(null);
						if(cdnur != null){
							com.transformedge.gstr.cdnur.entities.Items itms = new com.transformedge.gstr.cdnur.entities.Items();
							addItemsToCdnur(itms,mapRow);
							cdnur.getItms().add(itms);
						}else{
							cdnur = new Cdnur();
							addCdnur(cdnur , mapRow);
							gstValueCdnur.getGstValue().getCdnur().add(cdnur);
						}
					}else{
						gstValueCdnur = new com.transformedge.gstr.cdnur.entities.GstValue();
						com.transformedge.gstr.cdnur.entities.Gstr1 gstr1 = new com.transformedge.gstr.cdnur.entities.Gstr1();
						addCdnurToGstin(gstr1,mapRow);
						gstValueCdnur.setGstValue(gstr1);
						gstrInvoiceCdnurMap.put(mapRow.get(gstrTable.getIdentifierColumn()).toString(), gstValueCdnur);
					}
				}
				ObjectMapper mapperCdnur = new ObjectMapper();
				String jsonInStringCdnur = mapperCdnur.writeValueAsString(gstValueCdnur);
				System.out.println("jsonInStringCdnur ::"+jsonInStringCdnur);
				exchange.setProperty("proccessCode", "CDNUR");
				break;	
			default: 
				break;
			}
		}
		if(gstValueB2B != null){
			Collection<GstValue> gstrInvoices = gstrInvoiceB2bMap.values();
			exchange.getOut().setBody(gstrInvoices);
		}else if(gstValueB2Bur != null){
			Collection<com.transformedge.gstr.b2bur.entities.GstValue> gstrInvoices = gstrInvoiceB2burMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueHsnSum != null){
			Collection<com.transformedge.gstr.hsnsum.entities.GstValue> gstrInvoices = gstrInvoiceHsnSumMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueCdn != null){
			Collection<com.transformedge.gstr.cdn.entities.GstValue> gstrInvoices = gstrInvoiceCdnMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueCdnur != null){
			Collection<com.transformedge.gstr.cdnur.entities.GstValue> gstrInvoices = gstrInvoiceCdnurMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else if(gstValueImpg != null){
			Collection<com.transformedge.gstr.impg.entities.GstValue> gstrInvoices = gstrInvoiceImpgMap.values();
			exchange.getOut().setBody(gstrInvoices);

		}else{
			logger.error("Please check your SaveGstrProcessor exchange body");
		}
	}

	/* ====================================== B2B ================================= */
	private void addGstinToB2b(Gstr1 gstr1, Map<String, Object> mapRow, Gstr gstrColumnsObj2) {

		addGstinNumberToB2b(gstr1,mapRow);

		B2b b2b = new B2b();
		addB2bToGstin(b2b, mapRow);

		List<B2b> b2bDataList = new ArrayList<>();
		b2bDataList.add(b2b);

		gstr1.setB2b(b2bDataList);
	}


	private void addB2bToGstin(B2b b2b, Map<String, Object> mapRow) {
		b2b.setCtin(mapRow.get(gstrColumnsObj.getColumnsValue("THIRD_PARTY_REG_NUM")).toString());

		Inv inv = new Inv();
		addInvoiceForGstin(inv,mapRow);
		List<Inv> invoiceList = new ArrayList<Inv>();
		invoiceList.add(inv);
		b2b.setInv(invoiceList);		
	}


	private void addInvoiceForGstin(Inv inv, Map<String, Object> mapRow) {

		inv.setInum(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_NUMBER")).toString());
		inv.setIdt(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_DATE")).toString());
		inv.setVal(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_VALUE")).toString()));
		inv.setRchrg(mapRow.get(gstrColumnsObj.getColumnsValue("REV_CHARGE")).toString());
		inv.setPos(mapRow.get(gstrColumnsObj.getColumnsValue("POS")).toString());
		inv.setInv_typ(mapRow.get(gstrColumnsObj.getColumnsValue("INV_TYPE")).toString());

		List<Itms> items = new ArrayList<Itms>();
		Itms item = new Itms();
		addItemsToB2b(item,mapRow);
		items.add(item);
		inv.setItms(items);
	}


	private void addItemsToB2b(Itms itms, Map<String, Object> mapRow) {

		itms.setNum(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("SERIAL_NUM")).toString()));

		ItemDetails itm_det = new ItemDetails();
		ItcDetails itc = new ItcDetails();

		addItm_detailsToB2b(itm_det,mapRow);
		addItc_DetailsToB2b(itc,mapRow);

		itms.setItm_det(itm_det);
		itms.setItc(itc);

	}


	private void addItc_DetailsToB2b(ItcDetails itc, Map<String, Object> mapRow) {
		itc.setElg(mapRow.get(gstrColumnsObj.getColumnsValue("ELG")).toString());
		itc.setTx_i(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_IGST")).toString()));
		itc.setTx_s(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_SGST")).toString()));
		itc.setTx_c(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_CGST")).toString()));
		itc.setTx_cs(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_CESS")).toString()));
	}


	private void addItm_detailsToB2b(ItemDetails itm_det, Map<String, Object> mapRow) {
		itm_det.setRt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_RATE")).toString()));
		itm_det.setTxval(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAXABLE_AMT")).toString()));
		itm_det.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
		itm_det.setSamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("SGST")).toString()));
		itm_det.setCamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CGST")).toString()));
		itm_det.setCsamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CESS")).toString()));
	}


	private void addGstinNumberToB2b(Gstr1 gstr1, Map<String, Object> mapRow) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
	}

	/* =============================== B2BUR ========================== */
	private void addGstinToB2bur(com.transformedge.gstr.b2bur.entities.Gstr1 gstr1, Map<String, Object> mapRow) {
		addGstinNumberToB2bur(gstr1,mapRow);

		B2bur b2bur = new B2bur();
		
		com.transformedge.gstr.b2bur.entities.Inv inv = new com.transformedge.gstr.b2bur.entities.Inv();
		addB2burInvToGstin(inv, mapRow);
		List<com.transformedge.gstr.b2bur.entities.Inv> invList = new ArrayList<>();
		invList.add(inv);
		
		b2bur.setInv(invList);
		
		List<B2bur> b2burList = new ArrayList<>();
		b2burList.add(b2bur);

		gstr1.setB2bur(b2burList);
	}


	private void addGstinNumberToB2bur(com.transformedge.gstr.b2bur.entities.Gstr1 gstr1, Map<String, Object> mapRow) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
	}


	private void addB2burInvToGstin(com.transformedge.gstr.b2bur.entities.Inv inv, Map<String, Object> mapRow) {
		inv.setChkSUM(mapRow.get(gstrColumnsObj.getColumnsValue("CHKSUM")).toString());
		inv.setInum(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_NUMBER")).toString());
		inv.setIdt(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_DATE")).toString());
		inv.setVal(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_VALUE")).toString()));
		inv.setPos(mapRow.get(gstrColumnsObj.getColumnsValue("POS")).toString());
		inv.setSply_ty(mapRow.get(gstrColumnsObj.getColumnsValue("SUPP_TYPE")).toString());

		List<Items> items = new ArrayList<>();
		Items item = new Items();
		addB2burItemsToGsting(item,mapRow);

		items.add(item);
		inv.setItms(items);
	}

	private void addB2burItemsToGsting(Items items, Map<String, Object> mapRow) {
		items.setNum(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("SERIAL_NUM")).toString()));

		Itemdetails itm_det = new Itemdetails();
		Itcdetails itc = new Itcdetails();

		addB2burItm_detailsToGstin(itm_det,mapRow);
		addB2burItc_DetailsToGstin(itc,mapRow);

		items.setItm_det(itm_det);
		items.setItc(itc);
	}

	private void addB2burItc_DetailsToGstin(Itcdetails itc, Map<String, Object> mapRow) {
		itc.setElg(mapRow.get(gstrColumnsObj.getColumnsValue("ELG")).toString());
		itc.setTx_i(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_IGST")).toString()));
		itc.setTx_s(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_SGST")).toString()));
		itc.setTx_c(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_CGST")).toString()));
		itc.setTx_cs(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_CESS")).toString()));
	}

	private void addB2burItm_detailsToGstin(Itemdetails itm_det, Map<String, Object> mapRow) {
		itm_det.setRt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_RATE")).toString()));
		itm_det.setTxval(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAXABLE_AMT")).toString()));
		itm_det.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
		itm_det.setSamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("SGST")).toString()));
		itm_det.setCamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CGST")).toString()));
		itm_det.setCsamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CESS")).toString()));
	}

	/* ============================= HSNSUM ============================= */
	private void addHsnSumToGstin(com.transformedge.gstr.hsnsum.entities.Gstr1 gstr1, Map<String, Object> mapRow) {
		addGstinNumberToHsnSum(gstr1,mapRow);
		HsnSum hsnSum = new HsnSum();
		addHsnSum(hsnSum,mapRow);
		gstr1.setHsnsum(hsnSum);
	}

	private void addHsnSum(HsnSum hsnSum, Map<String, Object> mapRow) {
		Det det = new Det();
		addDetoHsnSum(det,mapRow);
		List<Det> detList = new ArrayList<>();
		detList.add(det);
		hsnSum.setDet(detList);
	}

	private void addDetoHsnSum(Det det, Map<String, Object> mapRow) {
		det.setNum(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("SERIAL_NUM")).toString()));
		det.setHsn_sc(mapRow.get(gstrColumnsObj.getColumnsValue("HSN_CODE")).toString());
		det.setDesc(mapRow.get(gstrColumnsObj.getColumnsValue("HSN_DESC")).toString());
		det.setUqc(mapRow.get(gstrColumnsObj.getColumnsValue("UOM")).toString());
		det.setQty(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("QTY")).toString()));
		det.setVal(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_VALUE")).toString()));
		det.setTxval(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAXABLE_AMT")).toString()));
		det.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
		det.setCamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CGST")).toString()));
		det.setSamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("SGST")).toString()));
		det.setCsamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CESS")).toString()));
	}

	private void addGstinNumberToHsnSum(com.transformedge.gstr.hsnsum.entities.Gstr1 gstr1,
			Map<String, Object> mapRow) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
	}

	/* =============================== CDN ============================== */
	private void addCdnToGstin(com.transformedge.gstr.cdn.entities.Gstr1 gstr1, Map<String, Object> mapRow) {
		addGstinToCdn(gstr1,mapRow);

		Cdn cdn = new Cdn();
		List<Cdn> cdnList = new ArrayList<>();

		addCdn(cdn, mapRow);

		cdnList.add(cdn);
        gstr1.setCdn(cdnList);

	}
	private void addCdn(Cdn cdn, Map<String, Object> mapRow) {
		cdn.setCtin(mapRow.get(gstrColumnsObj.getColumnsValue("THIRD_PARTY_REG_NUM")).toString());
		Nt nt = new Nt();
		List<Nt> ntList = new ArrayList<>();
		
		addNtToCtin(nt,mapRow);
		
		ntList.add(nt);
		cdn.setNt(ntList);
	}

	private void addNtToCtin(Nt nt, Map<String, Object> mapRow) {
		nt.setNtty(mapRow.get(gstrColumnsObj.getColumnsValue("NOTE_TYPE")).toString());
		nt.setNt_num(mapRow.get(gstrColumnsObj.getColumnsValue("NOTE_NUMBER")).toString());
		nt.setNt_dt(mapRow.get(gstrColumnsObj.getColumnsValue("NOTE_DATE")).toString());
		nt.setRsn(mapRow.get(gstrColumnsObj.getColumnsValue("RSN")).toString());
		nt.setP_gst(mapRow.get(gstrColumnsObj.getColumnsValue("P_GST")).toString());
		nt.setInum(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_NUMBER")).toString());
		nt.setIdt(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_DATE")).toString());
		nt.setVal(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_VALUE")).toString()));

		com.transformedge.gstr.cdn.entities.Items itms = new com.transformedge.gstr.cdn.entities.Items();
		addItemsToNt(itms,mapRow);
		List<com.transformedge.gstr.cdn.entities.Items> itmsList = new ArrayList<>();
		itmsList.add(itms);
		nt.setItms(itmsList);
	}

	private void addItemsToNt(com.transformedge.gstr.cdn.entities.Items itms, Map<String, Object> mapRow) {
		itms.setNum(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("SERIAL_NUM")).toString()));

		Itemdet itm_det = new Itemdet();
		Itcdet itc = new Itcdet();

		addCdnItm_detailsToGstin(itm_det,mapRow);
		addCdnItc_DetailsToGstin(itc,mapRow);

		itms.setItm_det(itm_det);
		itms.setItc(itc);
	}

	private void addCdnItc_DetailsToGstin(Itcdet itc, Map<String, Object> mapRow) {
		itc.setElg(mapRow.get(gstrColumnsObj.getColumnsValue("ELG")).toString());
		itc.setTx_i(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_IGST")).toString()));
		itc.setTx_s(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_SGST")).toString()));
		itc.setTx_c(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_CGST")).toString()));
		itc.setTx_cs(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_CESS")).toString()));
	}

	private void addCdnItm_detailsToGstin(Itemdet itm_det, Map<String, Object> mapRow) {
		itm_det.setRt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_RATE")).toString()));
		itm_det.setTxval(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAXABLE_AMT")).toString()));
		itm_det.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
		itm_det.setSamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("SGST")).toString()));
		itm_det.setCamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CGST")).toString()));
		itm_det.setCsamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CESS")).toString()));
	}

	private void addGstinToCdn(com.transformedge.gstr.cdn.entities.Gstr1 gstr1, Map<String, Object> mapRow) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
	}

	/* ============================== CDNUR ============================ */

	private void addCdnurToGstin(com.transformedge.gstr.cdnur.entities.Gstr1 gstr1, Map<String, Object> mapRow) {
		addGstingToCdnur(gstr1,mapRow);
		Cdnur cdnur = new Cdnur();
		List<Cdnur> cdnurList = new ArrayList<>();
		addCdnur(cdnur , mapRow);
		cdnurList.add(cdnur);
		gstr1.setCdnur(cdnurList);
	}

	private void addCdnur(Cdnur cdnur, Map<String, Object> mapRow) {
		cdnur.setRtin(mapRow.get(gstrColumnsObj.getColumnsValue("THIRD_PARTY_REG_NUM")).toString());
		cdnur.setNtty(mapRow.get(gstrColumnsObj.getColumnsValue("NOTE_TYPE")).toString());
		cdnur.setNt_num(mapRow.get(gstrColumnsObj.getColumnsValue("NOTE_NUMBER")).toString());
		cdnur.setNt_dt(mapRow.get(gstrColumnsObj.getColumnsValue("NOTE_DATE")).toString());
		cdnur.setRsn(mapRow.get(gstrColumnsObj.getColumnsValue("RSN")).toString());
		cdnur.setP_gst(mapRow.get(gstrColumnsObj.getColumnsValue("P_GST")).toString());
		cdnur.setInum(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_NUMBER")).toString());
		cdnur.setIdt(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_DATE")).toString());
		cdnur.setVal(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_VALUE")).toString()));

		List<com.transformedge.gstr.cdnur.entities.Items> items = new ArrayList<>();
		com.transformedge.gstr.cdnur.entities.Items item = new com.transformedge.gstr.cdnur.entities.Items();
		addItemsToCdnur(item,mapRow);
		items.add(item);
		cdnur.setItms(items);
	}

	private void addItemsToCdnur(com.transformedge.gstr.cdnur.entities.Items item, Map<String, Object> mapRow) {
		item.setNum(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("SERIAL_NUM")).toString()));
		com.transformedge.gstr.cdnur.entities.Itemdet itm_det = new com.transformedge.gstr.cdnur.entities.Itemdet();
		com.transformedge.gstr.cdnur.entities.Itcdet itc = new com.transformedge.gstr.cdnur.entities.Itcdet();

		addCdnurItm_detailsToGstin(itm_det,mapRow);
		addCdnurItc_DetailsToGstin(itc,mapRow);

		item.setItm_det(itm_det);
		item.setItc(itc);
	}

	private void addCdnurItc_DetailsToGstin(com.transformedge.gstr.cdnur.entities.Itcdet itc,
			Map<String, Object> mapRow) {
		itc.setElg(mapRow.get(gstrColumnsObj.getColumnsValue("ELG")).toString());
		itc.setTx_i(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_IGST")).toString()));
		itc.setTx_s(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_SGST")).toString()));
		itc.setTx_c(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_CGST")).toString()));
		itc.setTx_cs(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_CESS")).toString()));
	}

	private void addCdnurItm_detailsToGstin(com.transformedge.gstr.cdnur.entities.Itemdet itm_det,
			Map<String, Object> mapRow) {
		itm_det.setRt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_RATE")).toString()));
		itm_det.setTxval(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAXABLE_AMT")).toString()));
		itm_det.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
		itm_det.setSamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("SGST")).toString()));
		itm_det.setCamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CGST")).toString()));
		itm_det.setCsamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CESS")).toString()));
	}

	private void addGstingToCdnur(com.transformedge.gstr.cdnur.entities.Gstr1 gstr1, Map<String, Object> mapRow) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
	}

	/* ================================ IMPG ======================== */
	private void addImpgToGstin(com.transformedge.gstr.impg.entities.Gstr1 gstr1, Map<String, Object> mapRow) {
		addGstingToImpg(gstr1, mapRow);
		Impg impg = new Impg();
		addImpg(impg, mapRow);
		List<Impg> impgList = new ArrayList<>();
		impgList.add(impg);
		gstr1.setImp_g(impgList);
	}

	private void addImpg(Impg impg, Map<String, Object> mapRow) {
		com.transformedge.gstr.impg.entities.Inv inv = new com.transformedge.gstr.impg.entities.Inv();
		addInvToImpg(inv,mapRow);
		List<com.transformedge.gstr.impg.entities.Inv> invList = new ArrayList<>();
		invList.add(inv);
		impg.setInv(invList);
	}

	private void addInvToImpg(com.transformedge.gstr.impg.entities.Inv inv, Map<String, Object> mapRow) {
		inv.setIs_sez(mapRow.get(gstrColumnsObj.getColumnsValue("IS_SEZ")).toString());
		inv.setStin(mapRow.get(gstrColumnsObj.getColumnsValue("THIRD_PARTY_REG_NUM")).toString());
		inv.setBoe_num(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_NUMBER")).toString()));
		inv.setBoe_dt(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_DATE")).toString());
		inv.setBoe_val(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_INVOICE_VALUE")).toString()));
		inv.setPort_code(mapRow.get(gstrColumnsObj.getColumnsValue("PORT_CODE")).toString());

		com.transformedge.gstr.impg.entities.Items itms = new com.transformedge.gstr.impg.entities.Items();
		List<com.transformedge.gstr.impg.entities.Items> itmsList = new ArrayList<>();
		addItemsToImpgInv(itms , mapRow);
		itmsList.add(itms);
		inv.setItms(itmsList);
	}

	private void addItemsToImpgInv(com.transformedge.gstr.impg.entities.Items itms, Map<String, Object> mapRow) {
		itms.setNum(Integer.parseInt(mapRow.get(gstrColumnsObj.getColumnsValue("SERIAL_NUM")).toString()));
		itms.setTxval(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAXABLE_AMT")).toString()));
		itms.setRt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("TAX_RATE")).toString()));
		itms.setIamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("IGST")).toString()));
		itms.setCsamt(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("CESS")).toString()));
		itms.setElg(mapRow.get(gstrColumnsObj.getColumnsValue("ELG")).toString());
		itms.setTx_i(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_IGST")).toString()));
		itms.setTx_cs(Double.parseDouble(mapRow.get(gstrColumnsObj.getColumnsValue("ITC_CESS")).toString()));
	}

	private void addGstingToImpg(com.transformedge.gstr.impg.entities.Gstr1 gstr1, Map<String, Object> mapRow) {
		gstr1.setGstin(mapRow.get(gstrColumnsObj.getColumnsValue("FIRST_PARTY_PRIMARY_REG_NUM")).toString());
		gstr1.setFp(mapRow.get(gstrColumnsObj.getColumnsValue("PERIOD_NAME")).toString());
	}

}
