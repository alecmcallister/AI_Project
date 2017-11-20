package ai.project;

import java.util.ArrayList;

/**
 * Assignment Class
 *
 * Represents an assignment of some number of courses to a TimeSlot.
 *
 * Note that this is separate from the TimeSlot class in order to allow TimeSlots to be constant. Because we may try
 * multiple different assignments to the same TimeSlot, if we did not distinguish between TimeSlots and Assignments,
 * we would be updating the same TimeSlot across the entire Department each time we tried a new assignment.
 */
public class Assignment {
    private TimeSlot timeSlot;
    private ArrayList<SlotItem> assignments = new ArrayList<>();

    public Assignment(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }


}
