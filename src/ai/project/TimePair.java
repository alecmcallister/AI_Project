package ai.project;

/**
 * TimePair Class
 *
 * Represents a pair of slot type and time, i.e. a day and a time on that day.
 *
 * Another way of thinking of this class is as an "abstract slot," as described in the assignment specification.
 * The purpose of this class is to represent some specific combination of day/start time, irrespective of min/max vals,
 * assignments, etc.
 *
 * Supports a variety of different construction options, including one that uses strings matching what we
 * expect to see in the input files.
 */
public class TimePair {
    private SlotType type;
    // Time is represented by a double because Java's Date class includes much more than we need and will complicate
    // usage considerably.
    // This is the hour of the day the time slot begins at (24 hour clock). May be X.5 to represent an X:30 start time.
    private double time;

    /**
     * Constructor to build a TimePair from a partially-parsed input string.
     *
     * @param dayString Day string. Expect one of MO, TU, or FR.
     * @param timeString Time string. Expected format H:MM or HH:MM.
     * @param isLab Is this a lab slot? (If false, this is a lecture slot).
     */
    TimePair(String dayString, String timeString, boolean isLab) {
        // Take out any leading or trailing white space on day string
        String trimDay = dayString.trim();
        if (trimDay.equalsIgnoreCase("mo")) {
            if (isLab) type = SlotType.MW_LAB;
            else type = SlotType.MWF_LEC;
        }
        else if (trimDay.equalsIgnoreCase("tu")) {
            if (isLab) type = SlotType.TT_LAB;
            else type = SlotType.TT_LEC;
        }
        else{
            // we've exhausted everything else -- default to Friday lab
            type = SlotType.F_LAB;
        }

        // Assume well-formatted time string (which may have leading or trailing white space)
        String trimTime = timeString.trim();
        String[] timeSegs = trimTime.split(":");
        int hour = Integer.parseInt(timeSegs[0]);
        int mins = Integer.parseInt(timeSegs[1]);

        double newTime = (double) hour;
        newTime += (mins / 100);
        this.time = boundTime(newTime);
    }

    /**
     * Simple constructor. Takes a SlotType and a start time as a double, representing hour.
     * The provided time will be rounded to the nearest half hour (0.5 in the double) as needed.
     * Times that are out-of-bounds (less than 0 or > 23.5) will also be rounded.
     *
     * @param type The SlotType we want to construct a TimePair for.
     * @param time The time the time pair is meant to represent, represented as a number of hours on a 24-hour clock.
     */
    TimePair(SlotType type, double time) {
        this.type = type;
        this.time = boundTime(time);
    }

    /**
     * Puts a given time double within sensible bounds.
     *
     * @param inTime A time value that may not necessarily be within bounds or rounded to the nearest 0.5.
     * @return A time value that is rounded and within boudns.
     */
    private double boundTime(double inTime) {
        // Range check on time val
        if (inTime > 23.5 || inTime < 0.0) {
            inTime = 0.0;
        }
        else {
            // Round time to nearest 0.5
            inTime = Math.round(inTime * 2) / 2.0;
        }
        return inTime;
    }

    /**
     * Gets the abstract slot type (e.g. Mo/We/Fr Lecture, Tu/Th Lab, etc.) of this TimePair.
     *
     * @return The abstract slot type of this TimePair.
     */
    public SlotType getType() { return this.type; }

    /**
     * Gets the time represented by this TimePair as a double. This double has the format
     *  H.M
     * where H is the number of hours on a 24-hour clock, and M is the number of minutes (should be either .5,
     * representing :30, or .0, representing :00, unless something has gone wrong).
     *
     * @return The time of this TimeSlot as a double.
     */
    public double getTime() { return this.time; }

    /**
     *Predicate: Does this time pair belong to a lecture?
     *
     * @return True if this is a lecture time (MWF lecture or TuTh lecture). False if it's a tutorial time.
     */
    public boolean isLecture() {
        return ((type == SlotType.MWF_LEC) || (type == SlotType.TT_LEC));
    }

    // --------------- Overrides -----------------

    @Override
    public String toString() {
        String rv = "";
        switch (type) {
            case MWF_LEC:
                rv = "MWF Lec ";
                break;
            case TT_LEC:
                rv = "TuTh Lec ";
                break;
            case MW_LAB:
                rv = "MW Lab ";
                break;
            case TT_LAB:
                rv = "TuTh Lab ";
                break;
            case F_LAB:
                rv = "Fri Lab ";
                break;
            default:
                break;
        }

        Double temp = Math.floor(time);
        rv += temp.intValue() + ":";
        rv += (time > temp) ? "30" : "00";

        return rv;
    }

    @Override
    public int hashCode() {
        int rv = type.ordinal() * 1000;
        rv += time * 2;
        return rv;
    }

    @Override
    public boolean equals(Object other) {
        // Self comparison
        if (this == other) return true;

        // Other TimePair comparison
        if (other instanceof TimePair) {
            SlotType otherType = ((TimePair)other).getType();
            double otherTime = ((TimePair)other).getTime();
            return ((this.type == otherType) && (this.time == otherTime));
        }

        // Other is not a TimePair
        return false;
    }
}
