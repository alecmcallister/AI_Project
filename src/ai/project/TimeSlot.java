package ai.project;

/**
 * TimeSlot Class
 *
 * Basically a wrapper around a TimePair that includes min/max values.
 * This is a separate class to create a clearer distinction between the Key (TimePair) and Value (TimeSlot) of
 * the TimeTable class' underlying map.
 */
public class TimeSlot {
    private int max;
    private int min;

    private TimePair timePair;

    TimeSlot(TimePair timePair) {
        this(0, 0, timePair);
    }

    TimeSlot(SlotType type, double time) {
        this(0, 0, new TimePair(type, time));
    }

    /**
     * Min/max constructor.
     */
    TimeSlot(int max, int min, SlotType type, double time) {
        this(max, min, new TimePair(type, time));
    }

    TimeSlot(int max, int min, TimePair timePair) {
        this.timePair = timePair;
        this.max = max;
        this.min = min;
    }

    public TimePair getTimePair() { return this.timePair; }

    public int getMin() { return this.min; }
    public int getMax() { return this.max; }

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
            return (this.timePair == ((TimeSlot)other).getTimePair());
        }

        // Other is not a TimeSlot
        return false;
    }

}
