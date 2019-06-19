package com.transformedge.teewb;

import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by chandrasekarramaswamy on 24/05/18.
 */
@Component
public class UUIDGenerator {

    public String getUuid() {
        //return UUID.randomUUID().toString();
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
        String strDate = dateFormat.format(date);
        return strDate;
    }

    public static void main(String args[]) {
        System.out.println(new UUIDGenerator().getUuid());
    }

}
