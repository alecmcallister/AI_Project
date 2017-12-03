/**
 * Created by dre3k on 2017-10-24.
 */
package ai.project;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import sun.font.TrueTypeFont;

import java.nio.file.*;

public class Main {
    public static void main(String args[]) 
    {
        String fileName1 = "deptinst1.txt";
        String fileName2 = "deptinst2.txt";
//        readFile(fileName1);
//        readFile(fileName2);
        DoTest(fileName1);
    }
    
    public static void DoTest(String fileName) 
    {
    	Department department = readFile(fileName);  
    	
    	System.out.println("Department: " + department.getDepartmentName());
    }
    
    public static Department readFile(String fileName) 
    {
        String line = null;
        int currentInfo = 0;

        String deptName = "";
        ArrayList<TimeSlot> courseSlots = new ArrayList<TimeSlot>();
        ArrayList<TimeSlot> labSlots = new ArrayList<TimeSlot>();
        ArrayList<Lecture> courses = new ArrayList<Lecture>();
        ArrayList<Lab> labs = new ArrayList<Lab>();
        ArrayList<SlotItem[]> notCompatible = new ArrayList<SlotItem[]>();
        ArrayList<String> unwanted = new ArrayList<String>();
        ArrayList<String> preferences = new ArrayList<String>();
        ArrayList<SlotItem[]> pairs = new ArrayList<SlotItem[]>();
        ArrayList<SlotItem[]> partials = new ArrayList<SlotItem[]>();
        
        Department department = null;

        try 
        {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(Paths.get("").toAbsolutePath().toString() + "/" + fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                if (line.equals("Name:")) {
                    deptName = bufferedReader.readLine();
                    department = new Department(deptName);
                }
                if (line.equals("Course slots:")) {
                    currentInfo = 1;
                    line = bufferedReader.readLine();
                }
                if (line.equals("Lab slots:")) {
                    currentInfo = 2;
                    line = bufferedReader.readLine();
                }
                if (line.equals("Courses:")) {
                    currentInfo = 3;
                    line = bufferedReader.readLine();
                }
                if (line.equals("Labs:")) {
                    currentInfo = 4;
                    line = bufferedReader.readLine();
                }
                if (line.equals("Not compatible:")) {
                    currentInfo = 5;
                    line = bufferedReader.readLine();
                }
                if (line.equals("Unwanted:")) {
                    currentInfo = 6;
                    line = bufferedReader.readLine();
                }
                if (line.equals("Preferences:")) {
                    currentInfo = 7;
                    line = bufferedReader.readLine();
                }
                if (line.equals("Pair:")) {
                    currentInfo = 8;
                    line = bufferedReader.readLine();
                }
                if (line.equals("Partial assignments:")) {
                    currentInfo = 9;
                    line = bufferedReader.readLine();
                }
                if (line.length() == 0) {
                    currentInfo = 10;
                }

                if (currentInfo == 1) 
                {
                	String[] split = line.split(",");
                	
                	String day = split[0].trim();
                	String time = split[1].trim();
                	int max = Integer.parseInt(split[2].trim());
                	int min = Integer.parseInt(split[3].trim());
						
                    //courseSlots.add(new TimeSlot(day, time, max, min, false));
                    department.addTimeSlot(day, time, max, min, false);
                }
                else if (currentInfo == 2) 
                {
                	String[] split = line.split(",");
                	
                	String day = split[0].trim();
                	String time = split[1].trim();
                	int max = Integer.parseInt(split[2].trim());
                	int min = Integer.parseInt(split[3].trim());
						
                    //labSlots.add(new TimeSlot(day, time, max, min, true));
                    department.addTimeSlot(day, time, max, min, true);
                } 
                else if (currentInfo == 3) 
                {
                	String[] split = line.split(" ");

                	String courseName = split[0];
                	int courseNum = Integer.parseInt(split[1]);
                	int lecNum = Integer.parseInt(split[3]);
                	
                    //courses.add(new Lecture(courseName, courseNum, lecNum));
                    department.addLecture(courseName, courseNum, lecNum);
                } 
                else if (currentInfo == 4) 
                {
                	String[] split = line.split(" ");
                	String courseName = split[0];
                	int courseNum = Integer.parseInt(split[1]);

                	if (split.length == 4)
                	{
                    	int labNum = Integer.parseInt(split[3]);
                    	
                        //labs.add(new Lab(courseName, courseNum, labNum));
                        department.addLab(courseName, courseNum, labNum);
                	}
                	else if (split.length == 6)
                	{
                		int secNum = Integer.parseInt(split[3]);
                    	int labNum = Integer.parseInt(split[5]);
                    	
                    	Lecture parent = null;
                    	
                    	for (Lecture lecture : courses)
							if (lecture.courseName == courseName && lecture.courseNum == courseNum && lecture.secNum == secNum)
								parent = lecture;

                    	if (parent != null)
                    	{
                            department.addLab(courseName, courseNum, labNum, parent.secNum);

                    		//labs.add(new Lab(courseName, courseNum, labNum, parent));
                    	}
                    	else
                    	{
                            department.addLab(courseName, courseNum, labNum);

                    		//labs.add(new Lab(courseName, courseNum, labNum));
                    	}
                	}
                } 
                else if (currentInfo == 5) 
                {
                	String[] items = line.split(",");
                	
                	ArrayList<SlotItem> consolidatedList = new ArrayList<>();
                	consolidatedList.addAll(department.getAllCourses());
                	
                	SlotItem course1 = SelectItem(consolidatedList, items[0]);
                	SlotItem course2 = SelectItem(consolidatedList, items[1]);
                	
                	
                	//notCompatible.add(new SlotItem[] { course1, course2 });
                	System.out.println("Adding incompatibility for " + course1.toString() + " - " + course2.toString());
                	//course1.addIncompatibility(course2);
                	department.addIncompatible(course1.courseName, course1.courseNum, course1.getClass() == Lab.class, course1.secNum, course2.courseName, course2.courseNum, course2.getClass() == Lab.class, course2.secNum);
                } 
                else if (currentInfo == 6) 
                {
                	String[] items = line.split(",");

                	ArrayList<SlotItem> consolidatedList = new ArrayList<>();
                	consolidatedList.addAll(department.getAllCourses());
                	
                	SlotItem course = SelectItem(consolidatedList, items[0]);
                	
                	department.addUnwanted(course.courseName, course.courseNum, course.secNum, items[1].trim(), items[2].trim(), course.getClass() == Lab.class);
                	
                    //unwanted.add(line);
                } 
                else if (currentInfo == 7) 
                {
                	String[] items = line.split(",");

                	ArrayList<SlotItem> consolidatedList = new ArrayList<>();
                	consolidatedList.addAll(department.getAllCourses());
                	
                	SlotItem course = SelectItem(consolidatedList, items[2]);
                	                	
                	department.addPreference(course.courseName, course.courseNum, course.secNum, items[0], items[1].trim(), Integer.parseInt(items[3].trim()), course.getClass() == Lab.class);
                	
                    //preferences.add(line);
                } 
                else if (currentInfo == 8) 
                {
                	String[] items = line.split(",");
                	
                	ArrayList<SlotItem> consolidatedList = new ArrayList<>();
                	consolidatedList.addAll(department.getAllCourses());
                	
                	SlotItem course1 = SelectItem(consolidatedList, items[0]);
                	SlotItem course2 = SelectItem(consolidatedList, items[1]);

                	//course1.addPair(course2);
                	//course2.addPair(course1);
                	
                	department.addPair(course1.courseName, course1.courseNum, course1.secNum, course1.getClass() == Lab.class, course2.courseName, course2.courseNum, course2.secNum, course2.getClass() == Lab.class);
                	
                    //pairs.add(new SlotItem[] { course1, course2 });
                } 
                else if (currentInfo == 9) 
                {
                	String[] items = line.split(",");
                	
                	ArrayList<SlotItem> consolidatedList = new ArrayList<>();
                	consolidatedList.addAll(courses);
                	consolidatedList.addAll(labs);
                	
                	SlotItem course1 = SelectItem(consolidatedList, items[0]);
                	SlotItem course2 = SelectItem(consolidatedList, items[1]);
                	                	
                    partials.add(new SlotItem[] { course1, course2 });
                }
            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }
        
//        System.out.println("NOT COMPATIBLE COURSES ARE");
//        for (String course : notCompatible) {
//            System.out.print(course + ";\n");
//        }
//        System.out.println();
        
        return department;
//        return new ParsedData(deptName, courseSlots, labSlots, courses, labs, notCompatible, unwanted, preferences, pairs, partials);
    }
    
    public static SlotItem SelectItem(ArrayList<SlotItem> fromList, String dirty)
    {    	
    	dirty = dirty.trim();

    	String[] item = dirty.split(" ");
    	
    	String courseName = item[0];
    	
    	int courseNum = Integer.parseInt(item[1]);
    	    	
    	int courseSection = Integer.parseInt(item[3]); 
    	
    	int labSection = Integer.parseInt((item.length == 4) ? item[3] : item[5]);
    	    	
		for (SlotItem course : fromList)
		{
			if (course.getClass() == Lecture.class)
			{
				if (course.courseName.equals(courseName) && course.courseNum == courseNum && course.secNum == courseSection)
					return course;
			}
			else
			{
				Lab lab = (Lab)course;
				
				if (!lab.hasParent())
				{
					if (lab.courseName.equals(courseName) && lab.courseNum == courseNum && lab.secNum == labSection)
						return course;
				}
				else if (lab.hasParent())
				{
					if (lab.courseName.equals(courseName) && lab.courseNum == courseNum && lab.secNum == labSection && lab.getParent().secNum == courseSection)
						return course;
				}
			}
		}
		
		return null;
    }
}





















