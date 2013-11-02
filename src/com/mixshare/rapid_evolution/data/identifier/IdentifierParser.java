package com.mixshare.rapid_evolution.data.identifier;

import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;

public class IdentifierParser {

	static public Identifier getIdentifier(String uniqueId) {
		if ((uniqueId == null) || (uniqueId.equals("")))
			return null;
		if (uniqueId.startsWith(ArtistIdentifier.typeDescription))
			return ArtistIdentifier.parseIdentifier(uniqueId);
		if (uniqueId.startsWith(LabelIdentifier.typeDescription))
			return LabelIdentifier.parseIdentifier(uniqueId);
		if (uniqueId.startsWith(ReleaseIdentifier.typeDescription))
			return ReleaseIdentifier.parseIdentifier(uniqueId);
		if (uniqueId.startsWith(SongIdentifier.typeDescription))
			return SongIdentifier.parseIdentifier(uniqueId);
		if (uniqueId.startsWith(PlaylistIdentifier.typeDescription))
			return PlaylistIdentifier.parseIdentifier(uniqueId);
		if (uniqueId.startsWith(TagIdentifier.typeDescription))
			return TagIdentifier.parseIdentifier(uniqueId);
		if (uniqueId.startsWith(StyleIdentifier.typeDescription))
			return StyleIdentifier.parseIdentifier(uniqueId);
		if (uniqueId.startsWith(MixoutIdentifier.typeDescription))
			return MixoutIdentifier.parseIdentifier(uniqueId);
		return null;
	}

}
