package com.transformedge.apps.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.transformedge.apps.model.UserLoginInfoResponse;

import lombok.Data;


@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property="employeeId")
@Data
@Entity
@Table(name="employee")
public class Employee implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="employee_id")
	private long employeeId;

	@Column(name="employee_firstname")
	private String employeeFirstName;

	@Column(name="employee_lasttname")
	private String employeeLastName;
	
	@Column(name="employee_gender")
	private String employeeGender;
	
	@Column(name="employee_mail_id")
	private String employeeMailId;

	@Column(name="employee_code")
	private String employeeCode;

	@Column(name="employee_phone")
	private String employeePhone;

	@Column(name="employee_designation")
	private String employeeDesignation;

	@Column(name="employee_status")
	private String employeeStatus;
	
	@Transient
	private Set<Role> employeeRoles;
	
	@Transient
	private UserLoginInfoResponse userLoginDetailsInfo;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	//@JsonBackReference
	private List<Task> tasks = new ArrayList<>();

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "file_id", referencedColumnName = "id")
	private FileDetailsDb employeeFileDetailsDb = new FileDetailsDb();
}
