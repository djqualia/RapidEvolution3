package test.com.mixshare.rapid_evolution.data.util.io;

import java.util.ArrayList;
import java.util.List;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.util.io.Serializer;

public class SerializerTest extends RE3TestCase {

	public void testSerializationToBytes() {
		ArtistIdentifier artistId = new ArtistIdentifier("aphex twin");
		byte[] array = Serializer.encodeCompressedBytes(artistId);
		ArtistIdentifier artistId2 = (ArtistIdentifier)Serializer.decodeCompressedBytes(array);
		if (!artistId.equals(artistId2))
			fail("serialization to bytes and back failed");
	}
	
	public void testCompressedDataListReadWrite() {
		List<Object> objects = new ArrayList<Object>();
		objects.add(4);
		objects.add(new MinedProfileHeader((byte) 2, (byte) 2));
		objects.add(new ArtistIdentifier("blah"));
		objects.add(new ArtistProfile());
		//objects.add(new LastfmArtistProfile("Daft Punk"));
		
		assertTrue(Serializer.saveCompressedDataList(objects, "temp/compressedDataList.jso"));
		
		List<Object> objectsRead = Serializer.readCompressedDataList("temp/compressedDataList.jso");
		assertEquals(objects.size(), objectsRead.size());
		assertEquals(4, objectsRead.get(0));
		assertEquals(objects.get(1), objectsRead.get(1));
		assertEquals(objects.get(2), objectsRead.get(2));
		assertTrue(objects.get(3) instanceof ArtistProfile);
		//assertTrue(objects.get(4) instanceof LastfmArtistProfile);
	}
}
