package ai.project;

import java.util.ArrayList;
import java.util.Random;

public class SetSearch
{
	ArrayList<Assignments> F;
	Department department;
	
	public SetSearch(Department department)
	{
		this.department = department;
	}
	
	public void DoTheSearchAlready(Assignments a, Assignments b)
	{
		ArrayList<SlotItem> evolutionList =  new ArrayList<>();
		evolutionList.addAll(department.getAllCourses());
		
		ArrayList<SlotItem> unassigned = new ArrayList<>();
		
		while (!evolutionList.isEmpty())
		{
			SlotItem random = evolutionList.remove(new Random().nextInt(evolutionList.size()));
		}
	}
}
