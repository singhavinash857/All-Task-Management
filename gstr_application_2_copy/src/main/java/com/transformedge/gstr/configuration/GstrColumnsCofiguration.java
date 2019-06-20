package com.transformedge.gstr.configuration;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "gstr-table-columns")
@Configuration
public class GstrColumnsCofiguration {
	
    private List<Gstr> gstr;

    public Gstr findByName(String name) {
        return gstr.stream().filter(a -> a.getName().equals(name))
                    .findFirst().get();
    }
    
    @Data
    public static class Gstr {
        private String name;
        private Map<String, String> columns;
        private Map<String,String> components;
        
		public String getColumnsValue(String key) {
			return columns.get(key);
		}
		
		public String getComponent(String key){
			return components.get(key);
		}
    }
}
