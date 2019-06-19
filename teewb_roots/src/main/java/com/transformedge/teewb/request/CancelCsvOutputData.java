package com.transformedge.teewb.request;

import java.util.*;

/**
 * Created by chandrasekarramaswamy on 17/06/18.
 */
public class CancelCsvOutputData {
    private List<CsvOutputRow> csvOutputRows;

    public CancelCsvOutputData() {
        csvOutputRows = new ArrayList<>();
    }

    public CsvOutputRow newRow() {
        return new CsvOutputRow();
    }

    public void addRow(CsvOutputRow row) {
        csvOutputRows.add(row);
    }

    public List<Map<String, Object>> asList() {
        List<Map<String, Object>> outputRows = new ArrayList<>();
        for (CsvOutputRow row : csvOutputRows) {
            outputRows.add(row.asMap());
        }
        return outputRows;
    }

    @Override
    public String toString() {
        return "CsvOutputData{" +
                "csvOutputRows=" + csvOutputRows +
                '}';
    }

    /*public void addDataToRows(String keyColumnName, String id, String key, Object value) {
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


        /**
         * Created by chandrasekarramaswamy on 15/07/18.
         */
    public static class CsvOutputRow {
        private Map<String, Object> row;

        public CsvOutputRow() {
            row = new LinkedHashMap<>();
        }

        public void addColumn(String key, Object value) {
            row.put(key, value);
        }

        public Map<String, Object> asMap() {
            return row;
        }

        @Override
        public String toString() {
            return "CsvOutputRow{" +
                    "row=" + row +
                    '}';
        }
    }
}
