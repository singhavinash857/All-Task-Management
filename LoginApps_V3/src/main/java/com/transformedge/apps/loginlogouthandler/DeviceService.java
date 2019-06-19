package com.transformedge.apps.loginlogouthandler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.transformedge.apps.entity.DeviceMetadata;
import com.transformedge.apps.entity.User;
import com.transformedge.apps.repository.DeviceMetadataRepository;
import com.transformedge.apps.service.UserService;
import com.transformedge.apps.utils.DateTimeUtility;
import com.transformedge.apps.utils.IpAddressUtils;

import ua_parser.Client;
import ua_parser.Parser;

@Component
public class DeviceService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final String UNKNOWN = "UNKNOWN";

	@Autowired
	IpAddressUtils ipAddressUtils;

	@Autowired
	DateTimeUtility dateTimeUtils;

	@Autowired
	UserService userService;

	 private DeviceMetadataRepository deviceMetadataRepository;
	    private DatabaseReader databaseReader;
	    private Parser parser;

	    public DeviceService(DeviceMetadataRepository deviceMetadataRepository,
	                         DatabaseReader databaseReader,
	                         Parser parser) {
	        this.deviceMetadataRepository = deviceMetadataRepository;
	        this.databaseReader = databaseReader;
	        this.parser = parser;
	    }

	public void verifyDevice(String userName, HttpServletRequest request) throws IOException, GeoIp2Exception {
		String ip = ipAddressUtils.extractCleintIp(request);
		String location = getIpLocation(ip);
		//String location = null;
		String deviceDetails = getDeviceDetails(request.getHeader("user-agent"));
		User user = userService.findByUsername(userName);
		if(user != null){
			DeviceMetadata existingDevice = findExistingDevice(user.getId(), deviceDetails, location);
			if (Objects.isNull(existingDevice)) {
				//unknownDeviceNotification(deviceDetails, location, ip, user.get, request.getLocale());

				DeviceMetadata deviceMetadata = new DeviceMetadata();
				deviceMetadata.setUserId(user.getId());
				deviceMetadata.setLocation(location);
				deviceMetadata.setDeviceDetails(deviceDetails);
				deviceMetadata.setLastLoggedIn(dateTimeUtils.getTodayDate());
				deviceMetadataRepository.save(deviceMetadata);
			} else {
				existingDevice.setLastLoggedIn(dateTimeUtils.getTodayDate());
				deviceMetadataRepository.save(existingDevice);
			}
		}
	}

	private DeviceMetadata findExistingDevice(Long userId, String deviceDetails, String location) {

		List<DeviceMetadata> knownDevices = deviceMetadataRepository.findByUserId(userId);

		for (DeviceMetadata existingDevice : knownDevices) {
			if (existingDevice.getDeviceDetails().equals(deviceDetails) &&
					existingDevice.getLocation().equals(location)) {
				return existingDevice;
			}
		}

		return null;
	}

	private String getDeviceDetails(String userAgent) {
		String deviceDetails = UNKNOWN;

		Client client = parser.parse(userAgent);
		if (Objects.nonNull(client)) {
			deviceDetails = client.userAgent.family + " " + client.userAgent.major + "." + client.userAgent.minor +
					" - " + client.os.family + " " + client.os.major + "." + client.os.minor;
		}

		return deviceDetails;	
	}

	private String getIpLocation(String ip) throws IOException, GeoIp2Exception {
		String location = UNKNOWN;
		InetAddress ipAddress = InetAddress.getByName("192.168.23.2");
		CityResponse cityResponse = databaseReader.city(ipAddress);
		if (Objects.nonNull(cityResponse) &&
				Objects.nonNull(cityResponse.getCity()) &&
				!Strings.isNullOrEmpty(cityResponse.getCity().getName())) {
			location = cityResponse.getCity().getName();
		}
		return location;
	}

}
