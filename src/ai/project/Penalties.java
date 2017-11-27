package ai.project;

/**
 * Penalties Class
 *
 * This is a basic container for the four types of fixed penalty values in the assignment specification:
 *  1. pen_coursemin: Penalty for not meeting the minimum number of courses for a given lecture section.
 *  2. pen_labsmin: Penalty for not meeting the minimum number of labs for a given lab section.
 *  3. pen_notpaired: Penalty for not scheduling Pairs together.
 *  4. pen_section: Penalty for scheduling different course sections at the same time.
 *
 *  The fifth type of penalty (for ignoring preferences) is variable, and so is not tracked by this class.
 *
 *  The purpose of the Penalties class is to simplify the passing of these penalty values throughout the application.
 */
public class Penalties {
    private int courseMin;
    private int labsMin;
    private int notPaired;
    private int section;

    public Penalties(int courseMin, int labsMin, int notPaired, int section) {
        this.courseMin = courseMin;
        this.labsMin = labsMin;
        this.notPaired = notPaired;
        this.section = section;
    }

    public int getCourseMin() {
        return courseMin;
    }

    public int getLabsMin() {
        return labsMin;
    }

    public int getNotPaired() {
        return notPaired;
    }

    public int getSection() {
        return section;
    }
}
