package ai.project;

/**
 * SlotItem Class
 *
 * This is an abstract class, acting as the ancestor of the Course and Lecture classes.
 *
 * Contains shared functionality for those classes.
 */
public abstract class SlotItem {
    String name;
    int num;

    @Override
    public int hashCode() {
        int rv = name.hashCode() * 1000000;
        rv += num * 1000;
        return rv;
    }
}