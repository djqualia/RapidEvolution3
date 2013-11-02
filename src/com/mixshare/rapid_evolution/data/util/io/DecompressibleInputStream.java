package com.mixshare.rapid_evolution.data.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import org.apache.log4j.Logger;

/**
 * This is a custom ObjectInputStream which will continue to read serialized objects
 * even if the serialVersionUID has changed.  A warning message will be displayed when
 * this occurs, as it could potentially be fatal.  However, this will allow most classes
 * whose signature has changed slightly to continue to be read.
 */
public class DecompressibleInputStream extends ObjectInputStream {

    static private Logger log = Logger.getLogger(DecompressibleInputStream.class);

    public DecompressibleInputStream(InputStream in) throws IOException {
        super(in);
    }

    /**
     * This will read the class descriptor and if there is a difference in serialVersionUID
     * values it will replace the class descriptor with the one that is currently available
     * and log a warning message.  In many cases this will not cause a problem and allow the
     * serialized Object to be read, but sometimes can result in a fatal operation.
     *
     * @return ObjectStreamClass
     */
    @Override
	protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        // initially streams descriptor
        ObjectStreamClass resultClassDescriptor = super.readClassDescriptor();
        Class<?> localClass = null;
        try {
            localClass = Class.forName(resultClassDescriptor.getName());
            ObjectStreamClass localClassDescriptor = ObjectStreamClass.lookup(localClass);
            // only if class implements serializable
            if (localClassDescriptor != null) {
                final long localSUID = localClassDescriptor.getSerialVersionUID();
                final long streamSUID = resultClassDescriptor.getSerialVersionUID();
                // check for serialVersionUID mismatch
                if (streamSUID != localSUID) {
                    if (log.isDebugEnabled()) {
                        final StringBuffer s = new StringBuffer("Overriding serialized class version mismatch: ");
                        s.append("local serialVersionUID=").append(localSUID);
                        s.append(" stream serialVersionUID=").append(streamSUID);
                        //Exception e = new InvalidClassException(s.toString());
                        //log.warn("readClassDescriptor(): potentially fatal deserialization operation");
                    }
                    // use local class descriptor for deserialization
                    resultClassDescriptor = localClassDescriptor;
                }
            }
            return resultClassDescriptor;
        } catch (ClassNotFoundException e) {
        	if (log.isTraceEnabled())
        		log.trace("readClassDescriptor(): no local class for " + resultClassDescriptor.getName(), e);
            return resultClassDescriptor;
        }
    }

}