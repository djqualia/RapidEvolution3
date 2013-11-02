package com.mixshare.rapid_evolution.ui.updaters.model;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.CommonModelManager;

public class ModelResetter extends Thread {

    private static Logger log = Logger.getLogger(ModelResetter.class);
	
	private CommonModelManager modelManager;

	public ModelResetter(CommonModelManager modelManager) {
		this.modelManager = modelManager;
	}
	
	public void run() {
		try {
			modelManager.resetModel();
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}	
	
}
