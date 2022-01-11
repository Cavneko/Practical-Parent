package ca.cmpt276.flame.model;

import java.util.Date;

/**
 * FLipHistoryEntry manages a coin flip history entry. It contains the UUID of
 * the Child who flipped the coin, what the result of the flip was, whether
 * the child won or not and the date of the flip.
 */
public class FlipHistoryEntry {
    private final long childId;
    private final FlipManager.CoinSide result;
    private final Boolean won;
    private final Date date;

    public FlipHistoryEntry(long childId, FlipManager.CoinSide result, Boolean won) {
        this.childId = childId;
        this.result = result;
        this.won = won;
        this.date = new Date();
    }

    // may return null if no child associated with the flip
    public long getChildId() {
        return childId;
    }

    public FlipManager.CoinSide getResult() {
        return result;
    }

    public Boolean wasWon() {
        return won;
    }

    public Date getDate() {
        return date;
    }
}
