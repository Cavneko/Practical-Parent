package ca.cmpt276.flame.model;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * ChildrenManager is a singleton that manages multiple Child objects.
 * It is saved to SharedPreferences so that children persist when the
 * app is closed and restarted.
 */
public class ChildrenManager implements Iterable<Child> {
    private static final String SHARED_PREFS_KEY = "SHARED_PREFS_CHILDREN_MANAGER";
    private static ChildrenManager childrenManager;
    private long nextChildId = 1L;
    private final LinkedHashMap<Long, Child> children = new LinkedHashMap<>();

    // Singleton

    public static ChildrenManager getInstance() {
        if(childrenManager == null) {
            childrenManager = (ChildrenManager) PrefsManager.restoreObj(SHARED_PREFS_KEY, ChildrenManager.class);
        }

        return childrenManager;
    }

    // Normal class

    private ChildrenManager() {
        // singleton: prevent other classes from creating new ones
    }

    public Child addChild(String name) {
        Child child = new Child(name);
        children.put(child.getId(), child);
        persistToSharedPrefs();
        return child;
    }

    // may return null if the child ID does not exist
    public Child getChild(long id) {
        if(children.containsKey(id)) {
            return children.get(id);
        }

        return null;
    }

    public void renameChild(Child child, String name) {
        checkValidChild(child);
        child.setName(name);
        persistToSharedPrefs();
    }

    public void setChildHasImage(Child child) {
        checkValidChild(child);
        child.setHasImage();
        persistToSharedPrefs();
    }

    public void removeChildImage(Child child, Context context) {
        checkValidChild(child);
        child.removeImage(context);
        persistToSharedPrefs();
    }

    public void removeChild(Child child, Context context) {
        checkValidChild(child);
        FlipManager.getInstance().removeChildFromHistory(child.getId());
        child.removeImage(context);
        children.remove(child.getId());
        persistToSharedPrefs();
    }

    protected long getNextChildId() {
        return nextChildId++;
    }

    private void checkValidChild(Child child) {
        if(child == null) {
            throw new IllegalArgumentException("ChildrenManager expects non-null child");
        } else {
            if(!children.containsKey(child.getId())) {
                throw new IllegalArgumentException("ChildrenManager expects ID to correspond to valid child");
            }
        }
    }

    private void persistToSharedPrefs() {
        PrefsManager.persistObj(SHARED_PREFS_KEY, this);
    }

    @NonNull
    @Override
    public Iterator<Child> iterator() {
        return children.values().iterator();
    }
}
