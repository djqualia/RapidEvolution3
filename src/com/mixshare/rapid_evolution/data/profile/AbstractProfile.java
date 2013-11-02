package com.mixshare.rapid_evolution.data.profile;

import java.util.Map;

import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class AbstractProfile implements Profile {

	@Override
	abstract public Map<Record, Object> mergeWith(Profile profile);

	@Override
	abstract public void update(SubmittedProfile submittedProfile, boolean overwrite);

	abstract protected void updateIdentifier(Identifier newId, Identifier oldId) throws AlreadyExistsException;

	@Override
	abstract public boolean save();

    @Override
	abstract public void write(LineWriter textWriter);

    @Override
	abstract public void setRating(Rating rating, byte source);

}
