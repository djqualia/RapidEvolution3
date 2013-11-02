package test.com.mixshare.rapid_evolution.data.util.table;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.data.util.table.UniqueIdTable;

public class UniqueIdTableTest extends RE3TestCase {

	public void testSerialization() {
		try {		
			UniqueIdTable uniqueIdTable = new UniqueIdTable(); // maps unique Ids to identifiers and vice versa
			
			StyleIdentifier styleId = new StyleIdentifier("test style");
		    int styleUniqueId = uniqueIdTable.getUniqueIdFromIdentifier(styleId);
			StyleIdentifier styleId2 = new StyleIdentifier("test style2");
		    int styleUniqueId2 = uniqueIdTable.getUniqueIdFromIdentifier(styleId2);
		    
			XMLSerializer.saveData(uniqueIdTable, "data/junit/temp/unique_id_table.xml");
			uniqueIdTable = (UniqueIdTable)XMLSerializer.readData("data/junit/temp/unique_id_table.xml");
			
			if (uniqueIdTable.getUniqueIdFromIdentifier(styleId) != styleUniqueId)
				fail("uniqueid table incorrect");
			if (uniqueIdTable.getUniqueIdFromIdentifier(styleId2) != styleUniqueId2)
				fail("uniqueid table incorrect");
			if (!uniqueIdTable.getIdentifierFromUniqueId(styleUniqueId).equals(styleId))
				fail("uniqueid table incorrect");
			if (!uniqueIdTable.getIdentifierFromUniqueId(styleUniqueId2).equals(styleId2))
				fail("uniqueid table incorrect");

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}	
	
}
