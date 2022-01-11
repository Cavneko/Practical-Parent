package ca.cmpt276.flame.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A ChildrenQueue represents a queue of children. It is used for both flipping
 * coins (deciding who flips next) and within each task to decide who gets to go
 * next. It is possible to override the next child (i.e. in the FlipCoinActivity).
 * This does not use an actual Queue internally, but rather orders the children
 * based upon history, which works much better if children are added or deleted.
 */
public class ChildrenQueue {
    private static final long NO_OVERRIDE = -1;
    private final List<Long> history = new ArrayList<>();
    private long overrideNextChildId = NO_OVERRIDE;

    protected Child getNext() {
        if(overrideNextChildId != NO_OVERRIDE) {
            return ChildrenManager.getInstance().getChild(overrideNextChildId);
        }

        List<Child> queue = getQueue();
        if(queue.isEmpty()) {
            return null;
        } else {
            return queue.get(0);
        }
    }

    protected void setOverride(Child child) {
        if(child != null) {
            overrideNextChildId = child.getId();
        } else {
            overrideNextChildId = Child.NONE;
        }
    }

    private void clearOverride() {
        overrideNextChildId = NO_OVERRIDE;
    }

    protected Child takeTurn() {
        Child child = getNext();

        if(child != null) {
            history.remove(child.getId());
            history.add(child.getId());
        }

        clearOverride();
        return child;
    }

    // front of the list (i.e. index 0) corresponds to the front of the queue
    protected List<Child> getQueue() {
        ChildrenManager childrenManager = ChildrenManager.getInstance();

        // add the children in the order they were added to the app
        List<Child> queue = new ArrayList<>();
        for(Child child : childrenManager) {
            queue.add(child);
        }

        // then rearrange the children according to the history
        Iterator<Long> histItr = history.iterator();
        while(histItr.hasNext()) {
            Child child = childrenManager.getChild(histItr.next());

            if(child != null) {
                queue.remove(child);
                queue.add(child);
            } else {
                histItr.remove();
            }
        }

        // if an override has been put in place, move that child to the front of the queue
        Child overrideChild = childrenManager.getChild(overrideNextChildId);

        if(overrideChild != null) {
            queue.remove(overrideChild);
            queue.add(0, overrideChild);
        }

        return queue;
    }
}
