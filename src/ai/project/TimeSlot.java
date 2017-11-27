package ai.project;

/**
 * TimeSlot Class
 *
 * Basically a fancy wrapper for a TimePair, that also provides:
 *  - min and max values (needed for soft and hard constraints, resp.)
 *  - Determining overlap between slots
 *  - Some convenience functions for comparisons, etc.
 *
 * This class is separate from a TimePair because it can be more efficient for some applications to just
 * instantiate a TimePair without having to build the whole wrapper as well. Additionally, the separation
 * permits using TimePairs as keys to a map of TimeSlots, as is used by the TimeTable class.
 *
 * For most applications, however, TimeSlots should be preferred over TimePairs. The equals() operator has been
 * overloaded such that TimeSlots with the same day/time (i.e. same TimePair) are considered equal.
 */
public class TimeSlot {
    private int max;
    private int min;

    private TimePair timePair;

    /**
     * Constructor to build a TimeSlot from a partially-parsed input string.
     *
     * @param day Day string. Expect one of MO, TU, or FR.
     * @param time Time string. Expected format H:MM or HH:MM.
     * @param max Maximum value for this slot.
     * @param min Minimum value for this slot.
     * @param isLab Is this a lab slot? (If false, this is a lecture slot).
     */
    TimeSlot(String day, String time, int max, int min, boolean isLab) {
        timePair = new TimePair(day, time, isLab);
        this.max = max;
        this.min = min;
    }

    /**
     * Constructor which uses an existing TimePair and 0 min/max and a given type.
     *
     * @param timePair An already-instantiated TimePair object.
     */
    TimeSlot(TimePair timePair) {
        this(0, 0, timePair);
    }

    /**
     * Constructor to build a time slot with 0 min/max and a given slot type.
     *
     * @param type The type of slot to use.
     * @param time The time (in format of a double with H.MM; minute values will be rounded to nearest 0.5 / 0:30)
     */
    TimeSlot(SlotType type, double time) {
        this(0, 0, new TimePair(type, time));
    }

    /**
     * Min/max constructor.
     *
     * @param max The maximum value for this slot.
     * @param min The minimum value for this slot.
     * @param type The type of slot to use.
     * @param time The time (in format of a double with H.MM; minute values will be rounded to nearest 0.5 / 0:30)
     */
    TimeSlot(int max, int min, SlotType type, double time) {
        this(max, min, new TimePair(type, time));
    }

    /**
     * Min/max constructor which uses an existing TimePair.
     *
     * @param max The maximum value for this slot.
     * @param min The minimum value for this slot.
     * @param timePair An already-instantiated TimePair object.
     */
    TimeSlot(int max, int min, TimePair timePair) {
        this.timePair = timePair;
        this.max = max;
        this.min = min;
    }

    // -------------- Getters --------------

    /**
     * Gets the TimePair for this slot, which is a pair of SlotType (i.e. abstract slot) and time (in hours).
     *
     * @return The underlying TimePair for this time slot.
     */
    public TimePair getTimePair() { return this.timePair; }

    /**
     * Gets the start time for this slot, as a double of hours.
     *
     * @return The start time for this time slot.
     */
    public double getTime() { return timePair.getTime(); }

    /**
     * Gets the SlotType for this slot, i.e. the abstract slot that it occupies.
     *
     * @return The slot type for this slot.
     */
    public SlotType getSlotType() { return timePair.getType(); }


    /**
     * Gets the minimum number of courses for this slot (soft constraint).
     *
     * @return The ideal minimum number of courses to be assigned to this slot.
     */
    public int getMin() { return this.min; }

    /**
     * Gets the maximum number of courses for this slot (hard constraint).
     *
     * A value of 0 indicates that this slot does not exist in the problem instance.
     *
     * @return The maximum number of courses that can be assigned to this slot.
     */
    public int getMax() { return this.max; }

    /**
     * Returns the duration of the slot, in hours. Value depends on the type of slot.
     *
     * @return The duration of the slot, in hours.
     */
    public double getLength() {
        double rv = 1.0;
        SlotType type = timePair.getType();
        if (type == SlotType.MW_LAB || type == SlotType.TT_LAB)
            rv += 0.5;
        else if (type == SlotType.F_LAB)
            rv += 1.0;
        return rv;
    }

    // -------------- Predicates --------------

    /**
     * Predicate to determine whether this is a lecture slot.
     *
     * @return True if this is a lecture slot, false otherwise.
     */
    public boolean isLectureSlot() {
        return timePair.isLecture();
    }

    /**
     * Predicate to determine whether this is a lab slot.
     *
     * @return True if this is a lab slot, false otherwise.
     */
    public boolean isLabSlot() {
        return !timePair.isLecture();
    }

    /**
     * Predicate to determine whether this is an evening slot (time >= 18:00).
     *
     * @return True if this is an evening slot, false otherwise.
     */
    public boolean isEveningSlot() { return timePair.getTime() >= 18.0; }

    /**
     * Predicate to determine if two TimeSlots occur on the same day (e.g. MWF_LEC and F_LAB).
     *
     * @param other The TimeSlot that may share a day with this one.
     * @return True if both TimeSlots can occur on the same day, or false otherwise.
     */
    public boolean overlapsDay(TimeSlot other) {
        SlotType type = getSlotType();
        SlotType otherType = other.getSlotType();
        if (type == SlotType.MWF_LEC) {
            return (otherType == SlotType.MWF_LEC
                    || otherType == SlotType.MW_LAB
                    || otherType == SlotType.F_LAB);
        }
        else if (type == SlotType.F_LAB) {
            return (otherType == SlotType.F_LAB
                    || otherType == SlotType.MWF_LEC);
        }
        else if (type == SlotType.MW_LAB) {
            return (otherType == SlotType.MW_LAB
                    || otherType == SlotType.MWF_LEC);
        }
        else {
            // TuTh lab or lecture
            return (otherType == SlotType.TT_LEC
                    || otherType == SlotType.TT_LAB);
        }
    }

    /**
     * Predicate to determine whether the TimeSlot overlaps with another TimeSlot.
     *
     * @param other The other TimeSlot to compare against.
     * @return True if the TimeSlots overlap; false otherwise.
     */
    public boolean overlaps(TimeSlot other) {
        if (!(overlapsDay(other))) return false;
        double start = timePair.getTime();
        double otherStart = other.getTime();
        double end = start + getLength();
        double otherEnd = otherStart + other.getLength();

        // Find which one starts earlier and iterate until we've run past either's max time.
        double lowerStart = Math.min(start, otherStart);
        for (double i = lowerStart; ((i < end) && (i < otherEnd)); i += 0.5 ) {
            if (i >= start && i >= otherStart)
                return true;
        }

        return false;
    }

    // -------------- Overrides --------------

    @Override
    public String toString() {
        String rv = String.format("%15s", timePair.toString());
        rv += " | Max = " + max;
        rv += " ; Min = " + min;
        return rv;
    }

    @Override
    public int hashCode() {
        return timePair.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        // Self comparison
        if (this == other) return true;

        // Other TimeSlot comparison. Just check if the time pair is the same.
        if (other instanceof TimeSlot) {
            return (this.timePair.equals(((TimeSlot)other).getTimePair()));
        }

        // Other is not a TimeSlot
        return false;
    }

}
