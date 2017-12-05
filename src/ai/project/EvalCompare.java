package ai.project;

import java.util.Comparator;

/**
 * EvalCompare class
 *
 * A comparator which compares Evaluated instances by their eval score.
 */
public class EvalCompare implements Comparator<Evaluated> {
    @Override
    public int compare(Evaluated e1, Evaluated e2) {
        // Same eval score; compare by TimeSlot-related values
        if (e1.getEval() == e2.getEval()) {
            TimeSlot t1 = e1.getTimeSlot();
            TimeSlot t2 = e2.getTimeSlot();
            SlotType type1 = t1.getSlotType();
            SlotType type2 = t2.getSlotType();
            if (t1.getSlotType() == t2.getSlotType()) {
                Double time1 = t1.getTime();
                Double time2 = t2.getTime();

                return (time1.compareTo(time2));
            }
            else {
                Integer day1Ord = type1.ordinal();
                Integer day2Ord=  type2.ordinal();

                return (day1Ord.compareTo(day2Ord));
            }
        }

        // Eval score different, compare
        return (e1.getEvalWrapped().compareTo(e2.getEvalWrapped()));
    }
}
