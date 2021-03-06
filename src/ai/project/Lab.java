package ai.project;

/**
 * Lab Class
 *
 * Represents a lab type SlotItem, as opposed to a lecture.
 *
 * Tracks a tutorial number and, optionally, a parent lecture.
 */
public class Lab extends SlotItem {
    private Lecture parent;

    /**
     * Parentless constructor.
     *
     * @param courseName The name of the course this tutorial belongs to.
     * @param courseNum The number of the course this tutorial belongs to.
     * @param labNum The number of the lab.
     */
    public Lab(String courseName, int courseNum, int labNum) {
        super(courseName, courseNum, labNum);
    }

    /**
     * Parented constructor. Will only set the parent if the parent belongs to the same course as the lab.
     *
     * @param courseName The name of the course this tutorial belongs to.
     * @param courseNum The number of the course this tutorial belongs to.
     * @param labNum The number of the lab.
     * @param parent The parent Lecture of this Lab.
     */
    public Lab(String courseName, int courseNum, int labNum, Lecture parent) {
        this(courseName, courseNum, labNum);
        if (sameCourse(parent))
            this.parent = parent;
    }


    // ----------- Getters -----------

    /**
     * Retrieves the parent lecture of this Lab (i.e. the specific lecture section with which this lab is associated),
     * if one exists.
     *
     * @return The parent lecture of this Lab if it has one, or null otherwise.
     */
    public Lecture getParent() {
        return parent;
    }

    /**
     * Returns the section number (TUT #) of this lab.
     *
     * @return The TUT # of this lab.
     */
    public int getLabNum() {
        return secNum;
    }


    // ----------- Predicates -----------

    /**
     * Predicate: does this Lab belong to a specific lecture section?
     *
     * @return True if this Lab has a parent lecture. False otherwise.
     */
    public boolean hasParent() {
        return (parent != null);
    }

    /**
     * Polymorphic method for easy distinction between lectures and labs through the superclass.
     *
     * @return False, categorically.
     */
    public boolean isLecture() { return false; }


    // ----------- Overrides -----------

    @Override
    public String toString() {
        String rv = getCourseString() + " ";
        if (hasParent())
            rv += parent.getIdString() + " ";
        rv += "TUT " + secNum;
        return rv;
    }

    @Override
    public boolean equals(Object other) {
        // Self comparison
        if (this == other) return true;

        // Other Lab comparison. Compare by lab numbers iff they belong to the same course.
        // Will return false if parents lectures are mismatched or unequal.
        if (other instanceof Lab) {
            if (sameCourse((Lab)other)) {
                Lab labOther = (Lab) other;
                if ((hasParent() && !labOther.hasParent())
                        || (!hasParent() && labOther.hasParent()))
                    return false;
                else if (hasParent() && labOther.hasParent()
                        && !(parent.equals(labOther.getParent())))
                    return false;

                return (this.getLabNum() == (labOther.getLabNum()));
            }
        }

        // Other is not a Lecture
        return false;
    }
}
