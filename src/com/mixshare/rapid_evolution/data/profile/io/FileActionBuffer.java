package com.mixshare.rapid_evolution.data.profile.io;

import java.io.File;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.util.timing.RWSemaphore;

/**
 * For use with the FileLimitingProfileIO.  Buffers writes/deletes...
 *
 * @param <T> The type of the object in the buffer.
 * @param <I> The identifier for the object types,
 */
public class FileActionBuffer<T, I> {

    static private Logger log = Logger.getLogger(FileActionBuffer.class);

	// stats:
	static long PENDING_ACTIONS_COLLAPSED = 0; // Measures when multiple updates come in for the same profile, which are batched.
	static long PENDING_ACTIONS_ADDED = 0;
	static long NEW_MUTATIONS_COUNT = 0; // Any write that is the first to go to a file, which had nothing to batch with.
	static long BATCHED_MUTATIONS_COUNT = 0; // Any write that gets batched together with an existing write

	enum Action {
		ADD_OR_UPDATE,
		DELETE
	}

	static public interface ActionApplicatorFactory<T, I> {
		ActionApplicator<T, I> getNewInstance();
	}

	static public interface ActionApplicator<T, I> {
		Map<I, T> load(String filename);
		boolean save(String filename, Map<I, T> data);
	}

	private final String filename;
	private final Vector<PendingAction> pendingActions = new Vector<PendingAction>();
	private final RWSemaphore fileSem = new RWSemaphore(15000);
	private final ActionApplicatorFactory<T, I> factory;

	public FileActionBuffer(String filename, ActionApplicatorFactory<T, I> factory) {
		this.filename = filename;
		this.factory = factory;
	}

	public RWSemaphore getSem() { return fileSem; }
	public String getFilename() { return filename; }

	public T getPendingObject(I identifier) {
		for (PendingAction pendingAction : pendingActions) {
			if (pendingAction.identifier.equals(identifier)) {
				return pendingAction.object;
			}
		}
		return null;
	}

	public synchronized void addOrUpdate(I identifier, T object) {
		addAction(identifier, object, Action.ADD_OR_UPDATE);
	}

	public synchronized void delete(I identifier) {
		addAction(identifier, null, Action.DELETE);
	}

	private void addAction(I identifier, T object, Action action) {
		if (pendingActions.size() == 0) {
			++NEW_MUTATIONS_COUNT;
		} else {
			++BATCHED_MUTATIONS_COUNT;
		}
		for (PendingAction pendingAction : pendingActions) {
			if (pendingAction.identifier.equals(identifier)) {
				// Updating/replace the pending action...
				pendingAction.object = object;
				pendingAction.action = action;
				++PENDING_ACTIONS_COLLAPSED;
				return;
			}
		}
		// Not found, add a new one
		pendingActions.add(new PendingAction(identifier, object, action));
		++PENDING_ACTIONS_ADDED;
	}

	public synchronized boolean applyActions() {
		if (pendingActions.size() == 0)
			return true;
		ActionApplicator<T, I> applicator = factory.getNewInstance();
		Map<I, T> objects = applicator.load(filename);
		for (PendingAction pendingAction : pendingActions) {
			if (pendingAction.action == Action.ADD_OR_UPDATE) {
				objects.put(pendingAction.identifier, pendingAction.object);
			} else if (pendingAction.action == Action.DELETE) {
				objects.remove(pendingAction.identifier);
			} else {
				throw new RuntimeException("Unknown action type=" + pendingAction.action);
			}
		}
		boolean success = false;
		if (objects.size() == 0) {
			int tries = 0;
			File existingFile = new File(filename);
			while (!existingFile.delete() && existingFile.exists() && !RapidEvolution3.isTerminated && (tries < FileLimitingProfileIO.FILE_DELETE_MAX_TRIES)) {
				if (log.isDebugEnabled())
					log.debug("deleteProfile(): could not delete existingFile=" + existingFile + ", waiting...");
				try {
					Thread.sleep(FileLimitingProfileIO.FILE_DELETE_WAIT_INTERVAL);
				} catch (InterruptedException e) { }
				++tries;
			}
			success = true;
		} else {
			success = applicator.save(filename, objects);
		}
		if (success)
			pendingActions.clear();
		return success;
	}

	private class PendingAction {
		final I identifier;
		T object;
		Action action;
		PendingAction(I identifier, T object, Action action) {
			this.identifier = identifier;
			this.object = object;
			this.action = action;
		}
	}
}
