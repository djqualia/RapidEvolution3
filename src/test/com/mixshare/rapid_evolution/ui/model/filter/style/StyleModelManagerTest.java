package test.com.mixshare.rapid_evolution.ui.model.filter.style;

import java.util.Vector;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.mixshare.rapid_evolution.ui.model.filter.style.StyleModelManager;

public class StyleModelManagerTest extends RE3TestCase implements AllColumns {

	public void testSerialization() {
		try {		
			StyleModelManager modelManager = new StyleModelManager();
			
			modelManager.setNextUserColumnId((byte)254);
			
			Vector<Column> sourceColumns = new Vector<Column>();
			sourceColumns.add(COLUMN_STYLE_NAME.getInstance(true));
			sourceColumns.add(COLUMN_DEGREE.getInstance(true));
			modelManager.setSourceColumns(sourceColumns);
			
			modelManager.setViewColumns(sourceColumns);

			Vector<ColumnOrdering> ordering = new Vector<ColumnOrdering>();
			ordering.add(new ColumnOrdering(COLUMN_STYLE_NAME.getColumnId()));
			modelManager.setSortOrdering(ordering);
						
			XMLSerializer.saveData(modelManager, "data/junit/temp/styleModelManager.xml");
			modelManager = (StyleModelManager)XMLSerializer.readData("data/junit/temp/styleModelManager.xml");
			
			if (modelManager.getNextUserColumnId() != (byte)254)
				fail();
			
			if (modelManager.getSourceColumns().get(0).getColumnId() != COLUMN_STYLE_NAME.getColumnId())
				fail();
			if (modelManager.getSourceColumns().get(1).getColumnId() != COLUMN_DEGREE.getColumnId())
				fail();

			if (modelManager.getViewColumns().get(0).getColumnId() != COLUMN_STYLE_NAME.getColumnId())
				fail();
			if (modelManager.getViewColumns().get(1).getColumnId() != COLUMN_DEGREE.getColumnId())
				fail();
			
			if (modelManager.getSortOrdering().get(0).getColumnId() != COLUMN_STYLE_NAME.getColumnId())
				fail();
						
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
}
