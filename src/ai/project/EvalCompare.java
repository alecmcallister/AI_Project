package ai.project;

import java.util.Comparator;

/**
 * EvalCompare class
 *
 * A comparator which compares Evaluated instances by their eval score.
 */
public class EvalCompare implements Comparator<Evaluated> {
    @Override
    public int compare(Evaluated e1, Evaluated e2) {
        return (e1.getEvalWrapped().compareTo(e2.getEvalWrapped()));
    }
}
