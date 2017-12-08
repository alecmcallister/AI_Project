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
 *  We also track weights:
 *  5. wMinFilled: The weight applied to the penalty for lectures or labs that do not meet the minimum value.
 *  6. wPref: The weight applied to the penalty for courses in the wrong place, in terms of preferences.
 *  7. wPair: The weight applied to the penalty for courses that are paired, yet still scheduled together.
 *  8. wSecDiff: The weight applied to the penalty for different sections of the same course scheduled together.
 *
 *  The purpose of the Penalties class is to simplify the passing of these penalty values throughout the application.
 */
public class Penalties {
    private int courseMin;
    private int labsMin;
    private int notPaired;
    private int section;

    public int wMinFilled;
    public int wPref;
    public int wPair;
    public int wSecDiff;


    public Penalties(int courseMin, int labsMin, int notPaired, int section,
    			int wMinFilled, int wPref, int wPair, int wSecDiff) {
        this.courseMin = courseMin;
        this.labsMin = labsMin;
        this.notPaired = notPaired;
        this.section = section;
        this.wMinFilled = wMinFilled;
        this.wPref = wPref;
        this.wPair = wPair;
        this.wSecDiff = wSecDiff;
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
