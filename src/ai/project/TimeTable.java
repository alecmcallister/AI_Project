package ai.project;

import java.util.*;

/**
 * TimeTable Class
 *
 * Organizes TimeSlots.
 */
public class TimeTable {
    private HashMap<TimePair, TimeSlot> timeSlots;

    public TimeTable() {
        timeSlots = new HashMap<>();
        initializeTable();
    }

    /**
     * Update a single entry in the table with a new slot. The new entry will replace the old one.
     *
     * @param slot The new TimeSlot to be inserted into the table.
     */
    public void updateTable(TimeSlot slot) {
        timeSlots.put(timeSlots.get(slot).getTimePair(), slot);
    }

    /**
     * Returns a Collection containing all time slots in the table (unordered).
     *
     * This is a copy of the values set of the underlying hash map. This is done so that references to TimeSlots are
     * the same as in the map, but altering the returned ArrayList is not going to affect the TimeTable.
     * @return
     */
    public ArrayList<TimeSlot> getAllSlots() {
        return new ArrayList<TimeSlot>(timeSlots.values());
    }

    /**
     * Initializes the time table with all slots given in the assignment spec.
     * All entries are initialized with min/max both at 0.
     */
    private void initializeTable() {
        // MWF lectures, MW labs, and TuTh labs
        for (double i = 8.0; i <= 20.0; i += 1.0) {
            TimePair lecPair = new TimePair(SlotType.MWF_LEC, i);
            TimePair mwLabPair = new TimePair(SlotType.MW_LAB, i);
            TimePair ttLabPair = new TimePair(SlotType.TT_LAB, i);

            timeSlots.put(lecPair, new TimeSlot(lecPair));
            timeSlots.put(mwLabPair, new TimeSlot(mwLabPair));
            timeSlots.put(ttLabPair, new TimeSlot(ttLabPair));
        }

        // TuTh lectures
        for (double i = 8.0; i <= 18.5; i+= 1.5) {
            TimePair lecPair = new TimePair(SlotType.TT_LEC, i);
            timeSlots.put(lecPair, new TimeSlot(lecPair));
        }

        // Friday labs
        for (double i = 8.0; i <= 18.0; i += 2.0) {
            TimePair labPair = new TimePair(SlotType.F_LAB, i);
            timeSlots.put(labPair, new TimeSlot(labPair));
        }
    }

    /**
     * Debug method. Prints out a (pretty much unordered) list of every slot in the time table.
     */
    private void debugPrintSlots() {
        Iterator it = timeSlots.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey());
            it.remove();
        }
    }
}
