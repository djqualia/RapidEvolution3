package com.mixshare.rapid_evolution.data.record;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.identifier.Identifiable;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.IdentifierParser;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.RWSemaphore;
import com.mixshare.rapid_evolution.util.timing.SemaphoreFactory;

abstract public class CommonRecord extends AbstractRecord implements Identifiable, Serializable, Comparable<Record>, DataConstants, AllColumns {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(CommonRecord.class);

    static private SemaphoreFactory duplicateIdSem = new SemaphoreFactory();

    ////////////
    // FIELDS //
    ////////////

    protected int uniqueId;
    protected int[] duplicateIds;

    protected Identifier id;

    private byte rating; // 0 - 100, translated to 1 to 5 stars, could be set by user or retrieved online...
    private boolean disabled; // disabled items will not show up when searching, can be used to "delete" a record yet still retain memory of it so as not to add it again
    private long lastModified = System.currentTimeMillis();

    transient protected RWSemaphore writeLockSem; // this is used to prevent changing parts the profile while it is being saved (protect from concurrent modification exceptions)
    transient private boolean relationalItemsChanged = false; // used for performance optimizations, this flag is set to determine whether the index update process should update relational items

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(CommonRecord.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("writeLockSem") || pd.getName().equals("relationalItemsChanged")) {
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

    public CommonRecord() { }
    public CommonRecord(LineReader lineReader) {
    	int version = Integer.parseInt(lineReader.getNextLine()); // version
    	uniqueId = Integer.parseInt(lineReader.getNextLine());
    	int numDuplicateIds = Integer.parseInt(lineReader.getNextLine());
    	duplicateIds = new int[numDuplicateIds];
    	for (int i = 0; i < numDuplicateIds; ++i)
    		duplicateIds[i] = Integer.parseInt(lineReader.getNextLine());
    	id = IdentifierParser.getIdentifier(lineReader.getNextLine());
    	rating = Byte.parseByte(lineReader.getNextLine());
    	disabled = Boolean.parseBoolean(lineReader.getNextLine());
    	lastModified = Long.parseLong(lineReader.getNextLine());
    }

    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////

    abstract public Index getIndex();

    /////////////
    // GETTERS //
    /////////////

    @Override
	public Identifier getIdentifier() { return id; }
    @Override
	public int getUniqueId() { return uniqueId; }

    @Override
	public int getNumDuplicateIds() {
    	if (duplicateIds == null)
    		return 0;
    	return duplicateIds.length;
    }
    @Override
	public int getDuplicateId(int index) { return duplicateIds[index]; }
    public boolean containsDuplicateIdentifier(int otherId) {
    	if (duplicateIds != null) {
			for (int duplicateId : duplicateIds)
				if (duplicateId == otherId)
					return true;
    	}
    	return false;
    }

    public byte getRating() { return rating; }
    public Rating getRatingValue() { return Rating.getRating(rating); }
    @Override
	public boolean isDisabled() { return disabled; }
    public Date getLastModifiedDate() { return new Date(lastModified); }
    @Override
	public long getLastModified() { return lastModified; }

    @Override
	public RWSemaphore getWriteLockSem() {
    	if (writeLockSem == null)
    		writeLockSem = new RWSemaphore(RE3Properties.getLong("write_lock_sem_timeout_millis"));
    	return writeLockSem;
    }

    @Override
	public boolean areRelationalItemsChanged() { return relationalItemsChanged; }

    // for serialization
    public int[] getDuplicateIds() { return duplicateIds; }
	public Identifier getId() { return id; }

    /////////////
    // SETTERS //
    /////////////

    /**
     * Do not call this directly.
     */
    @Override
	public void setId(Identifier id) {
    	this.id = id;
    }

    public void addDuplicateIdentifier(int duplicateId) { updateDuplicateIdentifier(duplicateId, true); }
    public void removeDuplicateIdentifier(int duplicateId) { updateDuplicateIdentifier(duplicateId, false); }
    private void updateDuplicateIdentifier(int duplicateId, boolean add) {
    	try {
    		duplicateIdSem.acquire(uniqueId << 4 + getDataType());
    		getWriteLockSem().startRead("updateDuplicateIdentifier");
	    	if (add) {
	        	if (!containsDuplicateIdentifier(duplicateId)) {
	        		if (duplicateIds == null) {
	        			duplicateIds = new int[1];
	        			duplicateIds[0] = duplicateId;
	        		} else {
	        			int[] newDuplicateIds = new int[duplicateIds.length + 1];
	        			for (int d = 0; d < duplicateIds.length; ++d) {
	        				newDuplicateIds[d] = duplicateIds[d];
	        			}
	        			newDuplicateIds[duplicateIds.length] = duplicateId;
	        			duplicateIds = newDuplicateIds;
	        		}
	        	}
	    	} else {
	    		// remove
	        	if (containsDuplicateIdentifier(duplicateId)) {
	        		if (duplicateIds.length == 1) {
	        			duplicateIds = new int[0];
	        		} else {
	        			int[] newDuplicateIds = new int[duplicateIds.length - 1];
	        			int i = 0;
	        			for (int d = 0; d < duplicateIds.length; ++d) {
	        				if (duplicateIds[d] != duplicateId)
	        					newDuplicateIds[i++] = duplicateIds[d];
	        			}
	        			duplicateIds = newDuplicateIds;
	        		}
	        	}
	    	}
    	} catch (Exception e) {
    		log.error("updateDuplicateIdentifier(): error", e);
    	} finally {
    		getWriteLockSem().endRead();
    		duplicateIdSem.release(uniqueId << 4 + getDataType());
    	}
    }

	public void setRatingValue(Rating rating) { this.rating = rating.getRatingValue(); }
	@Override
	public void setLastModified() { lastModified = System.currentTimeMillis(); }
	@Override
	public void setDisabled(boolean disabled) { this.disabled = disabled; }

	@Override
	public void setRelationalItemsChanged(boolean relationalItemsChanged) { this.relationalItemsChanged = relationalItemsChanged; }

    @Override
	public String toString() {
    	if (getIdentifier() != null)
    		return getIdentifier().toString();
    	return "";
    }

	// for serialization
	public void setDuplicateIds(int[] duplicateIds) { this.duplicateIds = duplicateIds; }
	public void setUniqueId(int uniqueId) { this.uniqueId = uniqueId; }
	public void setRating(byte rating) { this.rating = rating; }
	public void setLastModified(long lastModified) { this.lastModified = lastModified; }

	/////////////
	// METHODS //
	/////////////

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	CommonRecord commonRecord = (CommonRecord)record;
    	// id
    	addDuplicateIdentifier(commonRecord.getUniqueId());
    	// duplicate ids
    	for (int d = 0; d < commonRecord.getNumDuplicateIds(); ++d) {
    		addDuplicateIdentifier(commonRecord.getDuplicateId(d));
    	}
    	// rating
    	if ((rating != 0) && (commonRecord.getRatingValue().getRatingValue() != 0)) {
    		// average them together
    		rating = (byte)((rating + commonRecord.getRatingValue().getRatingValue()) / 2);
    	} else if (commonRecord.getRatingValue().getRatingValue() != 0) {
    		rating = commonRecord.getRatingValue().getRatingValue();
    	}
    }

    @Override
	public boolean equals(Object o) {
    	if (o instanceof Record) {
    		Record oR = (Record)o;
    		return ((getDataType() == oR.getDataType()) && (getUniqueId() == oR.getUniqueId()));
    	}
    	return false;
    }
    @Override
	public int hashCode() {
    	return ((getDataType()) << 8) + getUniqueId();
    }

    @Override
	public int compareTo(Record r) { return toString().compareToIgnoreCase(r.toString()); }

    @Override
	public void update() {
    	setLastModified();
    	getIndex().update(this); // will notify UI
    }

    @Override
	public void write(LineWriter textWriter) {
    	textWriter.writeLine(String.valueOf(1), "CommonRecord.version"); // version
    	textWriter.writeLine(String.valueOf(uniqueId), "uniqueId");
    	if (duplicateIds != null) {
    		textWriter.writeLine(String.valueOf(duplicateIds.length), "numDuplicateIds");
    		for (int dupId : duplicateIds)
    			textWriter.writeLine(dupId);
    	} else {
    		textWriter.writeLine(String.valueOf(0), "numDuplicateIds");
    	}
    	textWriter.writeLine(id.getUniqueId(), "idUniqueId");
    	textWriter.writeLine(String.valueOf(rating), "rating");
    	textWriter.writeLine(String.valueOf(disabled), "disabled");
    	textWriter.writeLine(String.valueOf(lastModified), "lastmodified");
    }

    @Override
	public Document getDocument() {
    	Document result = new Document();
    	addFieldsToDocument(result);
    	return result;
    }

    @Override
	public void addFieldsToDocument(Document document) {
    	document.add(new Field("id", String.valueOf(getUniqueId()), Field.Store.YES, Field.Index.ANALYZED));
    	if (duplicateIds != null) {
    		StringBuffer dupBuffer = new StringBuffer();
    		for (int dupId : duplicateIds) {
    			Identifier dupIdentifier = getIndex().getIdentifierFromUniqueId(dupId);
    			if (dupIdentifier != null) {
    				if (dupBuffer.length() > 0)
    					dupBuffer.append(", ");
    				dupBuffer.append(dupIdentifier.toString());
    			}
    		}
    		if (dupBuffer.length() > 0)
    			document.add(new Field("duplicate_identifiers", dupBuffer.toString(), Field.Store.YES, Field.Index.ANALYZED));
    	}
    	document.add(new Field("disabled", isDisabled() ? "1" : "0", Field.Store.YES, Field.Index.ANALYZED));
    }

}
