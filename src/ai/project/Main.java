/**
 * Created by dre3k on 2017-10-24.
 */
package ai.project;

import java.io.*;
import java.util.ArrayList;
import java.nio.file.*;

public class Main {
    public static void main(String args[]) 
    {
        String fileName1 = "";
        
        if (args.length > 0)
            fileName1 = args[0];
        else
            fileName1 = System.getProperty("user.dir") + "\\gehtnicht6.txt";

        DoTest(fileName1);
    }
    
    public static void DoTest(String fileName) 
    {
    	Department department = readFile(fileName);  
    	
    	System.out.println("Department: " + department.getDepartmentName());
    	
    	ArrayList<Assignments> F = new ArrayList<Assignments>();
    	
    	OTree orTree = new OTree(department, null, null);
    	
    	final long startTime = System.currentTimeMillis();
		orTree = orTree.genSolution(0);
    	final long endTime = System.currentTimeMillis();
    	
    	System.out.println("Total execution time: " + Long.toString(endTime - startTime) );
    	System.out.println("Result:\n" + orTree.getAssignments().toString() );
	System.out.println("isvalid = " + orTree.isValid());
	if (orTree.isValid()) {
		System.out.println("Eval = " + orTree.getAssignments().getEvalScore());
	}
    	return;
    	
    	/*
    	while (F.size() < 2)
		{
        	orTree = new OTree(department, null, null);
        	
        	orTree = orTree.genSolutionAsync();
        	
        	System.out.println( "Total execution time: " + Long.toString(endTime - startTime) );
        	
        	if( null == orTree )
        		System.out.println("Or-Tree Timed out");
        	else if (orTree.isValid())
        	{
        		System.out.println("Or-Tree Returned a Valid solution!");
        		F.add(orTree.getAssignments());
        	}
		}

    	SetSearch setSearch = new SetSearch(department);
    	setSearch.DoTheSearchAlready(F.get(0), F.get(1));*/
    }
    
    public static Department readFile(String fileName) 
    {
        String line = null;
        int currentInfo = 0;
        System.out.println("READING IN FILE");
        Department department = null;

        try 
        {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                if (line.equals("Name:")) {
                    line = bufferedReader.readLine();
                    System.out.println("NAME IS: " + line);
                    department = new Department(line);
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
						
                    department.addTimeSlot(day, time, max, min, false);
                }
                else if (currentInfo == 2) 
                {
                	String[] split = line.split(",");
                	
                	String day = split[0].trim();
                	String time = split[1].trim();
                	int max = Integer.parseInt(split[2].trim());
                	int min = Integer.parseInt(split[3].trim());
						
                    department.addTimeSlot(day, time, max, min, true);
                } 
                else if (currentInfo == 3) 
                {
                	String[] split = line.split(" ");

                	String courseName = split[0];
                	int courseNum = Integer.parseInt(split[1]);
                	int lecNum = Integer.parseInt(split[3]);
                	
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
                    	
                        department.addLab(courseName, courseNum, labNum);
                	}
                	else if (split.length == 6)
                	{
                		int secNum = Integer.parseInt(split[3]);
                    	int labNum = Integer.parseInt(split[5]);
                    	
                    	Lecture parent = null;
                    	
                    	for (Lecture lecture : department.getAllLectures())
							if (lecture.courseName == courseName && lecture.courseNum == courseNum && lecture.secNum == secNum)
								parent = lecture;
                    	
                    	if (parent != null)
                            department.addLab(courseName, courseNum, labNum, parent.secNum);

                    	else
                            department.addLab(courseName, courseNum, labNum);
                	}
                } 
                else if (currentInfo == 5) 
                {
                	String[] items = line.split(",");
                	
                	ArrayList<SlotItem> consolidatedList = new ArrayList<>();
                	consolidatedList.addAll(department.getAllCourses());
                	
                	SlotItem course1 = SelectItem(consolidatedList, items[0]);
                	SlotItem course2 = SelectItem(consolidatedList, items[1]);
                	
                	department.addIncompatible(course1.courseName, course1.courseNum, course1.getClass() == Lab.class, course1.secNum, course2.courseName, course2.courseNum, course2.getClass() == Lab.class, course2.secNum);
                } 
                else if (currentInfo == 6) 
                {
                	String[] items = line.split(",");

                	ArrayList<SlotItem> consolidatedList = new ArrayList<>();
                	consolidatedList.addAll(department.getAllCourses());
                	
                	SlotItem course = SelectItem(consolidatedList, items[0]);
                	
                	department.addUnwanted(course.courseName, course.courseNum, course.secNum, items[1].trim(), items[2].trim(), course.getClass() == Lab.class);
                } 
                else if (currentInfo == 7) 
                {
                	String[] items = line.split(",");

                	ArrayList<SlotItem> consolidatedList = new ArrayList<>();
                	consolidatedList.addAll(department.getAllCourses());
                	
                	SlotItem course = SelectItem(consolidatedList, items[2]);
                	                	
                	department.addPreference(course.courseName, course.courseNum, course.secNum, items[0], items[1].trim(), Integer.parseInt(items[3].trim()), course.getClass() == Lab.class);
                } 
                else if (currentInfo == 8) 
                {
                	String[] items = line.split(",");
                	
                	ArrayList<SlotItem> consolidatedList = new ArrayList<>();
                	consolidatedList.addAll(department.getAllCourses());
                	
                	SlotItem course1 = SelectItem(consolidatedList, items[0]);
                	SlotItem course2 = SelectItem(consolidatedList, items[1]);
                	
                	department.addPair(course1.courseName, course1.courseNum, course1.secNum, course1.getClass() == Lab.class, course2.courseName, course2.courseNum, course2.secNum, course2.getClass() == Lab.class);                	
                } 
                else if (currentInfo == 9) 
                {
                	 // input probably like:
                    // day, time, courseName+lecture/tut
                    // Ex. MO, 8:00, CPSC 203 LEC 01
                    // Ex. MO, 17:00, CPSC 203 LEC 95 TUT 96
                    // CPSC 433 LEC 01, MO, 8:00
                    String[] items = line.split(",");
                    String[] slotData = items[0].trim().split(" ");
                    
                    String day = items[1];
                    String time = items[2];
                    boolean isLab = items[0].contains("TUT") || items[2].contains("LAB");
                    String courseName = slotData[0];
                    int courseNum = Integer.parseInt(slotData[1]);
                    int secNum = Integer.parseInt(slotData[slotData.length - 1]);

//                    department.addPartial(courseName, courseNum, secNum, isLab, day, time);
                    if (isLab)
                        department.addPartialLab(courseName, courseNum, secNum, day, time);
                    else
                        department.addPartialLecture(courseName, courseNum, secNum, day, time);
                	
                	// Do the department add partials thing here
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
        
        return department;
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





















