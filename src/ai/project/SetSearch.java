package ai.project;

import java.util.ArrayList;
import java.util.Random;

/**
 * SetSearch class
 *
 * This class performs Set-based search logic.
 * Primarily, it handles the mutation component of the search.
 */
public class SetSearch
{
	Department department;

	public SetSearch(Department department)
	{
		this.department = department;
	}

    /**
     * Given two parent sets of Assignments, select a parent to descend from.
     * Tends towards picking the parent with the better eval score, but may not necessarily -- the
     * choice is randomized.
     *
     * @param a One set of Assignments.
     * @param b The other set of Assignments.
     * @return Whichever of A or B has a lower eval score.
     */
	public Assignments ChooseParent(Assignments a, Assignments b)
	{
		int aEval = a.getEvalScore();
		int bEval = b.getEvalScore();

		if (aEval <= 0 || bEval <= 0)
		{
			aEval = 10;
			bEval = 10;
		}

		int rand = new Random().nextInt(aEval + bEval);

		if (aEval > bEval)
		{
			if (rand < aEval)
				return b;

			return a;
		}
		else
		{
			if (rand < bEval)
				return a;

			return b;
		}
	}

    /**
     * Perform the set-based search. Takes two sets of Assignments, which it may hybridize to
     * create a new Assignments. On this new child, an Or-Tree is then run to generate a solution.
     *
     * @param a One Assignments parent.
     * @param b The other Assignments parent.
     * @return An Or-Tree, representing a solution that may descend from Assignments a and b.
     */
	public OTree DoTheSearchAlready(Assignments a, Assignments b)
	{
		ArrayList<SlotItem> evolutionList = new ArrayList<>();
		evolutionList.addAll(department.getAllCourses());

        Assignments child;
		if (department.getPartialAssignments() != null) {
            evolutionList.removeAll(department.getPartialAssignments().getAllCourses());
            child = new Assignments(department.getPartialAssignments());
        }
        else {
            child = new Assignments(department.getTimeTable());
        }

		ArrayList<SlotItem> unassigned = new ArrayList<>();



		while (!evolutionList.isEmpty())
		{
			SlotItem randomItem = evolutionList.remove(new Random().nextInt(evolutionList.size()));

			Assignments firstChoice = ChooseParent(a, b);
			Assignments secondChoice = (firstChoice == a) ? b : a;

			TimeSlot slotA = firstChoice.getTimeSlot(randomItem);
			TimeSlot slotB = secondChoice.getTimeSlot(randomItem);

			ArrayList<Evaluated> result = child.getViableTimeSlots(department.getTimeTable(), randomItem);

			boolean assigned = false;

			for (Evaluated evaluated : result)
			{
				if (evaluated.getTimeSlot().equals(slotA))
				{
					child.addAssignment(slotA, randomItem);
					assigned = true;
					break;
				}
				else if (evaluated.getTimeSlot().equals(slotB))
				{
					child.addAssignment(slotB, randomItem);
					assigned = true;
					break;
				}
			}
			if (!assigned)
			{
				unassigned.add(randomItem);
			}
		}

		OTree childTree = new OTree(department, child, unassigned);
		childTree.genSolution();

		return childTree;
	}
}










