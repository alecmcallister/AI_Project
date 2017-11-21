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

    /**
     * De novo constructor. Requires a TimeSlot as an Assignment to no known time does't make any sense.
     *
     * @param timeSlot The TimeSlot to represent with this Assignment.
     */
    public Assignment(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    /**
     * Copy constructor. Copies the TimeSlot and the list of courses assigned. The new list is independent of
     * the previous one (i.e. changes to the list of courses in the new Assignment will not affec the list of
     * courses in the old one).
     *
     * @param other The Assignment to copy.
     */
    public Assignment(Assignment other) {
        this.timeSlot = other.getTimeSlot();
        this.assignments = other.getAssignments();
    }


    /**
     * Assign a new course/lab to the slot managed by this assignment, iff the type of the new SlotItem matches
     * the type of the TimeSlot.
     *
     * @param item The new SlotItem to be putatively added to the assignment.
     */
    public void assign(SlotItem item) {
        if ((timeSlot.isLectureSlot() && item.isLecture())
                || (timeSlot.isLabSlot() && !item.isLecture())) {
            assignments.add(item);
        }
    }

    /**
     * Get the list of courses assigned to the managed TimeSlot. This actually retrieves a copy of the
     * underlying list for the sake of encapsulation. This is an O(n) operation, but as assignments should
     * generally be quite small, this inefficiency is expected to be negligible.
     *
     * @return A copy of the list of courses assigned to the TimeSlot managed by this Assignment.
     */
    public ArrayList<SlotItem> getAssignments() {
        ArrayList<SlotItem> rv = new ArrayList<>();
        rv.addAll(assignments);
        return rv;
    }

    /**
     * Get the TimeSlot managed by this Assignment.
     *
     * @return The TimeSlot managed by this assignment.
     */
    public TimeSlot getTimeSlot() {
        return this.timeSlot;
    }
}
