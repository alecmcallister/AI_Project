package ai.project;

/**
 * Simple enum for abstract slot types. This is in its own file, as opposed to belonging to some class, because a lot
 * of other classes use this, and it's better for this enum to be central w.r.t. all of them.
 *
 * This enum represents the different possibilities for abstract slots as given in lecture:
 *  - Monday/Wednesday/Friday lectures
 *  - Tuesday/Thursday lectures
 *  - Monday/Wednesday labs
 *  - Tuesday/Thursday labs
 *  - Friday labs
 */
public enum SlotType {
    MWF_LEC,
    TT_LEC,
    MW_LAB,
    TT_LAB,
    F_LAB
}
