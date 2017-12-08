/**
 * Created by dre3k on 2017-10-24.
 */
package ai.project;

import java.io.*;
import java.util.*;

public class Main
{
	private enum Input
	{
		DEPARTMENT_NAME, // = 0
		COURSE_SLOT, // = 1
		LAB_SLOT, // = 2
		COURSE, // = 3
		LAB, // = 4
		NOT_COMPATIBLE, // = 5
		UNWANTED, // = 6
		PREFERENCE, // = 7
		PAIR, // = 8
		PART_ASSIGN,
		UNKNOWN; // = 9
	}

	public static void main(String args[])
	{
		String fileName1 = "";
		String dir = "";
		if (System.getProperty("os.name").equals("Mac OS X"))
		{
			dir = "/";
		}

		else
		{
			dir = "\\";
		}

		if (args.length > 0)
		{
			fileName1 = args[0];
		}
		else
		{
			fileName1 = System.getProperty("user.dir") + dir + "gehtnicht1.txt";
		}


		String[] filenames = {
//				"gehtnicht1.txt",
//				"gehtnicht2.txt",
//				"gehtnicht3.txt",
//				"gehtnicht4.txt",
//				"gehtnicht5.txt",
//				"gehtnicht6.txt",
//				"gehtnicht7.txt",
//				"gehtnicht8.txt",
//				"gehtnicht9.txt",
//				"gehtnicht10.txt",
//				"gehtnicht11.txt",
//				"gehtnicht12.txt",
//				"minnumber.txt",
//				"pairing.txt",
//				"parallelpen.txt",
//				"partials.txt",
//				"prefexamp.txt",
				"deptinst1.txt",
				"deptinst2.txt"
		};

		for (String f : filenames)
		{
			System.out.println("Testing " + f);
			f = System.getProperty("user.dir") + dir + f;
			ParseAndCompute(f);
		}
	}

	public static void ParseAndCompute(String fileName)
	{
		Department department = readFile(fileName);

		ArrayList<Assignments> F = new ArrayList<Assignments>();

		ArrayList<SlotItem> unassigned = new ArrayList<>();
		unassigned.addAll(department.getAllCourses());

		Assignments partial = department.getPartialAssignments();

		if (partial != null)
			unassigned.removeAll(partial.getAllCourses());

		OTree orTree = new OTree(department, partial, unassigned);

		orTree = orTree.genSolution(0);

		while (F.size() < 2)
		{
			orTree = new OTree(department, department.getPartialAssignments(), unassigned);

			orTree = orTree.genSolution(0);

			if (orTree.isValid())
			{
				F.add(orTree.getAssignments());
			}
			else
			{
				System.out.println("No solution found\n");
				return;
			}
		}

		SetSearch setSearch = new SetSearch(department);
		OTree best = null;
		Assignments bestSchedule;

		for (int i = 0; i < 20; i++)
		{
			Assignments parentA = F.get(0);
			Assignments parentB = F.get(1);

			if (best != null)
			{
				parentA = (parentA.getEvalScore() < parentB.getEvalScore()) ? parentA : parentB;
				parentB = best.getAssignments();
			}

			OTree temp = setSearch.DoTheSearchAlready(parentA, parentB);

			if (!temp.isValid())
				continue;

			if (best == null)
			{
				best = temp;
			}
			else if (temp.getAssignments().getEvalScore() < best.getAssignments().getEvalScore())
			{
				best = temp;
				System.out.println("New best eval: " + best.getAssignments().getEvalScore());

				if (temp.getAssignments().getEvalScore() == 0)
					break;
			}
		}
		if (best == null)
		{
			System.out.println("Unable to create child solution.\nReverting to best parent...");
			bestSchedule = (F.get(0).getEvalScore() < F.get(1).getEvalScore()) ? F.get(0) : F.get(1);
		}
		else
			bestSchedule = best.getAssignments();


		System.out.println("Eval = " + bestSchedule.getEvalScore() + "\n" + bestSchedule.toString() + "\n\n");

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName.replace(".txt", "_schedule.txt")), "utf-8")))
		{
			writer.write("Eval = " + bestSchedule.getEvalScore() + "\n" + bestSchedule.toString());
		}
		catch (Exception e)
		{
		}

	}

	public static Department readFile(String fileName)
	{
		String line;
		Input currentInfo = Input.UNKNOWN;

		Department department = null;

		try
		{
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(fileName);


			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null)
			{
				if (line.equals("Name:"))
				{
					line = bufferedReader.readLine();
					department = new Department(line);
				}
				if (line.equals("Course slots:"))
				{
					currentInfo = Input.COURSE_SLOT;
					line = bufferedReader.readLine();
				}
				if (line.equals("Lab slots:"))
				{
					currentInfo = Input.LAB_SLOT;
					line = bufferedReader.readLine();
				}
				if (line.equals("Courses:"))
				{
					currentInfo = Input.COURSE;
					line = bufferedReader.readLine();
				}
				if (line.equals("Labs:"))
				{
					currentInfo = Input.LAB;
					line = bufferedReader.readLine();
				}
				if (line.equals("Not compatible:"))
				{
					currentInfo = Input.NOT_COMPATIBLE;
					line = bufferedReader.readLine();
				}
				if (line.equals("Unwanted:"))
				{
					currentInfo = Input.UNWANTED;
					line = bufferedReader.readLine();
				}
				if (line.equals("Preferences:"))
				{
					currentInfo = Input.PREFERENCE;
					line = bufferedReader.readLine();
				}
				if (line.equals("Pair:"))
				{
					currentInfo = Input.PAIR;
					line = bufferedReader.readLine();
				}
				if (line.equals("Partial assignments:"))
				{
					currentInfo = Input.PART_ASSIGN;
					line = bufferedReader.readLine();
				}
				if (line.length() == 0)
				{
					currentInfo = Input.UNKNOWN;
				}

				if (currentInfo == Input.COURSE_SLOT)
				{
					String[] split = line.split(",");

					String day = split[0].trim();
					String time = split[1].trim();
					int max = Integer.parseInt(split[2].trim());
					int min = Integer.parseInt(split[3].trim());

					department.addTimeSlot(day, time, max, min, false);
				}
				else if (currentInfo == Input.LAB_SLOT)
				{
					String[] split = line.split(",");

					String day = split[0].trim();
					String time = split[1].trim();
					int max = Integer.parseInt(split[2].trim());
					int min = Integer.parseInt(split[3].trim());

					department.addTimeSlot(day, time, max, min, true);
				}
				else if (currentInfo == Input.COURSE)
				{
					String[] split = line.split(" ");

					String courseName = split[0];
					int courseNum = Integer.parseInt(split[1]);
					int lecNum = Integer.parseInt(split[3]);

					department.addLecture(courseName, courseNum, lecNum);
					if (courseNum == 413 && courseName.equals("CPSC"))
					{
						department.addLecture(courseName, 913, lecNum);
						department.addPartialLecture(courseName, 913, lecNum, "TU", "18:00");
					}
					if (courseNum == 313 && courseName.equals("CPSC"))
					{
						department.addLecture(courseName, 813, lecNum);
						department.addPartialLecture(courseName, 813, lecNum, "TU", "18:00");
					}


				}
				else if (currentInfo == Input.LAB)
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
						{
							if (lecture.courseName == courseName && lecture.courseNum == courseNum && lecture.secNum == secNum)
							{
								parent = lecture;
							}
						}

						if (parent != null)
						{
							department.addLab(courseName, courseNum, labNum, parent.secNum);
						}

						else
						{
							department.addLab(courseName, courseNum, labNum);
						}
					}
				}
				else if (currentInfo == Input.NOT_COMPATIBLE)
				{
					String[] items = line.split(",");

					ArrayList<SlotItem> consolidatedList = new ArrayList<>();
					consolidatedList.addAll(department.getAllCourses());

					SlotItem course1 = SelectItem(consolidatedList, items[0]);
					SlotItem course2 = SelectItem(consolidatedList, items[1]);

					department.addIncompatible(course1.courseName, course1.courseNum, course1.getClass() == Lab.class, course1.secNum, course2.courseName, course2.courseNum, course2.getClass() == Lab.class, course2.secNum);
				}
				else if (currentInfo == Input.UNWANTED)
				{
					String[] items = line.split(",");

					ArrayList<SlotItem> consolidatedList = new ArrayList<>();
					consolidatedList.addAll(department.getAllCourses());

					SlotItem course = SelectItem(consolidatedList, items[0]);

					department.addUnwanted(course.courseName, course.courseNum, course.secNum, items[1].trim(), items[2].trim(), course.getClass() == Lab.class);
				}
				else if (currentInfo == Input.PREFERENCE)
				{
					String[] items = line.split(",");

					ArrayList<SlotItem> consolidatedList = new ArrayList<>();
					consolidatedList.addAll(department.getAllCourses());

					SlotItem course = SelectItem(consolidatedList, items[2]);

					department.addPreference(course.courseName, course.courseNum, course.secNum, items[0], items[1].trim(), Integer.parseInt(items[3].trim()), course.getClass() == Lab.class);
				}
				else if (currentInfo == Input.PAIR)
				{
					String[] items = line.split(",");

					ArrayList<SlotItem> consolidatedList = new ArrayList<>();
					consolidatedList.addAll(department.getAllCourses());

					SlotItem course1 = SelectItem(consolidatedList, items[0]);
					SlotItem course2 = SelectItem(consolidatedList, items[1]);

					department.addPair(course1.courseName, course1.courseNum, course1.secNum, course1.getClass() == Lab.class, course2.courseName, course2.courseNum, course2.secNum, course2.getClass() == Lab.class);
				}
				else if (currentInfo == Input.PART_ASSIGN)
				{
					// input probably like:
					// day, time, courseName+lecture/tut
					// Ex. MO, 8:00, CPSC 203 LEC 01
					// Ex. MO, 17:00, CPSC 203 LEC 95 TUT 96
					// CPSC 433 LEC 01, MO, 8:00
					String[] items = line.split(",");
					String day = "";
					String time = "";
					String courseData = "";

					for (String item : items) {
						if (item.trim().length() == 2)
							day = item.trim();
						else if (item.trim().length() < 8 && item.contains(":"))
							time = item.trim();
						else if (item.trim().length() > 8)
							courseData = item.trim();
					}

					String[] slotData = courseData.trim().split(" ");
					boolean isLab = courseData.contains("TUT") || courseData.contains("LAB");

					String courseName = slotData[0];
					int courseNum = Integer.parseInt(slotData[1]);
					int secNum = Integer.parseInt(slotData[slotData.length - 1]);

//                    department.addPartial(courseName, courseNum, secNum, isLab, day, time);
					if (isLab)
					{
						department.addPartialLab(courseName, courseNum, secNum, day, time);
					}
					else
					{
						department.addPartialLecture(courseName, courseNum, secNum, day, time);
					}

					// Do the department add partials thing here
				}
			}

			bufferedReader.close();
		}
		catch (FileNotFoundException ex)
		{
			System.out.println("Unable to open file, file not found '" + fileName + "'");
		}
		catch (IOException ex)
		{
			System.out.println("Error reading file '" + fileName + "'");
		}

		//System.out.println("Reading file completed...");
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
				{
					return course;
				}
			}
			else
			{
				Lab lab = (Lab) course;

				if (!lab.hasParent())
				{
					if (lab.courseName.equals(courseName) && lab.courseNum == courseNum && lab.secNum == labSection)
					{
						return course;
					}
				}
				else if (lab.hasParent())
				{
					if (lab.courseName.equals(courseName) && lab.courseNum == courseNum && lab.secNum == labSection && lab.getParent().secNum == courseSection)
					{
						return course;
					}
				}
			}
		}

		return null;
	}
}





















