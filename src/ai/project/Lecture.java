package ai.project;

/**
 * Lecture Class
 *
 * Represents a lecture type SlotItem, as opposed to a lab.
 *
 * Tracks a lecture number.
 */
public class Lecture extends SlotItem {
    private int lecNum;

    public Lecture(String courseName, int courseNum, int lecNum) {
        super(courseName, courseNum);
        this.lecNum = lecNum;
    }

    public boolean isLecture() { return true; }

    public int getLecNum() { return lecNum; }

    public String getIdString() { return "LEC " + lecNum;}


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
