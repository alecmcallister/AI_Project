package ai.project;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

public class SetSearch
{
	ArrayList<Assignments> F;
	Department department;
	
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
			
			//System.out.println("Item selected: " + randomItem.toString() + "\t\tA: " + slotA.toString() + "\t\tB: " + slotB.toString());
			
			// Implement Constr* here to chose the timeslot where randomItem goes
			TreeSet<Evaluated> result = child.assign(department.getTimeTable(), randomItem);
			
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
			
			// If neither parent's slots were in result...
			if (!assigned)
			{
				unassigned.add(randomItem);
			}
		}
		
		OTree childTree = new OTree(department, child, unassigned);
		childTree.genSolution();
		
		System.out.println("Child solution found");
		
		return childTree.getAssignments();
	}
}










