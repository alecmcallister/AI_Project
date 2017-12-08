package ai.project;

import java.util.*;

/**
 * TimeTable Class
 *
 * Organizes TimeSlots. This is intended to be used as a reference for all time slots available to a particular
 * department. The general use case is to create TimeSlots for every time slot read in by the parser, and put
 * them into this class via calls to updateTable(). Individual slots can then be queried, and the class can return
 * ArrayLists of all slots of either type (or both).
 *
 * The TimeTable class also keeps track of the total number of lectures and labs that have a minimum value
 * (i.e. min > 0). This is to improve efficiency of eval calculations for assignments down the line, because it allows
 * us to get a "baseline" penalty -- that is, the penalty for not meeting the minimum number of courses for any
 * TimeSlot, which is what we will have if no courses are assigned yet.
 *
 * This class works by organizing slots into two HashMaps, divided into Lecture and Lab slots.
 */
public class TimeTable {
    private HashMap<TimePair, TimeSlot> lecSlots;
    private HashMap<TimePair, TimeSlot> labSlots;
    private int totalLecturesWithMinimum;
    private int totalLabsWithMinimum;

    public TimeTable() {
        lecSlots = new HashMap<>();
        labSlots = new HashMap<>();
        totalLecturesWithMinimum = 0;
        totalLabsWithMinimum = 0;
    }

    /**
     * Update a single entry in the table with a new slot. The new entry will replace the old one if there is one,
     * or else will add a new entry to the table.
     *
     * @param slot The new TimeSlot to be inserted into the table.
     */
    public void updateTable(TimeSlot slot) 
    {
        if (slot.isLectureSlot()) {
            TimeSlot oldSlot = lecSlots.get(slot.getTimePair());
            if (oldSlot != null) {
                if (oldSlot.getMin() > 0) totalLecturesWithMinimum--;
                lecSlots.replace(slot.getTimePair(), slot);
            }
            else {
                lecSlots.put(slot.getTimePair(), slot);
            }
            if (slot.getMin() > 0) totalLecturesWithMinimum++;
        }
        else {
            TimeSlot oldSlot = labSlots.get(slot.getTimePair());
            if (oldSlot != null) {
                if (oldSlot.getMin() > 0) totalLabsWithMinimum--;
                labSlots.replace(slot.getTimePair(), slot);
            }
            else {
                labSlots.put(slot.getTimePair(), slot);
            }
            if (slot.getMin() > 0) totalLabsWithMinimum++;
        }
    }

    /**
     * Get the TimeSlot for a given time, if one exists. May return null if no slot exists for the specified time.
     *
     * @param time The TimePair for which to look up a slot.
     * @return The TimeSlot for the TimePair given, or null if it does not exist in the TimeTable.
     */
    public TimeSlot getSlot(TimePair time) {
        if (time.isLecture()) {
            return lecSlots.get(time);
        }
        else {
            return labSlots.get(time);
        }
    }

    /**
     * Get the TimeSlot for a given time, if one exists. TimeSlot overload that uses the internal TimePair of another
     * TimeSlot to perform the lookup.
     *
     * @param timeSlot Another TimeSlot. The underlying TimePair will be used to do the lookup.
     * @return The TimeSlot for the TimePair given, or null if it does not exist in the TimeTable.
     */
    public TimeSlot getSlot(TimeSlot timeSlot) {
        return getSlot(timeSlot.getTimePair());
    }

    /**
     * Get the TimeSlot for a given time, if one exists. Overload which constructs a TimePair.
     *
     * @param type Abstract slot type being looked up.
     * @param time Time being looked up, as a double representing start hour.
     * @return The TimeSlot for the TimePair given, or null if it does not exist in the TimeTable.
     */
    public TimeSlot getSlot(SlotType type, double time) {
        TimePair temp = new TimePair(type, time);
        return getSlot(temp);
    }

    /**
     * Get the TimeSlot for a given time, if it exists. Overload which does not require a SlotType.
     *
     * @param day A string for the day; one of "MO", "TU", or "FR".
     * @param time The time as a double (HH.MM).
     * @param isLab Is this a slot for a lab? True if this is a lab slot, false otherwise.
     * @return The TimeSlot, if it exists. Null if it is not in the TimeTable.
     */
    public TimeSlot getSlot(String day, String time, boolean isLab) {
        return getSlot(new TimePair(day, time, isLab));
    }

    /**
     * Returns an unordered ArrayList containing all time slots in the table.
     *
     * This is a copy of the values sets of the underlying hash maps. This is done so that references to TimeSlots are
     * the same as in the maps, but altering the returned ArrayList is not going to affect the TimeTable.
     *
     * Note that this merges two Collections, so where possible, it is more efficient to call either getLabSlots() or
     * getLectureSlots().
     *
     * @return An unsorted ArrayList, containing all TimeSlots in the TimeTable.
     */
    public ArrayList<TimeSlot> getAllSlots() {
        ArrayList<TimeSlot> slots = getAllLabSlots();
        slots.addAll(getAllLectureSlots());
        return slots;
    }

    /**
     * Returns an unordered ArrayList containing all lab slots in the table.
     *
     * @return An unsorted ArrayList, containing all lab TimeSlots in the TimeTable.
     */
    public ArrayList<TimeSlot> getAllLabSlots() {
        return new ArrayList<>(labSlots.values());
    }

    /**
     * Returns an unordered ArrayList containing all lecture slots in the table.
     *
     * @return An unsorted ArrayList, containing all lecture TimeSlots in the TimeTable.
     */
    public ArrayList<TimeSlot> getAllLectureSlots() {
        return new ArrayList<>(lecSlots.values());
    }

    /**
     * Computes the total number of lecture slots in the TimeTable that have a minimum value.
     * Used to establish a baseline penalty for a fresh set of Assignments (i.e. what is the eval score if
     * we literally assign nothing?).
     *
     * @return The total number of lecture slots in the TimeTable with a minimum value.
     */
    public int getTotalLecturesWithMinimum() {
        return totalLecturesWithMinimum;
    }

    /**
     * Computes the total number of lab slots in the TimeTable that have a minimum value.
     * Used to establish a baseline penalty for a fresh set of Assignments (i.e. what is the eval score if
     * we literally assign nothing?).
     *
     * @return The total number of lab slots in the TimeTable with a minimum value.
     */
    public int getTotalLabsWithMinimum() {
        return totalLabsWithMinimum;
    }
}
