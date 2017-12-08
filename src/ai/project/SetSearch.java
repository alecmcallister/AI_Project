package ai.project;

import java.util.ArrayList;
import java.util.Random;

public class SetSearch
{
	Department department;

	public SetSearch(Department department)
	{
		this.department = department;
	}

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

	public OTree DoTheSearchAlready(Assignments a, Assignments b)
	{
		ArrayList<SlotItem> evolutionList = new ArrayList<>();
		evolutionList.addAll(department.getAllCourses());

		if (department.getPartialAssignments() != null)
			evolutionList.removeAll(department.getPartialAssignments().getAllCourses());

		ArrayList<SlotItem> unassigned = new ArrayList<>();

		Assignments child = new Assignments(OTree.penalties, department.getTimeTable());

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
		childTree.genSolution(0);

		return childTree;
	}
}










