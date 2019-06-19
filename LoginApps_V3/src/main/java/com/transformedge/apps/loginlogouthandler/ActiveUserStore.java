package com.transformedge.apps.loginlogouthandler;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ActiveUserStore {
	 
    public List<String> users;
    public ActiveUserStore() {
        users = new ArrayList<String>();
    }
}