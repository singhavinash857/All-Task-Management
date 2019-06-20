package com.transformedge.gstr.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.transformedge.gstr.b2b.entities.SearchRequest;
import com.transformedge.gstr.b2b.entities.SearchResponse;
import com.transformedge.gstr.b2b.entities.TransactionAudit;
import com.transformedge.gstr.configuration.AuditConfiguration;
import com.transformedge.gstr.configuration.QueryConfiguration;
import com.transformedge.gstr.configuration.TableMetadataConfiguration;
import com.transformedge.gstr.models.Item;

@Service
public class AuditService {

	private RestTemplate restTemplate;

	//    @Autowired
	//    private JdbcTemplate jdbcTemplate;

	@Autowired
	private AuditConfiguration auditConfiguration;

	@Autowired
	private QueryConfiguration queryConfiguration;
	@Autowired
	private TableMetadataConfiguration tableMetadataConfiguration;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public AuditService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public TransactionAudit findTransactionAudit(String gstrInvoiceId) {
		SearchRequest searchRequest = new SearchRequest();
		SearchRequest.Selector selector = new SearchRequest.Selector();

		Map<String, Object> fields = new HashMap<>();

		fields.put("type", "transaction");
		fields.put("gstin", gstrInvoiceId);

		selector.setFields(fields);
		searchRequest.setSelector(selector);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		System.out.println("searchRequest.toJSON() :::::"+searchRequest.toJSON());
		
		HttpEntity<String> entity = new HttpEntity<String>(searchRequest.toJSON(), headers);
		String response = restTemplate.postForObject(auditConfiguration.getUrl() + "/_find", entity, String.class);
		logger.debug("Find transaction response: " + response);
		SearchResponse searchResponse = SearchResponse.fromJSON(response);
		logger.debug("SearchResposne: " + searchResponse);
        List<TransactionAudit> transactionAuditList = searchResponse.extractResponse(TransactionAudit.class);
	  //  System.out.println("transactionAuditList ::"+transactionAuditList);
		return transactionAuditList.isEmpty() ? null : transactionAuditList.get(0);
	}


}
