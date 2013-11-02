package com.mixshare.rapid_evolution.workflow.mining;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;

abstract public class MiningTask extends CommonTask {

	////////////
	// FIELDS //
	////////////
	
	protected int taskPriority;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public MiningTask() {
		super();		
		this.taskPriority = RE3Properties.getInt(DataConstantsHelper.getDataSourceDescription(getMinedProfileHeader().getDataSource()).toLowerCase() + "_mining_task_priority");
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract public void init(TaskResultListener resultListener, SearchProfile profile);	
	abstract public MinedProfileHeader getMinedProfileHeader();
	abstract public long getMinimumTimeBetweenQueries(); // for the same item
	abstract public CommonMiningAPIWrapper getAPIWrapper();
	
	/////////////
	// GETTERS //	
	/////////////
	
	public int getTaskPriority() { return taskPriority; }

	public boolean isIndefiniteTask() { return true; }
	
}
