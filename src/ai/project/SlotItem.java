package ai.project;

import java.util.*;

/**
 * SlotItem Class
 *
 * This is an abstract class, acting as the ancestor of the Course and Lecture classes.
 *
 * Contains shared functionality for those classes.
 */
public abstract class SlotItem {
    protected String courseName;
    protected int courseNum;
    protected int secNum;

    // Various structures to keep track of constraints for this item
    private HashSet<SlotItem> incompatible;
    private HashSet<TimeSlot> unwanted;
    private HashMap<TimeSlot, Integer> preferences;
    private HashSet<SlotItem> pairs;

    public SlotItem(String name, int courseNum, int secNum) {
        courseName = name;
        this.courseNum = courseNum;
        this.secNum = secNum;
        pairs = new HashSet<>();
        preferences = new HashMap<>();
        incompatible = new HashSet<>();
        unwanted = new HashSet<>();
    }

    /**
     * Add a new pair to the list of pairs. This will succeed unless the new item is identical to this one, or
     * the SlotItem is already in the list of pairs.
     *
     * This will also check if the new SlotItem also contains the pair, and will call addPair on it if not.
     *
     * @param pair The SlotItem to be considered as a pair to this SlotItem.
     */
    public void addPair(SlotItem other) {
        if (!this.equals(other)) {
            pairs.add(other);
            if (!other.isPair(this))
                other.addPair(this);
        }
    }

    /**
     * Add a new preference for a particular TimeSlot with a given value.
     *
     * As preferences are backed by a HashMap, if this SlotItem already has a preference for the given TimeSlot,
     * nothing will be added (the old one will be used instead).
     *
     * @param slot The TimeSlot to add a preference for.
     * @param pref The preference value for the preferred time slot.
     */
    public void addPreference(TimeSlot slot, int pref) {
       preferences.put(slot, pref);
    }

    /**
     * Add a new incompatibility between this TimeSlot and another. This will succeed unless the new item is
     * identical to this one (makes no sense) or the new SlotItem is already in the list of incompatibilities.
     *
     * This will also check if the new SlotItem also contains the incompatibility, and will call addIncompatibility
     * on it if not.
     *
     * @param other The SlotItem that this SlotItem is to be considered incompatible with.
     */
    public void addIncompatibility(SlotItem other) {
        if (!this.equals(other)) {
            incompatible.add(other);
            if (!other.incompatibleWith(this))
                other.addIncompatibility(this);
        }
    }

    /**
     * Adds a TimeSlot to the list of TimeSlots that are considered unwanted for this particular SlotItem. Will
     * succeed unless the new TimeSlot is already in the list of unwanted slots.
     *
     * @param slot The TimeSlot that is unwanted for this SlotItem.
     */
    public void addUnwanted(TimeSlot slot) {
        unwanted.add(slot);
    }


    // ------------ Predicates ------------

    /**
     * Predicate: is this SlotItem a pair with another SlotItem?
     *
     * @param other A SlotItem which may form a pair with this one.
     * @return True if this SlotItem is a pair with other, false otherwise.
     */
    public boolean isPair(SlotItem other) {
        return pairs.contains(other);
    }

    /**
     * Predicate: Does this SlotItem have any pair constraints?
     *
     * @return True if this SlotItem has pairs, false otherwise.
     */
    public boolean hasPairs() { return pairs.size() > 0; }

    /**
     * Predicate: is this SlotItem incompatible with another?
     *
     * @param other A SlotItem which may be incompatible with this one.
     * @return True if the other SlotItem is incompatible with this one, false otherwise.
     */
    public boolean incompatibleWith(SlotItem other) {
        return incompatible.contains(other);
    }

    /**
     * Predicate: is a given time slot unwanted for this SlotItem?
     *
     * @param time The TimeSlot that may be unwanted for this SlotItem.
     * @return True if the TimeSlot is unwanted for this SlotItem, false otherwise.
     */
    public boolean hasUnwanted(TimeSlot time) {
        return unwanted.contains(time);
    }

    /**
     * Predicate: does this SlotItem belong to the same overall course as some other slot item?
     *
     * @param other A SlotItem which may or may not belong to the same course as this one.
     * @return True if these are the same course (i.e. same course name and number). False otherwise.
     */
    public boolean sameCourse(SlotItem other) {
        return (courseNum == other.getCourseNum()
                && courseName.equals(other.getCourseName()));
    }

    /**
     * Predicate: is this an evening slot item? (I.e. does the section number begin with 9?)
     *
     * Note that this predicate assumes that the section number will have two digits.
     *
     * @return True if this is an evening slot, false otherwise.
     */
    public boolean isEvening() {
        return (secNum / 10) == 9;
    }

    /**
     * Predicate: is this a 500-level course? (I.e. course num is 5??)
     *
     * @return True if this is a 500-level course, false otherwise.
     */
    public boolean is500Level() { return (courseNum >= 500 && courseNum <= 600);}

    /**
     * Inherited predicate to obviate the need for reflexively checking the class of a lecture or lab.
     * @return True if this is a lecture, false if it is a lab.
     */
    public abstract boolean isLecture();


    // ------------ Getters ------------

    /**
     * Gets the name of the course.
     *
     * @return The name of hte course.
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * Gets the number of the course as an integer.
     *
     * @return The number of the course.
     */
    public int getCourseNum() {
        return courseNum;
    }

    /**
     * Gets an integer value representing this SlotItem's preference for a particular TimeSlot.
     *
     * @param time The TimeSlot for which we are retrieving a preference.
     * @return The preference value for that slot if this SlotItem has a preference for it, otherwise 0.
     */
    public int getPreferenceForSlot(TimeSlot time) {
        Integer temp = preferences.get(time);
        if (temp != null) return temp;
        return 0;
    }

    /**
     * Sums up the preference values for all slots other than the one given.
     *
     * @param time The TimeSlot to be excluded from the count of preference values.
     * @return The sum of all preference values for every time slot other than the one given.
     */
    public int getPreferencesForOtherSlots(TimeSlot time) {
        Integer prefForThisSlot = getPreferenceForSlot(time);
        Collection<Integer> allPrefs = preferences.values();
        int count = 0;
        for (Integer i : allPrefs) {
            count += i;
        }
        count -= prefForThisSlot;
        return count;
    }

    /**
     * Get the set of all paired courses.
     *
     * @return A HashSet containing all SlotItems with which this SlotItem is paired.
     */
    public HashSet<SlotItem> getPairs() {
        return new HashSet<>(pairs);
    }

    /**
     * Gets a string identifying this course.
     *
     * @return A string in the format of "NAME ###", where NAME is the course name, and ### is the course number.
     */
    public String getCourseString() {
        return courseName + " " + courseNum;
    }

    @Override
    public int hashCode() {
	int rv = courseNum * 10000;
	rv += secNum * 100;
	rv += courseName.hashCode();
	return rv;
    }

}
