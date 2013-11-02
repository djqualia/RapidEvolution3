package com.mixshare.rapid_evolution.data.record;

import java.util.Map;

import org.apache.lucene.document.Document;

import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class AbstractRecord implements Record {

	/**
	 * This method is truly abstract in the sense that each inheriting class
	 * will implement some of this method while calling super.mergeWith to
	 * incorporate the parent class(es) logic...
	 */
	@Override
	abstract public void mergeWith(Record record, Map<Record, Object> recordsToRefresh);

	@Override
	abstract public void update();

    @Override
	abstract public void write(LineWriter textWriter);

    abstract public void addFieldsToDocument(Document document);

    abstract public void setId(Identifier id);

}
