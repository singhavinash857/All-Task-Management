package com.transformedge.apps.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Date;
import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class DateTimeUtility {

	private DateTime now = new DateTime(); 
	private static LocalDateTime localDateTime = new LocalDateTime();

	public java.sql.Date getTodayDate() {
		return jodatToSQLDate(localDateTime);
	}

	private Date jodatToSQLDate(LocalDateTime localDateTime2) {
		return new Date(localDateTime.toDateTime().getMillis());
	}

	public String getCurrentTime() {
		 LocalDateTime ldt = new LocalDateTime();
	     DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");
	     return fmt.print(ldt);
	}


	public static Timestamp jodaToSQLTimestamp(LocalDateTime localDateTime) {
		return new Timestamp(localDateTime.toDateTime().getMillis());
	}

	public Timestamp jodaToSQLTimestamp() {
		return new Timestamp(localDateTime.toDateTime().getMillis());
	}
	
	public static LocalDateTime sqlTimestampToJodaLocalDateTime(Timestamp timestamp) {
		return LocalDateTime.fromDateFields(timestamp);
	}

	public static class Test{
		public static void main(String[] args) {
			 LocalDateTime ldt = new LocalDateTime();
			 
		     DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");
		     String str = fmt.print(ldt);
		     System.out.println("str ::"+str);
		     
		     InetAddress localhost = null;
			try {
				localhost = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} 
		        System.out.println("System IP Address : " + 
		                      (localhost.getHostAddress()).trim());
		}
	}
}
