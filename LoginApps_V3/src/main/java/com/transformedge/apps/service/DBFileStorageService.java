package com.transformedge.apps.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.transformedge.apps.appconfiguration.Translator;
import com.transformedge.apps.entity.FileDetailsDb;
import com.transformedge.apps.exceptions.FileStorageException;
import com.transformedge.apps.exceptions.MyFileNotFoundException;
import com.transformedge.apps.repository.FileRepository;

@Service
public class DBFileStorageService {

	@Autowired
    private FileRepository dbFileRepository;
	
	public FileDetailsDb storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(fileName);
        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
            	String fileNameInvalid = Translator.toLocale("file.invalid.name");
                throw new FileStorageException(fileNameInvalid + " file name :" +fileName);
            }if(file.getSize() > 512000){
            	String couldNotStoreFile = Translator.toLocale("file.fileSize.coludNotStore");
                throw new FileStorageException(couldNotStoreFile+fileName);
            }if(fileExtension.equalsIgnoreCase("JPEG") || fileExtension.equalsIgnoreCase("PNG") || fileExtension.equalsIgnoreCase("JPG")){
            	 FileDetailsDb dbFile = new FileDetailsDb(fileName, file.getContentType(), file.getBytes());
                 return dbFileRepository.save(dbFile);
            }else{
            	String couldNotStoreFile = Translator.toLocale("file.fileExtension.coludNotStore");
                throw new FileStorageException(couldNotStoreFile);
            }
        } catch (IOException ex) {
        	String couldNotStoreFile = Translator.toLocale("file.invalid.coludNotStore");
            throw new FileStorageException(couldNotStoreFile+fileName);
        }
    }

    private String getFileExtension(String fileName) {
    	//checkNotNull(fullName);
    	if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			return fileName.substring(fileName.lastIndexOf(".")+1);
		else return "";
	}

	public FileDetailsDb getFile(String fileId) {
    	String fileNotFound = Translator.toLocale("file.invalid.notFound");
        return dbFileRepository.findById(fileId)
                .orElseThrow(() -> new MyFileNotFoundException(fileNotFound+ fileId));
    }

}
