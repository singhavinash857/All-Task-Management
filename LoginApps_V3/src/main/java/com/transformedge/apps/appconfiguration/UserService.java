package com.transformedge.apps.appconfiguration;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import com.maxmind.geoip2.DatabaseReader;
import com.transformedge.apps.loginlogouthandler.ActiveUserStore;
import com.transformedge.apps.loginlogouthandler.LoginLogoutDateTimeComponent;

import ua_parser.Parser;
@Configuration
public class UserService {

	@Bean
	public ActiveUserStore activeUserStore(){
		return new ActiveUserStore();
	}

	@Bean
	public LoginLogoutDateTimeComponent  loginLogoutDateTimeComponent(){
		return new LoginLogoutDateTimeComponent();
	}

	@Bean
	public Parser uaParser() throws IOException {
		return new Parser();
	}

	@Bean
	@Qualifier()
	public DatabaseReader databaseReader() throws IOException {
		File database = ResourceUtils.getFile("classpath:maxmind/GeoLite2-City.mmdb");
		return new DatabaseReader.Builder(database).build();
	}
}
