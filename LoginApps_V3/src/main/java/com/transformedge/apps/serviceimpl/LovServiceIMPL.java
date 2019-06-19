package com.transformedge.apps.serviceimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.transformedge.apps.entity.LovsInfo;
import com.transformedge.apps.repository.LovDetailsRepository;
import com.transformedge.apps.service.LovService;

@Service
public class LovServiceIMPL implements LovService{

	@Autowired
	private LovDetailsRepository lovDetailsRepository;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public LovsInfo saveLovData(final LovsInfo lovData) {
		logger.info("INSIDE LovServiceIMPL START METHOD saveLovData");
		return lovDetailsRepository.save(lovData);
		
	}

}
