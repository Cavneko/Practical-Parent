package ca.cmpt276.flame.model;

/**
 * BreathsManager is a singleton that manages saving state for the Take a Breath activity
 */
public class BreathsManager {
    public static final int MIN_BREATHS = 1;
    public static final int MAX_BREATHS = 10;
    private static final int DEFAULT_NUM_BREATHS = 3;
    private static final String SHARED_PREFS_KEY = "SHARED_PREFS_BREATHS_MANAGER";
    private static BreathsManager breathsManager;
    private int numBreaths = DEFAULT_NUM_BREATHS;

    // Singleton

    public static BreathsManager getInstance() {
        if(breathsManager == null) {
            breathsManager = (BreathsManager) PrefsManager.restoreObj(SHARED_PREFS_KEY, BreathsManager.class);
        }

        return breathsManager;
    }

    // Normal class

    private BreathsManager() {
        // singleton: prevent other classes from creating new ones
    }

    public int getNumBreaths() {
        return numBreaths;
    }

    public void setNumBreaths(int numBreaths) {
        if(numBreaths < MIN_BREATHS || numBreaths > MAX_BREATHS) {
            throw new IllegalArgumentException("BreathsManager expects number of breaths to be within the min/max range");
        }

        this.numBreaths = numBreaths;
        persistToSharedPrefs();
    }


    private void persistToSharedPrefs() {
        PrefsManager.persistObj(SHARED_PREFS_KEY, this);
    }
}
