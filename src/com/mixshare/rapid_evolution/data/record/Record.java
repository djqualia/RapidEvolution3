package com.mixshare.rapid_evolution.data.record;

import java.util.Map;

import org.apache.lucene.document.Document;

import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.RWSemaphore;

/**
 * A record stores the minimal portion of the profile information in memory used for searching and lookups...
 */
public interface Record {

	public byte getDataType();

	public int getUniqueId();
	public Identifier getIdentifier();

	public int getNumDuplicateIds();
	public int getDuplicateId(int index);
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh);

    public boolean isDisabled();
    public void setDisabled(boolean disabled);

    public long getLastModified();
    public void setLastModified();

    public void update(); // called after changing to fire event listeners/UI/etc

    public boolean areRelationalItemsChanged();
    public void setRelationalItemsChanged(boolean changed);

    public RWSemaphore getWriteLockSem();

    public void write(LineWriter textWriter);

    public Document getDocument();

}
