package ai.project;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Assignments Class
 *
 * Essentially a wrapper over a Map, which maps TimeSlots to their set of assigned courses.
 *
 * Does some basic type enforcement; this class will not accept an assignment of a lecture to a lab TimeSlot, for
 * instance. Could be expanded to do more constraint enforcement if need be.
 *
 * Usage:
 *  - Instantiate either de novo with the default constructor or as a copy of another Assignments
 *  - call assign() to add a new course to a given TimeSlot
 *      > this will fail silently if the type of TimeSlot and course are mismatched
 *  - call getAssignments() to get all assignments for a particular TimeSlot
 *  - call getAllAssignments() to get a copy of the map of all assignments
 *
 */
public class Assignments {
    private HashMap<TimeSlot, HashSet<SlotItem>> assignments;

    /**
     * Default constructor. Creates a new map of TimeSlots to courses.
     */
    public Assignments() {
        assignments = new HashMap<>();
    }

    /**
     * Copy constructor. Basically just copies the assignment HashMap.
     *
     * @param other The Assignments to copy.
     */
    public Assignments(Assignments other) {
        this.assignments = other.getAllAssignments();
    }

    /**
     * Assign a new course/lab to a time slot in this set of assignments, iff the type of the new SlotItem matches
     * the type of the TimeSlot.
     *
     * This does not enforce maximum course constraints, though it could easily be made to do so.
     *
     * @param item The new SlotItem to be putatively added to the assignment.
     */
    public void assign(TimeSlot timeSlot, SlotItem item) {
        if ((timeSlot.isLectureSlot() && item.isLecture())
                || (timeSlot.isLabSlot() && !item.isLecture())) {
            HashSet<SlotItem> set = assignments.get(timeSlot);
            if (set == null) {
                // Create a new assignment for this TimeSlot if one doesn't exist
                set = new HashSet<>();
                assignments.put(timeSlot, set);
            }
            set.add(item);
        }
    }

    /**
     * Retrieves a copy of the set of assignments for a given TimeSlot in the set of Assignments.
     * This is an O(n) operation, but because no TimeSlot should be assigned a huge number of courses, this should
     * still perform pretty quickly.
     *
     * The TimeSlot passed in need not be reference equal, i.e. it can have the same day and time, but be a different
     * object.
     *
     * May return null if nothing is yet assigned to the given TimeSlot.
     *
     * @param timeSlot The TimeSlot for which to retrieve the set of assignments.
     * @return The set of assignments for the given TimeSlot if it exists in these Assignments. If not, returns null.
     */
    public HashSet<SlotItem> getAssignment(TimeSlot timeSlot) {
        return new HashSet<SlotItem>(assignments.get(timeSlot));
    }

    /**
     * Get the number of SlotItems (lectures or labs) assigned to the given TimeSlot.
     * Returns 0 if the TimeSlot has not been assigned any courses (does not exist in the map).
     *
     * @param timeSlot The TimeSlot (not necessarily reference equal) to look for.
     * @return The number of elements in the set of courses mapped to the TimeSlot.
     */
    public int getNumAssigned(TimeSlot timeSlot) {
        HashSet<SlotItem> set = assignments.get(timeSlot);
        return (timeSlot == null) ? 0 : set.size();
    }

    /**
     * Get the list of courses assigned to the managed TimeSlot. This actually retrieves a shallow copy of the
     * underlying map for the sake of encapsulation, which may be a slightly expensive operation if this object
     * is managing a lot of assignments.
     *
     * @return A copy of the map of courses assigned with this Assignments object.
     */
    public HashMap<TimeSlot, HashSet<SlotItem>> getAllAssignments() {
        return new HashMap<TimeSlot, HashSet<SlotItem>>(assignments);
    }
}
