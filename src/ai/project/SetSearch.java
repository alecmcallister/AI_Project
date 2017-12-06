package ai.project;

import java.util.ArrayList;
import java.util.Random;

public class SetSearch
{
	ArrayList<Assignments> F;
	Department department;
	
	public static ArrayList<Assignments> generated = new ArrayList<>();
	
	public SetSearch(Department department)
	{
		this.department = department;
	}
	
	public Assignments DoTheSearchAlready(Assignments a, Assignments b)
	{		
		ArrayList<SlotItem> evolutionList =  new ArrayList<>();
		evolutionList.addAll(department.getAllCourses());
		
		ArrayList<SlotItem> unassigned = new ArrayList<>();
		
		Assignments child = new Assignments(new Penalties(0, 0, 0, 0), department.getTimeTable());
		
		while (!evolutionList.isEmpty())
		{
			SlotItem randomItem = evolutionList.remove(new Random().nextInt(evolutionList.size()));			
			
			TimeSlot slotA = a.getTimeSlot(randomItem);
			TimeSlot slotB = b.getTimeSlot(randomItem);
						
			ArrayList<Evaluated> result = child.assign(department.getTimeTable(), randomItem);
			
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
		
		while (!childTree.isValid())
		{
			childTree = new OTree(department, child, unassigned);
			childTree.genSolution(0);
		}
		
		generated.add(childTree.getAssignments());
		
		if (generated.size() < 20)
		{
			DoTheSearchAlready((a.getEvalScore() > b.getEvalScore()) ? a : b, childTree.getAssignments());
		}
		else
		{
			Assignments best = null;
			
			for (Assignments generatedSolution : generated)
			{
				if (best == null)
					best = generatedSolution;
				
				if (generatedSolution.getEvalScore() < best.getEvalScore())
					best = generatedSolution;
			}
			
			return best;
		}
		
		return childTree.getAssignments();
	}
}










