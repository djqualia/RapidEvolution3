package com.mixshare.rapid_evolution.workflow.maintenance;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.CommonIndex;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.io.CustomLocalProfileIO;
import com.mixshare.rapid_evolution.data.profile.io.FileLimitingProfileIO;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class ProfileIOMigrator extends CommonTask {

	static private Logger log = Logger.getLogger(ProfileIOMigrator.class);
    static private final long serialVersionUID = 0L;

	@Override
	public void execute() {
		try {
			log.info("execute(): starting!");

			CustomLocalProfileIO oldIO = new CustomLocalProfileIO();
			FileLimitingProfileIO newIO = new FileLimitingProfileIO();

//			for (CommonIndex index : Database.getAllIndexes()) {
//				for (int id : index.getIds()) {
//					Profile profile = oldIO.getProfile(index.getIdentifierFromUniqueId(id), id);
//					if (profile != null) {
//						newIO.saveProfile(profile);
//
//						if (profile instanceof SearchProfile) {
//							for (byte dataSource : ((SearchProfile) profile).getSearchRecord().getMinedProfileSources()) {
//								MinedProfile minedProfile = oldIO.getMinedProfile(index.getDataType(), dataSource, id);
//								if (minedProfile != null) {
//									newIO.saveMinedProfile(minedProfile, id);
//								}
//							}
//						}
//					}
//				}
//			}
//			log.info("execute(): done writing");

			log.info("execute(): verifying");
			for (CommonIndex index : Database.getAllIndexes()) {
				for (int id : index.getIds()) {
					Profile oldProfile = newIO.getProfile(index.getIdentifierFromUniqueId(id), id);
					Profile newProfile = oldIO.getProfile(index.getIdentifierFromUniqueId(id), id);
					if (oldProfile != null) {
						if (newProfile == null) {
							log.error("execute(): ERROR reading oldProfile=" + oldProfile);
						} else {
							if (!oldProfile.getIdentifier().equals(newProfile.getIdentifier())
									|| (oldProfile.getUniqueId() != newProfile.getUniqueId())) {
								log.error("execute(): DIFFERENT profile read, expected=" + oldProfile + ", but got=" + newProfile);
							}
						}

						if (oldProfile instanceof SearchProfile) {
							for (byte dataSource : ((SearchProfile) oldProfile).getSearchRecord().getMinedProfileSources()) {
								MinedProfile oldMinedProfile = oldIO.getMinedProfile(index.getDataType(), dataSource, id);
								if (oldMinedProfile != null) {
									MinedProfile newMinedProfile = newIO.getMinedProfile(index.getDataType(), dataSource, id);
									if (newMinedProfile == null) {
										log.error("execute(): ERROR reading oldMinedProfile=" + oldMinedProfile);
									} else {
										if (!oldMinedProfile.getHeader().equals(newMinedProfile.getHeader())) {
											log.error("execute(): DIFFERENT minedProfile read, expected=" + oldMinedProfile + ", but got=" + newMinedProfile);
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}

	@Override
	public boolean isIndefiniteTask() { return true; }

	@Override
	public String toString() {
		return "Migrating profiles...";
	}

}
