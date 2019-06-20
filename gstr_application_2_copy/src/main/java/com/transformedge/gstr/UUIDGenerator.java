package com.transformedge.gstr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class UUIDGenerator {

    public String getUuid() {
        //return UUID.randomUUID().toString();
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
        String strDate = dateFormat.format(date);
        return strDate;
    }

    /*public static void main(String args[]) {
        System.out.println(new UUIDGenerator().getUuid());
    }*/

}
