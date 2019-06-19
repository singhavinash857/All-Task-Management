package com.transformedge.apps.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    @Column(name="username",nullable=false,unique=true)
    private String username;
    
    private String password;
    private String passwordConfirm;
    private int enabled = 1;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Role> roles = new ArrayList<>();

}
