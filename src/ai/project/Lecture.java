package ai.project;

/**
 * Lecture Class
 *
 * Represents a lecture type SlotItem, as opposed to a lab.
 *
 * Tracks a lecture number.
 */
public class Lecture extends SlotItem {

    /**
     * Sole constructor. Just a wrapper for the super constructor.
     *
     * @param courseName The name of the course (e.g. "CPSC", "SENG")
     * @param courseNum The number of the course (e.g. 411, 201)
     * @param lecNum The section number of the lecture (e.g. 1, 99).
     */
    public Lecture(String courseName, int courseNum, int lecNum) {
        super(courseName, courseNum, lecNum);
    }

    /**
     * Polymorphic method for easy distinction between lectures and labs through the superclass.
     *
     * @return True, categorically.
     */
    public boolean isLecture() { return true; }

    /**
     * Returns the section number (LEC #) of this lecture.
     *
     * @return The LEC # of this lab.
     */
    public int getLecNum() { return secNum; }

    /**
     * Retrieves a string which identifies the lecture section of this lecture.
     *
     * @return A string in the format of "LEC %s", where %s is the section number of the lecture.
     */
    public String getIdString() { return "LEC " + secNum;}


    // --------------- Overrides ---------------

    @Override
    public String toString() {
        String rv = getCourseString() + " ";
        rv += getIdString();
        return rv;
    }

    @Override
    public boolean equals(Object other) {
        // Self comparison
        if (this == other) return true;

        // Other Lecture comparison. Compare by lecture numbers iff they belong to the same course.
        if (other instanceof Lecture) {
            if (sameCourse((Lecture)other))
                return (this.getLecNum() == ((Lecture) other).getLecNum());
        }

        // Other is not a Lecture
        return false;
    }
}
