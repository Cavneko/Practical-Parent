package ca.cmpt276.flame.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * FlipManager is a singleton class that manages coin flips and
 * a list of FlipHistoryEntry objects. It is persisted between app
 * launches using SharedPreferences.
 */
public class FlipManager implements Iterable<FlipHistoryEntry> {
    /** CoinSide represents the two possible sides of a coin */
    public enum CoinSide {
        HEADS,
        TAILS
    }

    private static final String SHARED_PREFS_KEY = "SHARED_PREFS_FLIP_MANAGER";
    private static FlipManager flipManager;
    private final ChildrenQueue childrenQueue = new ChildrenQueue();
    private final List<FlipHistoryEntry> history = new ArrayList<>();

    // Singleton

    public static FlipManager getInstance() {
        if(flipManager == null) {
            flipManager = (FlipManager) PrefsManager.restoreObj(SHARED_PREFS_KEY, FlipManager.class);
        }

        return flipManager;
    }

    // Normal class

    private FlipManager() {
        // singleton: prevent other classes from creating new ones
    }

    public void overrideTurnChild(Child child) {
        childrenQueue.setOverride(child);
        persistToSharedPrefs();
    }

    // may return null if there are no children configured / no child is flipping
    public Child getTurnChild() {
        return childrenQueue.getNext();
    }

    public List<Child> getTurnQueue() {
        return childrenQueue.getQueue();
    }

    // performs a coin flip, adds it to the history and returns the result
    public CoinSide doFlip(CoinSide selection) {
        CoinSide result = getRandomCoinSide();
        Child child = childrenQueue.takeTurn();

        if(child == null) {
            history.add(new FlipHistoryEntry(Child.NONE, result, false));
        } else {
            history.add(new FlipHistoryEntry(child.getId(), result, result == selection));
        }

        persistToSharedPrefs();
        return result;
    }

    private CoinSide getRandomCoinSide() {
        CoinSide[] sides = CoinSide.values();
        int randomChoice = (int) Math.floor(Math.random() * sides.length);
        return sides[randomChoice];
    }

    public CoinSide getLastCoinValue() {
        if(history.size() == 0) {
            return CoinSide.TAILS;
        }

        return history.get(history.size() - 1).getResult();
    }

    // should only be called by the ChildrenManager when a child is deleted
    protected void removeChildFromHistory(long childId) {
        for(int i = 0; i < history.size(); i++) {
            long historyChildId = history.get(i).getChildId();
            if(historyChildId == childId) {
                history.remove(i);
                i--; // array size has now changed
            }
        }
        persistToSharedPrefs();
    }

    private void persistToSharedPrefs() {
        PrefsManager.persistObj(SHARED_PREFS_KEY, this);
    }

    @NonNull
    @Override
    public Iterator<FlipHistoryEntry> iterator() {
        return history.iterator();
    }
}
