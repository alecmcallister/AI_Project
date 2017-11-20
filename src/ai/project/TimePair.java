package ai.project;

/**
 * This is a struct-like class to represent a pair of a slot type and time.
 */
public class TimePair {
    private SlotType type;
    // Time is represented by a double because Java's Date class includes much more than we need.
    // This is the hour of the day the time slot begins at (24 hour clock). May be X.5 to represent
    // an X:30 start time.
    private double time;


    TimePair(SlotType type, double time) {
        this.type = type;

        // Range check on time val
        if (time > 23.5 || time < 0.0) this.time = 0.0;
        else {
            // Round time to nearest 0.5
            this.time = Math.round(time * 2) / 2.0;
        }
    }

    public SlotType getType() { return this.type; }

    public double getTime() { return this.time; }

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
