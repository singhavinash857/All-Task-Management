package com.transformedge.gstr.request;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CsvOutputData {
    private List<Map<String, Object>> data;
    
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

}