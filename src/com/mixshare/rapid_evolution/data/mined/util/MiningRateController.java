package com.mixshare.rapid_evolution.data.mined.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.lastfm.song.LastfmSongProfile;

public class MiningRateController {

    static private Logger log = Logger.getLogger(MiningRateController.class);

    static public long INTERVAL_LENGTH_MILLIS = 1000 * 60 * 60 * 24; // 1 day 
    
	////////////
	// FIELDS //
	////////////
	
    private byte dataSource;
	private float maxQueriesPerSecond;
	private long minQueryInterval; // milliseconds	
	private long timeOfLastQuery;	
	private int maxQueriesPerInterval;
	
	private int numQueriesThisInterval;
	private long timeOfFirstQuery;
	private long timeOfNextAllowedQuery;
	
	transient private Semaphore startQueryLock;
	
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(MiningRateController.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("startQueryLock")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public MiningRateController(byte dataSource) {
		this.dataSource = dataSource;
		this.maxQueriesPerSecond = RE3Properties.getFloat(DataConstantsHelper.getDataSourceDescription(dataSource).toLowerCase() + "_max_queries_per_second");
		this.maxQueriesPerInterval = RE3Properties.getInt(DataConstantsHelper.getDataSourceDescription(dataSource).toLowerCase() + "_max_queries_per_day");
		if (maxQueriesPerSecond > 0)
			minQueryInterval = Math.round(1000.0f / maxQueriesPerSecond);
		else
			minQueryInterval = 0;
		// persistent data
		String numQueriesTodayKey = DataConstantsHelper.getDataSourceDescription(dataSource).toLowerCase() + "_num_queries_today";
		String timeOfFirstQueryKey = DataConstantsHelper.getDataSourceDescription(dataSource).toLowerCase() + "_time_of_first_query";
		String timeOfNextAllowedQueryKey = DataConstantsHelper.getDataSourceDescription(dataSource).toLowerCase() + "_time_of_next_allowed_query";
		if (Database.getProperty(numQueriesTodayKey) != null)
			numQueriesThisInterval = (Integer)Database.getProperty(numQueriesTodayKey);
		if (Database.getProperty(timeOfFirstQueryKey) != null)
			timeOfFirstQuery = (Long)Database.getProperty(timeOfFirstQueryKey);
		if (Database.getProperty(timeOfNextAllowedQueryKey) != null)
			timeOfNextAllowedQuery = (Long)Database.getProperty(timeOfNextAllowedQueryKey);
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void setNumRequestsThisInterval(int numQueriesThisInterval) { this.numQueriesThisInterval = numQueriesThisInterval; }
	
	public boolean canMakeQuery() {
		if (maxQueriesPerInterval > 0) 
			return ((numQueriesThisInterval < maxQueriesPerInterval) && (System.currentTimeMillis() >= timeOfNextAllowedQuery));
		return true;
	}
	
	public long getTimeUntilNextQuery() {
		if (canMakeQuery()) {
			long currentTime = System.currentTimeMillis();
			long timeSinceLastQuery = currentTime - timeOfLastQuery;
			long timeToWait = minQueryInterval - timeSinceLastQuery;
			if (timeToWait > 0)
				return timeToWait;
			return 0;
		} else {
			long timeToWait = timeOfNextAllowedQuery - System.currentTimeMillis();
			if (timeToWait > 0)
				return timeToWait;
			return 0;
		}
	}
		
	public void startQuery() throws MiningLimitReachedException {
		if (System.currentTimeMillis() - timeOfFirstQuery > INTERVAL_LENGTH_MILLIS)
			numQueriesThisInterval = 0;
		if (System.currentTimeMillis() < timeOfNextAllowedQuery)
			throw new MiningLimitReachedException();
		try {
			getStartQueryLock().acquire();
			long timeToWait = getTimeUntilNextQuery();
			if (timeToWait > 0) {
				if (log.isTraceEnabled())
					log.trace("startQuery(): delaying ms=" + timeToWait);
				Thread.sleep(timeToWait);
			}
			timeOfLastQuery = System.currentTimeMillis();
			++numQueriesThisInterval;
			if (timeOfFirstQuery == 0)
				timeOfFirstQuery = System.currentTimeMillis();			
			if ((maxQueriesPerInterval > 0) && (numQueriesThisInterval >= maxQueriesPerInterval)) {
				timeOfNextAllowedQuery = System.currentTimeMillis() + INTERVAL_LENGTH_MILLIS;
				numQueriesThisInterval = 0;
				timeOfFirstQuery = 0;
			}
			// persistent data
			String numQueriesTodayKey = DataConstantsHelper.getDataSourceDescription(dataSource).toLowerCase() + "_num_queries_today";
			String timeOfFirstQueryKey = DataConstantsHelper.getDataSourceDescription(dataSource).toLowerCase() + "_time_of_first_query";
			String timeOfNextAllowedQueryKey = DataConstantsHelper.getDataSourceDescription(dataSource).toLowerCase() + "_time_of_next_allowed_query";
			Database.setProperty(numQueriesTodayKey, numQueriesThisInterval);
			Database.setProperty(timeOfFirstQueryKey, timeOfFirstQuery);
			Database.setProperty(timeOfNextAllowedQueryKey, timeOfNextAllowedQuery);			
		} catch (Exception e) {
			log.error("startQuery(): error", e);
		} finally {
			getStartQueryLock().release();
		}
	}
	
	protected Semaphore getStartQueryLock() {
		if (startQueryLock == null)
			startQueryLock = new Semaphore(1);
		return startQueryLock;
	}
	
}
