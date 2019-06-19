package com.transformedge.apps.model;

import lombok.Data;

@Data
public class PasswordResetModel {
		private String token;
		private String password;
}
