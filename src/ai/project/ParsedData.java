package ai.project;

import java.util.ArrayList;

/**
 * Temporary class to store parsed data
 */
public class ParsedData
{
    public String DeptName = "";
    public ArrayList<TimeSlot> CourseSlots = new ArrayList<TimeSlot>();
    public ArrayList<TimeSlot> LabSlots = new ArrayList<TimeSlot>();
    public ArrayList<Lecture> Courses = new ArrayList<Lecture>();
    public ArrayList<Lab> Labs = new ArrayList<Lab>();
    public ArrayList<SlotItem[]> NotCompatible = new ArrayList<SlotItem[]>();
    public ArrayList<String> Unwanted = new ArrayList<String>();
    public ArrayList<String> Preferences = new ArrayList<String>();
    public ArrayList<SlotItem[]> Pairs = new ArrayList<SlotItem[]>();
    public ArrayList<SlotItem[]> Partials = new ArrayList<SlotItem[]>();
    
    public ParsedData(String deptName, ArrayList<TimeSlot> courseSlots, ArrayList<TimeSlot> labSlots, ArrayList<Lecture> courses, ArrayList<Lab> labs, ArrayList<SlotItem[]> notCompatible, ArrayList<String> unwanted, ArrayList<String> preferences, ArrayList<SlotItem[]> pairs, ArrayList<SlotItem[]> partials)
    {
    	DeptName = deptName;
    	CourseSlots = courseSlots;
    	LabSlots = labSlots;
    	Courses = courses;
    	Labs = labs;
    	NotCompatible = notCompatible;
    	Unwanted = unwanted;
    	Preferences = preferences;
    	Pairs = pairs;
    	Partials = partials;
    }
}
