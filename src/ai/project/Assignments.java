package ai.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Assignments Class
 *
 * Essentially a wrapper over a Map, which maps a TimeSlots to a set of Courses assigned to it.
 */
public class Assignments {
    private TimeSlot timeSlot;
    private ArrayList<SlotItem> assignments = new ArrayList<>();

    private HashMap<TimeSlot, HashSet<SlotItem>> assigns;

    public Assignments() {
        assigns = new HashMap<>();
    }

    /**
     * Copy constructor. Basically just copies the assignment hashmap.
     *
     * @param other The Assignments to copy.
     */
    public Assignments(Assignments other) {
        

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
