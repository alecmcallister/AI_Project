package ai.project;

import java.util.ArrayList;

/**
 * SlotItem Class
 *
 * This is an abstract class, acting as the ancestor of the Course and Lecture classes.
 *
 * Contains shared functionality for those classes.
 */
public abstract class SlotItem {
    String courseName;
    int courseNum;

    // Various structures to keep track of constraints for this item
    private ArrayList<SlotItem> pairs;
    private ArrayList<Preference> preferences;
    private ArrayList<SlotItem> incompatible;
    private ArrayList<TimeSlot> unwanted;

    /** Simple struct-class for tracking preferences
     */
    private class Preference {
        private TimeSlot slot;
        private int preferenceValue;
    }
}