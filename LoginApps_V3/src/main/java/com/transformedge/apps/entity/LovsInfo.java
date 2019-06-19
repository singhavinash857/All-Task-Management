package com.transformedge.apps.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="Lov_info")
public class LovsInfo implements Serializable{
	private static final long serialVersionUID = -6083025660077719754L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long lovId;
		
	@Column(name="lov_type", nullable=false, unique=false)
	private String lovType;
    
    @Column(name="lov_value", nullable=false, unique=false)
	private String lovValue;
    
    @Column(name="lov_description", nullable=false, unique=false)
	private String lovDescription;
    
    @Column(name="lov_comments", nullable=false, unique=false)
	private String lovComments;
}
