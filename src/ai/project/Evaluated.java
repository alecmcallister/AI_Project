package ai.project;

import java.util.Comparator;

/**
 * Evaluated class
 *
 * This class represents a pairing of a TimeSlot and an evaluation (soft constraint) value.
 * The reason this class is needed is to allow automatic sorting by a simple comparator on a TreeSet when we compute
 * eval for all possible assignments for a new SlotItem.
 *
 * Eval must be computed separately (currently done in the Assignments class).
 * Note that instances of this class are not meant to be permanent; if assignments change they may render the eval value
 * in an instance of Evaluated outdated. In other words, Evaluated offers no guarantee that the eval value will be
 * valid once Assignments change.
 */
public class Evaluated {
    private TimeSlot timeSlot;
    private int eval;

    public Evaluated(TimeSlot timeSlot, int eval) {
        this.timeSlot = timeSlot;
        this.eval = eval;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public int getEval() {
        return eval;
    }

    public Integer getEvalWrapped() {
        return eval;
    }
}
