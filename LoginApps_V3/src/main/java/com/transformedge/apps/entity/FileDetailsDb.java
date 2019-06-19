package com.transformedge.apps.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "files")
public class FileDetailsDb {
	
	    @Id
	    @GeneratedValue(generator = "uuid")
	    @GenericGenerator(name = "uuid", strategy = "uuid2")
	    private String id;
	    private String fileName;
	    private String fileType;
	    private String fileDownloadUri;
		private long size;
	    
	    @Lob
	    private byte[] data;
	    
	    @OneToOne(mappedBy = "employeeFileDetailsDb")
	    private Employee employee;
	    
	    public FileDetailsDb(String fileName, String fileType, byte[] data) {
	        this.fileName = fileName;
	        this.fileType = fileType;
	        this.data = data;
	    }
}
