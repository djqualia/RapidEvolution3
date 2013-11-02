package com.mixshare.rapid_evolution.data.profile.io;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.Record;

public interface ProfileIOInterface {

	public boolean saveProfile(Profile profile);
	public Profile getProfile(Identifier id, int profileId);
	public boolean deleteProfile(Record record);
	public boolean deleteProfileFile(int uniqueId, byte dataType, String typeDescription);

	public boolean saveMinedProfile(MinedProfile profile, int uniqueId);
	public MinedProfile getMinedProfile(byte dataType, byte dataSource, int uniqueId);
	public boolean deleteMinedProfile(byte dataType, byte dataSource, int uniqueId);

	public Vector<Profile> readProfilesFromFile(String filename, byte type);

	public void savePendingProfiles();
}
