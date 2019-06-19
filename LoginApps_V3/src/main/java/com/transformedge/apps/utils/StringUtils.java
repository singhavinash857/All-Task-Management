package com.transformedge.apps.utils;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class StringUtils {

	public String removeLastOccuranceString(String str){
		if (null != str && str.length() > 0 ){
			return str.substring(0, str.lastIndexOf("/"));
		}
		return str;
	}

	public String generatePasswordResetToken(Long id) {
		String token = Jwts.builder()
		.setSubject(Long.toString(id))
		.setExpiration(new Date(System.currentTimeMillis()+SecurityConstants.PASSWORD_RESTE_EXPIRATION_TIME))
		.signWith(SignatureAlgorithm.HS512, SecurityConstants.getTokenSecret())
		.compact();
		return token;
	}

	public boolean isTokenExpired(String token) {
		Claims claims = Jwts.parser()
				.setSigningKey(SecurityConstants.getTokenSecret())
				.parseClaimsJws(token).getBody();
		Date tokenExpirationDate = claims.getExpiration();
		Date todayDate = new Date();
		return tokenExpirationDate.before(todayDate);
	}
}
