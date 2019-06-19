package com.transformedge.teewb.request;

import java.util.*;

/**
 * Created by chandrasekarramaswamy on 17/06/18.
 */
public class CsvOutputData {
    private List<Map<String, Object>> data;

    public CsvOutputData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public void addDataToRows(String keyColumnName, String id, String key, Object value) {
        for (Map<String, Object> row : data) {
            if (row.get(keyColumnName).toString().equals(id)) {
                row.put(key, value);
            }
        }
    }

    public void addData(Map<String, Object> values)  {
        data.add(values);
    }

    public List<Map<String, Object>> filterColumnsAndGetData(String idColumnName, List<String> outputColumns) {
        List<Map<String, Object>> results = new ArrayList<>();
        List<String> processedInvoice = new ArrayList<>();
        for (Map<String, Object> row : data) {
            Map<String, Object> outputValues = new LinkedHashMap<>();
            if (!processedInvoice.contains(row.get(idColumnName))) {
                for (String columnName : outputColumns) {
                    outputValues.put(columnName, format(String.valueOf(row.get(columnName))));
                }
            } else {
                continue;
            }

            processedInvoice.add(String.valueOf(row.get(idColumnName)));
            results.add(outputValues);
        }
        return results;
    }

    public String format(String str) {
        return str == null || str.equalsIgnoreCase("null") ? "" : str;
    }

    @Override
    public String toString() {
        return data.toString();
    }

    /*private Map<String, List<CsvRow>> rows;

    public CsvOutputData() {
        rows = new LinkedHashMap<>();
    }

    public CsvRow newRow(String id) {
        List<CsvRow> rowList = findRow(id);
        CsvRow row = new CsvRow(id);;
        if (rowList == null) {
            // create a new row list and add it to map
            rowList = new ArrayList<>();
            rowList.add(row);
            rows.put(id, rowList);
        } else {
            rowList.add(row);
        }

        return row;
    }

    public List<CsvRow> findRow(String id) {
        return rows.get(id);
    }

    public void addDataToRows(String id, String key, Object value) {
        List<CsvRow> csvRowList = findRow(id);
        for (CsvRow row : csvRowList) {
            row.addData(key, value);
        }
    }

    public void addInvoiceDataToOutput(String idColumnName, Invoice invoice) {
        String id = String.valueOf(invoice.getFieldValue(idColumnName));
        Map<String, Object> invoiceFields = invoice.getFields();
        Iterator<Map.Entry<String, Object>> iterator = invoiceFields.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (!entry.getKey().equals(idColumnName)) {
                addDataToRows(id, entry.getKey(), entry.getValue());
            }
        }
    }

    public void addItemDataToOutput(String idColumnName, Item item) {
        String id = String.valueOf(item.getFieldValue(idColumnName));
        Map<String, Object> itemFields = item.getFields();
        Iterator<Map.Entry<String, Object>> iterator = itemFields.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (!entry.getKey().equals(idColumnName)) {
                addDataToRows(id, entry.getKey(), entry.getValue());
            }
        }
    }

    public List<Map<String, Object>> asList() {
        List<Map<String, Object>> results = new ArrayList<>();
        Iterator<Map.Entry<String, List<CsvRow>>> iterator = rows.entrySet().iterator();
        while (iterator.hasNext()) {
            List<CsvRow> rowList = iterator.next().getValue();
            for (CsvRow csvRow : rowList) {
                results.add(csvRow.asMap());
            }
        }
        return results;
    }

    @Override
    public String toString() {
        return rows.toString();
    }

    public static class CsvRow {
        CsvData csvData;

        public CsvRow(String id) {
            csvData = new CsvData(id);
        }

        public void addData(String key, Object value) {
            csvData.put(key, value);
        }

         Map<String, Object> asMap() {
            return csvData.getRow();
        }

        @Override
        public String toString() {
            return csvData.toString();
        }
    }

     static class CsvData {
        Map<String, Object> row;

        public CsvData(String id) {
            row = new LinkedHashMap<>();
            row.put("e_trx_id", id);
        }

        public void put(String key, Object value) {
            row.put(key, value);
        }

        public Map<String, Object> getRow() {
            return row;
        }

         @Override
         public String toString() {
             return row.toString();
         }
     }
*/

}
