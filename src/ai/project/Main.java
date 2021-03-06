package ai.project;

import java.io.*;
import java.util.*;

/**
 * CPSC 433 Project
 * Fall 2017
 * Group 08
 *
 * Computes a solution to the course scheduling problem, as given by the assignment specification, using Set-based
 * Search backed by an Or-Tree.
 *
 * Usage:
 *  Compile as set of classes or jar. Call on command line as follows:
 *       java (Main or JAR) [input-filename]
 *       java (Main or JAR) [input-filename] [config-filename]
 *
 * Where:
 *  input-filename is a path, relative or absolute, to the input file.
 *  config-filename is a path, relative or absolute, to the config file
 *
 * If a config file is not specified, the application will try to load config.properties from the current directory.
 * If that is not found, all penalties will be initialized to 0.
 *
 * A valid filename must be given for this program to run.
 */
public class Main
{
	private enum Input
	{
		COURSE_SLOT,
		LAB_SLOT,
		COURSE,
		LAB,
		NOT_COMPATIBLE,
		UNWANTED,
		PREFERENCE,
		PAIR,
		PART_ASSIGN,
		UNKNOWN
	}

    /**
     * Main method.
     * Parses arguments and passes them off to appropriate readers, then calls the input file parser
     * and computes a solution.
     *
     * @param args The args to the program. This application takes one or two args, the first of which must be
     *             an input filename/path, and the second of which is optionally a config file.
     */
	public static void main(String args[])
	{
		String fileName ;
        if ((args.length < 1) || (args.length > 2)) {
            System.out.println("Insufficient arguments. Accepted formats:");
            printFormats();
            System.out.println("Aborting.");
            System.exit(-1);
        }
        fileName = args[0];
        if (args.length == 2) {
            try {
                readPenaltiesFromConfig(args[1]);
            }
            catch (IOException ioex) {
                System.err.println("Could not read config file. Aborting.");
                System.exit(-2);
            }
        }
        else {
            System.out.println("No config file provided on command line. Trying default 'config.properties' in" +
                    " current dir...");
            try {
                readPenaltiesFromConfig("config.properties");
                System.out.println("Config file read successfully.");
            }
            catch (IOException ioex) {
                System.out.println("Could not open config.properties in current dir.");
                System.out.println("Continuing with penalties all set to 0.");
            }
        }

        System.out.println("------------------");
        ParseAndCompute(fileName);
	}

    /**
     * Print to System.out the valid formats for running the program on the command line.
     */
    public static void printFormats() {
        System.out.println("    java (Main or JAR) <input-filename>");
        System.out.println("    java (Main or JAR) <input-filename> <config-filename>");
    }

    /**
     * Parses a configuration file (properties file) for the Penalties values.
     *
     * @param configFile The configuration file containing Penalties.
     * @throws IOException Thrown if the file cannot be opened.
     */
    public static void readPenaltiesFromConfig(String configFile) throws IOException {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            // Set up stream
            input = new FileInputStream(configFile);

            // Load config file
            prop.load(input);

            // Get properties and assign them to the Penalties instance
            Penalties penalties = Penalties.getInstance();

            penalties.setCourseMin(Integer.parseInt(prop.getProperty("penCourseMin")));
            penalties.setLabsMin(Integer.parseInt(prop.getProperty("penLabsMin")));
            penalties.setNotPaired(Integer.parseInt(prop.getProperty("penNotPaired")));
            penalties.setSection(Integer.parseInt(prop.getProperty("penSection")));
            penalties.setwMinFilled(Integer.parseInt(prop.getProperty("wMinFilled")));
            penalties.setwPref(Integer.parseInt(prop.getProperty("wPref")));
            penalties.setwPair(Integer.parseInt(prop.getProperty("wPair")));
            penalties.setwSecDiff(Integer.parseInt(prop.getProperty("wSecDiff")));
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException ioex) {
                    System.err.println("Warning: could not close config file.");
                }
            }
        }
    }

    /**
     * Parse an input file and compute the solution.
     *
     * @param fileName The input file name.
     */
	public static void ParseAndCompute(String fileName)
	{
        System.out.println("Searching for solution for " + fileName + ".");
		Department department = readFile(fileName);

		ArrayList<Assignments> F = new ArrayList<>();

		ArrayList<SlotItem> unassigned = new ArrayList<>();
		unassigned.addAll(department.getAllCourses());

		Assignments partial = department.getPartialAssignments();


		if (partial != null)
			unassigned.removeAll(partial.getAllCourses());

		OTree orTree;

        while (F.size() < 2)
		{
			orTree = new OTree(department, department.getPartialAssignments(), unassigned);
			orTree = orTree.genSolution();

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


        System.out.println();
		System.out.println("Eval-value: " + bestSchedule.getEvalScore() + "\n" + bestSchedule.toString() + "\n\n");

		bestSchedule.WriteToFile(fileName);
	}

    /**
     * Parses the input file and translates the contents into a Department object.
     *
     * @param fileName The name of the input file.
     * @return A Department, containing all information in the input file.
     */
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
				if (line == null || line.length() == 0)
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
							if (lecture.courseName.equals(courseName) && lecture.courseNum == courseNum && lecture.secNum == secNum)
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

					if (isLab)
					{
						department.addPartialLab(courseName, courseNum, secNum, day, time);
					}
					else
					{
						department.addPartialLecture(courseName, courseNum, secNum, day, time);
					}

				}
			}

			bufferedReader.close();
		}
		catch (FileNotFoundException ex)
		{
			System.err.println("Unable to open file, file not found '" + fileName + "'");
		}
		catch (IOException ex)
		{
			System.err.println("Error reading file '" + fileName + "'");
		}

		return department;
	}

    /**
     * Matches a string parsed from the input file to a SlotItem in an ArrayList.
     *
     * @param fromList The ArrayList to select from.
     * @param dirty The parsed string to search for.
     * @return The SlotItem that matches the String, if present in the array. Null if not.
     */
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





















