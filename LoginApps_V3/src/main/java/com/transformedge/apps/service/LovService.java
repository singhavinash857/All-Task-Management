package com.transformedge.apps.service;

import javax.validation.Valid;

import com.transformedge.apps.entity.LovsInfo;

public interface LovService {
	LovsInfo saveLovData(@Valid LovsInfo lovData);
}
