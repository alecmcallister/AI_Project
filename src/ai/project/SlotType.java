package ai.project;

/**
 * Simple enum for abstract slot types. This is in its own file, as opposed to belonging to some class, because a lot
 * of stuff uses this and it gets tedious to have to type out an extended name every time.
 */
public enum SlotType {
    MWF_LEC,
    TT_LEC,
    MW_LAB,
    TT_LAB,
    F_LAB
}
