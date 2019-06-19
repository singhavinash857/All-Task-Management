package com.transformedge.teewb;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformedge.teewb.request.OutputResponse;

import java.io.File;
import java.io.FileReader;

public class Main {

    public static void main(String args[]) throws Exception {
       /* //new JsonParser().parse(new FileReader(new File("C:\\Projects\\Audit.json")));
        String json = "{\"OutputResponse\": {\"status\": \"1\",\"data\": \"sZpVfPorPIyr0mFNXriA3bivjPVYkvJvT46LHGr/UICqIqgx8tvOgHyfxQb8/G592+jaP74eCEFATyAwlhCP+oICE5Gcvwgzo0DDMi2ESzHU3O5mBE6tw9DBFDr0vNGgAG9n7sjdQCghnL/YBR22Ig==\",\"ewayBillNo\": \"341001006715\",\"ewayBillDate\": \"25/04/2018 12:03:00 AM\",\"validUpto\": \"26/04/2018 11:59:00 PM\"}}";
        //System.out.println(new JsonParser().parse(json).toString());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        OutputResponse outputResponse = mapper.readValue(json, OutputResponse.class);
        System.out.println("from string: " + outputResponse);

        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        System.out.println("from object: " + mapper.writeValueAsString(outputResponse));*/

       //ObjectMapper mapper = new ObjectMapper();
       // mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
       //OutputResponse response = mapper.readValue(new FileReader(new File("C:\\Projects\\InvalidResponse.json")), OutputResponse.class);
       //System.out.println(response);
        System.out.println("12345ghh.1234".matches("^([0-9]+\\.?[0-9]*|[0-9]*\\.[0-9]+)$"));
    }

}

