package ai.project;

import java.io.IOException;
import java.util.*;

/**
 * Assignments Class
 *
 * Essentially a wrapper over a Map, which maps TimeSlots to their set of assigned courses.
 *
 * Does some basic type enforcement; this class will not accept an assignment of a lecture to a lab TimeSlot, for
 * instance. Could be expanded to do more constraint enforcement if need be.
 *
 * This class was written with the assumption that the underlying TimeSlot map will only track TimeSlots that
 * have something assigned to them already. In other words, Assignments objects have no inherent awareness of TimeSlots
 * that have nothing assigned to them yet.
 *
 * Usage:
 *  - Instantiate either de novo with the default constructor or as a copy of another Assignments
 *  - call addAssignment() to add a new course to a given TimeSlot
 *      > this will fail silently if the type of TimeSlot and course are mismatched
 *  - call getAssignments() to get all assignments for a particular TimeSlot
 *  - call getAllAssignments() to get a copy of the map of all assignments
 *
 */
public class Assignments {
    private HashMap<TimeSlot, HashSet<SlotItem>> assignments;
    private Penalties penalties;
    private int evalScore;
    private HashMap<SlotItem, UnfilledPairs> unfilledPairsMap;

    /**
     * Default constructor. Creates a new map of TimeSlots to courses.
     *
     * @param penalties Penalties for the department. Needed, as otherwise evaluation methods won't make any sense.
     * @param table TimeTable is required to calculate a baseline penalty for the total number of courses and labs with minimums.
     */
    public Assignments(Penalties penalties, TimeTable table) {
        assignments = new HashMap<>();
        this.penalties = penalties;
        evalScore = (table.getTotalLabsWithMinimum() * penalties.getLabsMin())
                    + (table.getTotalLecturesWithMinimum() * penalties.getCourseMin());
        unfilledPairsMap = new HashMap<>();
    }

    /**
     * Copy constructor. Basically just copies the assignment HashMap, Penalties, and eval score.
     *
     * @param other The Assignments to copy.
     */
    public Assignments(Assignments other) {
        this.assignments = other.getAllAssignments();
        this.penalties = other.getPenalties();
        this.evalScore = other.getEvalScore();
        this.unfilledPairsMap = other.getUnfilledPairsMap();
    }

    /**
     * Assign a new course/lab to a time slot in this set of assignments, iff the type of the new SlotItem matches
     * the type of the TimeSlot.
     *
     * This does not enforce maximum course constraints, though it could easily be made to do so.
     *
     * @param timeSlot The new TimeSlot to be assigned to.
     * @param item The new SlotItem to be putatively added to the assignment.
     */
    public void addAssignment(TimeSlot timeSlot, SlotItem item) {
        if ((timeSlot.isLectureSlot() && item.isLecture())
                || (timeSlot.isLabSlot() && !item.isLecture())) {
            HashSet<SlotItem> set = assignments.get(timeSlot);
            if (set == null) {
                // Create a new assignment for this TimeSlot if one doesn't exist
                set = new HashSet<>();
                assignments.put(timeSlot, set);
            }
            set.add(item);

            // Adjust eval for this Assignments instance.
            evalScore = eval(timeSlot, item).getEval();

            // Handle pairs. We track unfilled pairs as we go so that we don't have to search the entire set of
            // Assignments every time we evaluate soft constraints. The actual eval adjustment is done with the
            // call to eval() above.
            if (item.hasPairs()) {
                for (SlotItem paired : item.getPairs()) {
                    // check if we've already added this
                    UnfilledPairs unfilled = unfilledPairsMap.get(paired);
                    if ((unfilled != null) && (unfilled.expectsPair(item))) {
                        // resolve the pair
                        unfilled.resolvePair(item);
                    }
                    else {
                        // The corresponding pair doesn't exist yet
                        UnfilledPairs thisUnfilled = unfilledPairsMap.get(item);
                        if (thisUnfilled == null) {
                            thisUnfilled = new UnfilledPairs(timeSlot);
                            unfilledPairsMap.put(item, thisUnfilled);
                        }
                        thisUnfilled.addPair(paired);
                    }
                }
            }
        }
    }

    /**
     * Given a SlotItem and a TimeTable (representing the full set of available TimeSlots irrespective of assignments)
     * this method finds all slots that meet constr() in the current Assignments (see below) and returns these slots
     * as an ordered set, sorted by an Eval comparator.
     *
     * N.B. This does not actually assign anything to the set of assignments. The addAssignment() method must still
     * be called if we want to modify the Assignments.
     *
     * @param timeTable The TimeTable with all valid TimeSlots for the Department. Needed because an Assignments instance
     *                  is not aware of any TimeSlots that it doesn't yet assign anything to.
     * @param slotItem The SlotItem we want to assign.
     */
    public ArrayList<Evaluated> assign(TimeTable timeTable, SlotItem slotItem) {
        ArrayList<Evaluated> rv = new ArrayList<>();

        // Save a bit of processing by only looking at slots that match the type of slotItem
        ArrayList<TimeSlot> slots = slotItem.isLecture() ? timeTable.getAllLectureSlots() : timeTable.getAllLabSlots();

        for (TimeSlot slot : slots) {
            if (constr(slot, slotItem)) rv.add(eval(slot, slotItem));
        }

        // Randomize ordering
        Collections.shuffle(rv);

        return rv;
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
        HashSet<SlotItem> assigned = assignments.get(timeSlot);
        if (assigned == null) return null;

        return new HashSet<>(assignments.get(timeSlot));
    }
    
    public TimeSlot getTimeSlot(SlotItem slotItem)
    {
    	for (TimeSlot element : assignments.keySet())
		{
			if (assignments.get(element).contains(slotItem))
				return element;
		}
    	return null;
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
        return (set == null) ? 0 : set.size();
    }

    /**
     * Get the list of courses assigned to the managed TimeSlot. This actually retrieves a shallow copy of the
     * underlying map for the sake of encapsulation, which may be a slightly expensive operation if this object
     * is managing a lot of assignments.
     *
     * @return A copy of the map of courses assigned with this Assignments object.
     */
    public HashMap<TimeSlot, HashSet<SlotItem>> getAllAssignments() {
        return new HashMap<>(assignments);
    }

    public Penalties getPenalties() { return penalties; }

    public int getEvalScore() { return evalScore; }


    /**
     * Get all TimeSlots that overlap with a given TimeSlot and have something assigned to them.
     *
     * @param timeSlot The TimeSlot that might overlap with other TimeSlots in the Assignments.
     * @return A set of all TimeSlots that have something assigned to them and overlap with timeSlot.
     */
    public HashSet<TimeSlot> getAssignedOverlaps(TimeSlot timeSlot) {
        HashSet<TimeSlot> rv = new HashSet<>();
        SlotType slotType = timeSlot.getSlotType();
        Set<TimeSlot> candidates = assignments.keySet();

        for (TimeSlot candidate : candidates) {
            if (timeSlot.overlaps(candidate)) rv.add(candidate);
        }

        return rv;
    }

    /**
     * Gets all SlotItems which exist in TimeSlots that overlap with a given timeSlot.
     *
     * @param timeSlot The TimeSlot that might overlap with other TimeSlots in the Assignments.
     * @return A set of all SlotItems that have been assigned to TimeSlots which overlap with timeSlot.
     */
    public HashSet<SlotItem> getAssignedOverlapCourses(TimeSlot timeSlot) {
        HashSet<SlotItem> rv = new HashSet<>();
        HashSet<TimeSlot> overlapTimes = getAssignedOverlaps(timeSlot);
        for (TimeSlot slot : overlapTimes) {
            rv.addAll(getAssignment(slot));
        }

        return rv;
    }

    /**
     * Gets a copy of the internal map which maps SlotItems to UnfilledPairs.
     * Intended mainly for the copy constructor.
     *
     * @return A copy of the internal map which maps SlotItems to UnfilledPairs.
     */
    public HashMap<SlotItem, UnfilledPairs> getUnfilledPairsMap() {
        return new HashMap<>(unfilledPairsMap);
    }


    // ------------- Hard Constraint Assessors -------------

    /**
     * Predicate that evaluates whether a given SlotItem, with a given TimeSlot, will satisfy constr() (the set of
     * hard constraints) for this Assignments object.
     *
     * The only constraint this does not check is partassign(). This is because any set of Assignments can be
     * instantiated with the partial assignments passed in through the constructor, so they'll already be included
     * in the set of assignments under consideration.
     *
     * Additionally, there are some limitations. Most notably, the CPSC 813/CPSC 913 hard constraint does not check
     * for incompatibility between their corresponding courses (313 for 813, 413 for 913). See doc for each constraint
     * for more details.
     *
     * @param timeSlot The TimeSlot for the putative assignment.
     * @param item The SlotItem being assigned.
     * @return True if all hard constraints are met for item assigned to timeSlot.
     */
    public boolean constr(TimeSlot timeSlot, SlotItem item) {
        return (underMax(timeSlot)
                && courseLabNoOverlap(timeSlot, item)
                && isFullyCompatible(timeSlot, item)
                && isNotUnwanted(timeSlot, item)
                && eveningClassInEveningSlot(timeSlot, item)
                && noOther500Level(timeSlot, item)
                && outsideTuesdayDeadZone(timeSlot)
                && satisfiesSpecialOverlap(timeSlot, item));
    }

    /**
     * Verifies that, after adding an additional SlotItem assigned to a given TimeSlot, the maximum for the TimeSlot
     * will still be equal to or less than the maximum number of courses/lectures for that slot.
     *
     * @param timeSlot The TimeSlot we might add an assignment to.
     * @return False if assigning a new SlotItem to timeSlot will cause it to exceed its maximum value. True otherwise.
     */
    private boolean underMax(TimeSlot timeSlot) {
        return ((getNumAssigned(timeSlot) + 1) <= timeSlot.getMax());
    }

    /**
     * Verifies that a TimeSlot we are trying to assign a SlotItem to does not contain conflicting lectures/labs.
     *  - If item is a lecture, we check that TimeSlot has no assignments for labs in the same section.
     *  - If item is a lab, we check that TimeSlot has no assignments for lectures in the same section.
     *
     * This method considers unparented labs to be incompatible with any section of the course.
     *
     * @param timeSlot The TimeSlot being examined.
     * @param item The SlotItem to assign.
     * @return False if the TimeSlot has assignments for conflicting courses/labs, true otherwise.
     */
    private boolean courseLabNoOverlap(TimeSlot timeSlot, SlotItem item) {
        HashSet<TimeSlot> overlapSlots = getAssignedOverlaps(timeSlot);
        HashSet<SlotItem> overlapCourses = getAssignedOverlapCourses(timeSlot);

        if (item.isLecture()) {
            // Check for overlapping labs
            for (SlotItem otherCourse : overlapCourses)
                if (!otherCourse.isLecture() && item.sameCourse(otherCourse)) {
                    // Found an assigned lab in the same course. See if there's a conflict.
                    Lab otherLab = (Lab)otherCourse;
                    if (!otherLab.hasParent() || otherLab.getParent().equals(item)) return false;
            }
        }
        else {
        // Check for overlapping lectures
            for (SlotItem otherCourse : overlapCourses)
                if (otherCourse.isLecture() && item.sameCourse(otherCourse)) {
                    // Found an assigned lecture in the same course. See if there's a conflict.
                    Lecture otherLec = (Lecture)otherCourse;
                    Lab thisLab = (Lab)item;
                    if (!thisLab.hasParent() || thisLab.getParent().equals(otherLec)) return false;
                    return false;
            }
        }

        return true;
    }

    /**
     * Verifies that a TimeSlot we are considering assigning a SlotItem to does not already have assignments which are
     * incompatible with the course being added.
     *
     * @param timeSlot The TimeSlot to assign to.
     * @param item The SlotItem being assigned.
     * @return False if TimeSlot has any assignments which are incompatible with item. True otherwise.
     */
    private boolean isFullyCompatible(TimeSlot timeSlot, SlotItem item) {
        HashSet<SlotItem> others = getAssignment(timeSlot);
        if (others == null) {
		return true;
	}
        for (SlotItem other : others) {
            if (item.incompatibleWith(other)) {
	    	System.out.println("found incompat: " + item + " with " + other);
                return false;
		}
        }

        return true;
    }

    /**
     * Verifies that a TimeSlot we are considering assigning a SlotItem to is not unwanted for the course we're trying
     * to assign.
     *
     * @param timeSlot The TimeSlot being assigned to.
     * @param item The SlotItem being assigned.
     * @return False if timeSlot is unwanted for item. True otherwise.
     */
    private boolean isNotUnwanted(TimeSlot timeSlot, SlotItem item) {
        return !(item.hasUnwanted(timeSlot));
    }

    /**
     * Verifies that an evening SlotItem (course or lecture section beginning with 9) is being assigned to an evening
     * TimeSlot (time >= 18:00).
     *
     * This returns false only if the SlotItem is an evening section, but the TimeSlot is not an evening slot.
     * This does NOT return false if the SlotItem is *not* an evening section, but the TimeSlot is an evening slot,
     * because that is not a requirement as far as I can see. If this is seen as undesirable, this can be changed
     * without much difficulty.
     *
     * @param timeSlot The TimeSlot being assigned to.
     * @param item The SlotItem being assigned.
     * @return False if item is an evening section and timeSlot is not an evening slot. True otherwise.
     */
    private boolean eveningClassInEveningSlot(TimeSlot timeSlot, SlotItem item) {
        return !(item.isEvening() && !timeSlot.isEveningSlot());
    }

    /**
     * Verifies that, if a SlotItem is a 500-level course, then it is not being assigned to a TimeSlot where we have
     * already assigned any other 500-level courses.
     *
     * @param timeSlot The TimeSlot to assign to.
     * @param item The SlotItem being assigned.
     * @return False if item is 500-level and other 500-level courses are assigned to timeSlot already. True otherwise.
     */
    private boolean noOther500Level(TimeSlot timeSlot, SlotItem item) {
        if (!item.is500Level()) return true;

        HashSet<SlotItem> others = getAssignment(timeSlot);
        if (others == null) return true;

        for (SlotItem other : others) {
            if (other.is500Level())
                return false;
        }

        return true;
    }

    /**
     * One hard constraint is that nothing can be scheduled on Tuesday between 11:00-12:30.
     * This method checks that constraint.
     *
     * @param timeSlot The TimeSlot being examined.
     * @return False if the TimeSlot is in the range of [11:00, 12:30) on Tuesday (lab or lecture). True otherwise.
     */
    private boolean outsideTuesdayDeadZone(TimeSlot timeSlot) {
        SlotType type = timeSlot.getSlotType();
        double time = timeSlot.getTime();
        return !((type == SlotType.TT_LAB || type == SlotType.TT_LEC)
                 && (time >= 11.0 && time < 12.5));
    }

    /**
     * Checks the special constraint for CPSC 813/913 w.r.t. 313/413. Ensures that if the slot is 813 or 913, then the
     * time is 18:00, and there are no overlaps with any section of 313 or 413, resp. Returns true if the course being
     * assessed is not one of {CPSC 313, CPSC 413, CPSC 813, CPSC 913}.
     *
     * Note that this method does not currently check for transitive incompatibility. For example, if given a 913 slot,
     * it will check for overlapping 413 sections (and return false if it finds them) but it will not search for
     * sections which are incompatible with 413.
     *
     * @param timeSlot The TimeSlot to possibly assign to.
     * @param item The SlotItem being assigned.
     * @return True if the condition is met, false otherwise.
     */
    private boolean satisfiesSpecialOverlap(TimeSlot timeSlot, SlotItem item) {
        if (!item.getCourseName().equals("CPSC")) return true;
        int courseNum = item.getCourseNum();

        int counterCourseNum;
        switch (courseNum) {
            case 313:
                counterCourseNum = 813;
                break;
            case 413:
                counterCourseNum = 913;
                break;
            case 813:
                if (timeSlot.getTime() != 18.0) return false;
                counterCourseNum = 313;
                break;
            case 913:
                if (timeSlot.getTime() != 18.0) return false;
                counterCourseNum = 413;
                break;
            default:
                return true;
        }

        HashSet<SlotItem> overlaps = getAssignedOverlapCourses(timeSlot);
        for (SlotItem other : overlaps) {
            if (other.getCourseNum() == counterCourseNum) return false;
        }

        return true;
    }

    // ------------- Soft Constraint (Eval) Assessors ------------

    /**
     * Evaluate the eval score after assigning a given SlotItem to a given timeSlot.
     * Essentially just calculates the change to the evalScore of the overall Assignments to determine what the new
     * evalScore would be if the given assignment is added.
     *
     * @param timeSlot The TimeSlot we may assign to.
     * @param item The item being assigned.
     * @return The soft constraint value for the given item assigned to the given TimeSlot for this Assignments instance.
     *         This is returned as an Evaluated object, which is done to allow sorting of a set of different evals.
     */
    public Evaluated eval(TimeSlot timeSlot, SlotItem item) {
        int val = evalScore;

        // Check for a change in courseMin or labMin. This will change only if the assignment will put the timeSlot
        // over its courseMin value, where it wasn't previously. The "default" state of evalScore is that all courses
        // and labs that have a minimum have not yet met that minimum, so any change to the evalScore here is purely
        // subtractive.
        int numAssigned = getNumAssigned(timeSlot);
        if (numAssigned < timeSlot.getMin()) {
            // Currently below min. Subtract penalty if adding one new item will put us over
            if (numAssigned + 1 >= timeSlot.getMin())
                val -= item.isLecture() ? (penalties.getCourseMin() * penalties.wMinFilled) : (penalties.getLabsMin() * penalties.wMinFilled);
        }

        // Check for a change in preferences. The only way the penalty imposed by assignments can go down is if we
        // removed Assignments. Since all we are going to do is add them, not remove them, the penalty can only increase
        // in this step, if it changes at all.
        evalScore += (item.getPreferencesForOtherSlots(timeSlot) * penalties.wPref);

        // Check for a change in pairs.
        if (item.hasPairs()) {
            for (SlotItem paired : item.getPairs()) {
                UnfilledPairs unfilledPairs = unfilledPairsMap.get(paired);
                if ((unfilledPairs != null) && (unfilledPairs.expectsPair(item))) {
                    if (!unfilledPairs.getTimeSlot().equals(timeSlot)) val += (penalties.getNotPaired() * penalties.wPair);
                }
            }
        }

        // Check for section collisions.
        if (numAssigned > 0) {
            // No section collisions if nothing is assigned here
            for (SlotItem assigned : getAssignment(timeSlot)) {
                if (((item.isLecture() && assigned.isLecture())
                        || (!item.isLecture() && !assigned.isLecture()))
                        && item.sameCourse(assigned)) {
                    val += (penalties.getSection() * penalties.wSecDiff);
                }
            }
        }

        return new Evaluated(timeSlot, val);
    }

    /**
     * UnfilledPairs nested class
     *
     * The purpose of this class is to wrap a set of expected (but not yet assigned) paired SlotItems for a given
     * SlotItem which *has* already been assigned. Every time we assign something new that has a pair, we check to see
     * if it has an unfilled pair waiting (i.e. if the other part of the pair has already been placed) and remove it
     * if so. In the process, we check whether the TimeSlot matches up, and if not, we add to the penalty.
     *
     * This is done so that we can efficiently manage eval scores for pairs without having to iterate through the
     * entire set of assignments every time we evaluate.
     */
    private class UnfilledPairs {
        private TimeSlot timeSlot;
        private HashSet<SlotItem> expectedPairs;

        UnfilledPairs(TimeSlot timeSlot) {
            this.timeSlot = timeSlot;
            expectedPairs = new HashSet<>();
        }

        void addPair(SlotItem pairedCourse) {
            expectedPairs.add(pairedCourse);
        }

        TimeSlot getTimeSlot() { return timeSlot; }

        boolean resolvePair(SlotItem added) {
            if (expectedPairs.contains(added)) {
                expectedPairs.remove(added);
                return true;
            }
            return false;
        }

        boolean expectsPair(SlotItem other) {
            return expectedPairs.contains(other);
        }
    }
        
    public String toString() 
    {
    	//System.out.print("\b");
    	String result = "";
    	
    	for (TimeSlot timeSlot : TimeTable.slots)
		{
 			result += timeSlot.toString() + "\t";
			
			if (assignments.containsKey(timeSlot))
			{
				for (SlotItem item : assignments.get(timeSlot))
				{
					result += item.toString() + "\t";
				}
			}
			result += "\n";
		}
    	
    	return result;
    }
}






















