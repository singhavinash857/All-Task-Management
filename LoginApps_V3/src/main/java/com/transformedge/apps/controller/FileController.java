package com.transformedge.apps.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.transformedge.apps.appconfiguration.CsvConfiguration;
import com.transformedge.apps.appconfiguration.Translator;
import com.transformedge.apps.entity.Employee;
import com.transformedge.apps.entity.FileDetailsDb;
import com.transformedge.apps.exceptions.ErrorFormInfo;
import com.transformedge.apps.service.DBFileStorageService;
import com.transformedge.apps.service.EmployeeService;
import com.transformedge.apps.utils.StringUtils;

@RestController
@CrossOrigin
@RequestMapping(value="${spring.data.rest.base-path}/file_controller")
public class FileController {
	private final String STATIC_DOWNLOAD_FILE_URL = "/file_controller"+"/downloadfile/";

	private static final Logger logger = LoggerFactory.getLogger(FileController.class);

	@Autowired
	CsvConfiguration yamlCsvConfiguration;

	@Autowired
	private DBFileStorageService dBFileStorageService;

	@Autowired
	StringUtils stringUtils;

	@Autowired
	EmployeeService employeeService;

	@PostMapping("/upload_photo_by_id")
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
			                            @RequestParam("employeeId") long employeeId,
			                            HttpServletRequest request) {
		logger.info("INSIDE FileController uploadFile CALLED !!");
		Employee employee = employeeService.getEmployeeById(employeeId);
		ErrorFormInfo errorInfo = null;
		if(employee != null){
			System.out.println("file.getSize() ::"+file.getSize());
			FileDetailsDb dbStoredFile = dBFileStorageService.storeFile(file);
			logger.info("dbStoredFile ::"+dbStoredFile);
			if(dbStoredFile != null){
				String currentPath = yamlCsvConfiguration.getBaseURl()+STATIC_DOWNLOAD_FILE_URL;
				String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
						.path(currentPath)
						.path(dbStoredFile.getId())
						.toUriString();
				dbStoredFile.setFileDownloadUri(fileDownloadUri);
				dbStoredFile.setSize(file.getSize());
				employee.setEmployeeFileDetailsDb(dbStoredFile);
				if(employeeService.saveEmployee(employee) != null){
					String successMsg = Translator.toLocale("file.uploaded.successfully");
					errorInfo = new ErrorFormInfo(HttpStatus.OK, true, request.getRequestURI(), successMsg, null);
					return new ResponseEntity<>(errorInfo, HttpStatus.OK);
				}
			} 
		}
		return new ResponseEntity<>(errorInfo, HttpStatus.RESET_CONTENT);
	}

	@GetMapping("/downloadfile/{fileId}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
		logger.info("INSIDE FileController downloadFile CALLED !!");
		FileDetailsDb dbFile = dBFileStorageService.getFile(fileId);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(dbFile.getFileType()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbFile.getFileName() + "\"")
				.body(new ByteArrayResource(dbFile.getData()));
	}


}
