package com.transformedge.teewb.service;

import com.transformedge.teewb.config.AuditConfiguration;
import com.transformedge.teewb.config.QueryConfiguration;
import com.transformedge.teewb.config.TableMetadataConfiguration;
import com.transformedge.teewb.entity.SearchRequest;
import com.transformedge.teewb.entity.SearchResponse;
import com.transformedge.teewb.entity.TransactionAudit;
import com.transformedge.teewb.request.Invoice;
import com.transformedge.teewb.request.Item;
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

import java.util.*;

/**
 * Created by chandrasekarramaswamy on 20/05/18.
 */
@Service
public class AuditService {

    private RestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    public SearchResponse findDocumentsById(Exchange exchange) {
        SearchRequest searchRequest = new SearchRequest();
        SearchRequest.Selector selector = new SearchRequest.Selector();
        Map<String, Object> fields = new HashMap<>();
        Integer tracking_id = Integer.parseInt(exchange.getIn().getHeader("tracking_id").toString());
        logger.debug("Fetching documents for tracking ID: "+ tracking_id);
        fields.put("trxIds", Arrays.asList(tracking_id));
        selector.setFields(fields);
        searchRequest.setSelector(selector);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(searchRequest.toJSON(), headers);

        String response = restTemplate.postForObject(auditConfiguration.getUrl() + "/_find", entity, String.class);
        //return SearchResponse.fromJSON(response);
        return null;
    }

    public SearchResponse findAllInvoices() {
        SearchRequest searchRequest = new SearchRequest();
        SearchRequest.Selector selector = new SearchRequest.Selector();
        Map<String, Object> fields = new HashMap<>();
        fields.put("type", "transaction");
        selector.setFields(fields);
        searchRequest.setSelector(selector);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(searchRequest.toJSON(), headers);

        String response = restTemplate.postForObject(auditConfiguration.getUrl() + "/_find", entity, String.class);
        return null;
    }

    public Invoice findInvoice(Integer trxId) {
        QueryConfiguration.Query query = queryConfiguration.findByName("findInvoice");
        TableMetadataConfiguration.Table invoiceTable = tableMetadataConfiguration.findByName("invoiceTable");
        TableMetadataConfiguration.Table itemTable = tableMetadataConfiguration.findByName("itemTable");
        List<String> invoiceColumns = invoiceTable.getColumns();
        List<String> itemColumns = itemTable.getColumns();
        String invoiceIdentifierColumn = invoiceTable.getIdentifierColumn();

        List<Map<String, Object>> received = jdbcTemplate.queryForList(query.getQuery(), trxId);
        received = convertToLowerCase(received);

        Map<String, Object> invoicesMap = received.get(0);
        invoicesMap.keySet().retainAll(invoiceColumns);
        Invoice invoice = new Invoice(invoicesMap);

        for (Map<String, Object> dbRow : received) {
            Map<String, Object> itemRow = new HashMap<>(dbRow);
            itemRow.keySet().retainAll(itemColumns);
            invoice.addItem(new Item(itemRow));
        }

        logger.debug("Invoice found for trxId: " + trxId + " is: " + invoice);
        return invoice;
    }

    public TransactionAudit findTransactionAudit(String trxId) {

        // getting the invoice table...
        TableMetadataConfiguration.Table invoiceTable = tableMetadataConfiguration.findByName("invoiceTable");


        SearchRequest searchRequest = new SearchRequest();
        SearchRequest.Selector selector = new SearchRequest.Selector();

        Map<String, Object> fields = new HashMap<>();

        fields.put("type", "transaction");
        fields.put("trxId", trxId);

        selector.setFields(fields);
        searchRequest.setSelector(selector);

        // creating the HttpHeaders for hitting the api of couch db...
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


        HttpEntity<String> entity = new HttpEntity<String>(searchRequest.toJSON(), headers);

        // finding the trx id from the couchdb...
        String response = restTemplate.postForObject(auditConfiguration.getUrl() + "/_find", entity, String.class);
        logger.debug("Find transaction response: " + response);
        SearchResponse searchResponse = SearchResponse.fromJSON(response);
        logger.debug("SearchResposne: " + searchResponse);
        List<TransactionAudit> transactionAuditList = searchResponse.extractResponse(TransactionAudit.class);

        System.out.println("transactionAuditList ::"+transactionAuditList);
        return transactionAuditList.isEmpty() ? null : transactionAuditList.get(0);
    }

    private List<Map<String, Object>> convertToLowerCase(List<Map<String, Object>> input) {
        List<Map<String, Object>> newList = new ArrayList<>(input.size());
        for (Map<String, Object> row : input) {
            /*Map<String, Object> newMap = row.keySet().stream()
                                            .collect(Collectors.toMap(key -> key.toLowerCase(), key -> row.get(key)));
            newList.add(newMap);*/
            Map<String, Object> newMap = new HashMap<>();
            Iterator<Map.Entry<String, Object>> entryIterator =  row.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, Object> entry = entryIterator.next();
                newMap.put(entry.getKey().toLowerCase(), entry.getValue());
                newList.add(newMap);
            }
        }
        return newList;
    }

}
