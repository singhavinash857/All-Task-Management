package com.transformedge.apps.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="password_reset_tokens")
public class PasswordResetTokenEnity implements Serializable{
	
	private static final long serialVersionUID = 1130420740956761854L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	private String token;
	
	@OneToOne()
	@JoinColumn(name="user_id")
	private User userDetails;
}
