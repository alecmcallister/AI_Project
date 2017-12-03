package ai.project;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Department Class
 *
 * Contains data which is constant for a given department:
 *  - Name of the department
 *  - Table of possible time slots
 *  - Table of all courses (labs and lectures)
 *  - List of partial assignments
 *  - Penalty values
 *
 * This class includes methods to add time slots, lectures, and labs to the internal tables.
 * There are also methods to set constraints on courses, and to get various subsets of the data contained.
 */

public class Department {
    private String departmentName;

    private TimeTable timeTable;
    private CourseTable courseTable;
    private Assignments partialAssignments;
    private Penalties penalties;

    /**
     * Base constructor. Sets name and initializes tables.
     * It's not yet clear how penalties will be taken as input, so this currently creates a placeholder
     * Penalties object with all 4 penalty types set to 0.
     *
     * Partial Assignments are not created here, because they rely on the TimeTable having already been made.
     *
     * @param departmentName The name of the new department.
     */
    public Department(String departmentName) {
        this.departmentName = departmentName;
        timeTable = new TimeTable();
        courseTable = new CourseTable();
        penalties = new Penalties(0,0,0,0);
    }

    /**
     * Constructor that takes a Penalties instance.
     * Presumably, regardless of how penalties are passed in, we can construct the Penalties object somewhere else.
     * If we can pass them in through the constructor (i.e. penalty values are known before we need to start
     * parsing) then we can set them here.
     *
     * Partial Assignments are not created here, because they rely on the TimeTable having already been made.
     *
     * @param departmentName The name of the new department.
     */
    public Department(String departmentName, Penalties penalties) {
        this.departmentName = departmentName;
        timeTable = new TimeTable();
        courseTable = new CourseTable();
        this.penalties = penalties;
    }

    /**
     * Set the Penalties for the Department. This should be used if, for some reason, we won't know the Penalties
     * before initialization. If that is the case, then the constructor that takes a Penalties instance should be
     * used instead.
     *
     * @param penalties The soft constraint Penalties to be used for this Department.
     */
    public void setPenalties(Penalties penalties) {
        this.penalties = penalties;
    }

    /**
     * Gets the name of the department.
     *
     * @return The name of the department.
     */
    public String getDepartmentName() {
        return departmentName;
    }


    // ------------- Data Adders -------------

    /**
     * Adds a TimeSlot to the TimeTable.
     *
     * @param day The day string, as one of MO, TU, or FR.
     * @param time The time string, in the format H:MM or HH:MM.
     * @param max The maximum number of lectures in this slot.
     * @param min The minimum number of lectures in this slot.
     * @param isLab Is the slot being added for a lab?
     */
    public void addTimeSlot(String day, String time, int max, int min, boolean isLab) {
        timeTable.updateTable(new TimeSlot(day, time, max, min, isLab));
    }


    /**
     * Adds a Lecture to the pool of courses for the department.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG).
     * @param courseNum The number of the course (e.g. 203, 433).
     * @param lecNum The lecture number (e.g. 01, 99).
     */
    public void addLecture(String courseName, int courseNum, int lecNum) {
        courseTable.addLecture(courseName, courseNum, lecNum);
    }

    /**
     * Adds a Lab *without a parent lecture* to the pool of courses for the department.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG).
     * @param courseNum The number of the course (e.g. 203, 433).
     * @param labNum The lab number (e.g. 01, 99).
     */
    public void addLab(String courseName, int courseNum, int labNum) {
        courseTable.addLab(courseName, courseNum, labNum);
    }

    /**
     * Adds a Lab *with a parent lecture* to the pool of courses for the department.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG).
     * @param courseNum The number of the course (e.g. 203, 433).
     * @param labNum The lab number (e.g. 01, 99).
     * @param lecNum The number of the parent lecture (e.g. 01, 99).
     */
    public void addLab(String courseName, int courseNum, int labNum, int lecNum) {
        courseTable.addLab(courseName, courseNum, labNum, lecNum);
    }

    /**
     * Creates a partial assignment and adds it to the set of partial assignments.
     * Because neither sample input file at this point includes example partial assignments,
     * this method may be subject to change.
     *
     * @param courseName The name of the course being assigned.
     * @param courseNum The number of the course being assigned.
     * @param secNum The section number of the course (lab or tut #).
     * @param isLab Are we assigning a lab?
     * @param day The day string (as either MO, TU, or FR).
     * @param time The time string (as either H:MM or HH:MM).
     */
    public void addPartial(String courseName, int courseNum, int secNum, boolean isLab, String day, String time) {
        if (partialAssignments == null) {
            partialAssignments = new Assignments(penalties, timeTable);
        }
        TimeSlot slot = timeTable.getSlot(day, time, isLab);
        SlotItem course;
        if (isLab)
            course = courseTable.getLab(courseName, courseNum, secNum);
        else
            course = courseTable.getLecture(courseName, courseNum, secNum);

        if ((slot != null) && (course != null)) partialAssignments.addAssignment(slot, course);
    }


    // ------------- Constraint Adders -------------

    /**
     * Adds a non-compatibility constraint to the course pool.
     *
     * Note that this function only needs to be called once in order for the compatibility restraint to be created
     * for both courses involved.
     *
     * @param course1Name The name of the first course in the non-compatibility pair (e.g. CPSC, SENG).
     * @param course1Num The number of the first course in the non-compatibility pair (e.g. 203, 433).
     * @param is1Lab Is the first course a lab?
     * @param sec1Num The section number (lec or tut #) for the first course.
     * @param course2Name The name of the second course in the non-compatibility pair (e.g. CPSC, SENG).
     * @param course2Num The number of the second course in the non-compatibility pair (e.g. 203, 433).
     * @param is2Lab Is the second course a lab?
     * @param sec2Num The section number (lec or tut #) for the second course.
     */
    public void addIncompatible(String course1Name, int course1Num, boolean is1Lab, int sec1Num,
                                String course2Name, int course2Num, boolean is2Lab, int sec2Num) {
        if (is1Lab) {
            if (is2Lab)
                courseTable.addIncompatibleLabs(course1Name, course1Num, sec1Num, course2Name, course2Num, sec2Num);
            else
                courseTable.addIncompatibleLecLab(course2Name, course2Num, sec2Num, course1Name, course1Num, sec1Num);
        }
        else if (is2Lab) {
            courseTable.addIncompatibleLecLab(course1Name, course1Num, sec1Num, course2Name, course2Num, sec2Num);
        }
        else {
            courseTable.addIncompatibleLectures(course1Name, course1Num, sec1Num, course2Name, course2Num, sec2Num);
        }
    }

    /**
     * Adds a preference constraint to the course pool.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG).
     * @param courseNum The number of the course (e.g. 203, 433).
     * @param secNum The section number (lec or tut #) for the course.
     * @param day The preferred day (as either MO, TU, or FR).
     * @param time The preferred time (as a string in the format H:MM or HH:MM).
     * @param preference The preference value, as an integer.
     * @param isLab Is the course a lab?
     */
    public void addPreference(String courseName, int courseNum, int secNum,
                              String day, String time, int preference, boolean isLab) {
        TimeSlot slot = timeTable.getSlot(day, time, /*isLab*/ isLab);
        if (isLab)
            courseTable.addPreferenceLab(courseName, courseNum, secNum, slot, preference);
        else
            courseTable.addPreferenceLecture(courseName, courseNum, secNum, slot, preference);
    }

    /**
     * Adds an unwanted constraint to the course pool.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG).
     * @param courseNum The number of the course (e.g. 203, 433).
     * @param secNum The section number (lec or tut #) for the course.
     * @param day The unwanted day (as either MO, TU, or FR).
     * @param time The unwanted time (as a string in the format H:MM or HH:MM).
     * @param isLab Is the course a lab?
     */
    public void addUnwanted(String courseName, int courseNum, int secNum, String day, String time, boolean isLab) {
        TimeSlot slot = timeTable.getSlot(day, time, /*isLab*/ isLab);
        if (isLab)
            courseTable.addUnwantedLab(courseName, courseNum, secNum, slot);
        else
            courseTable.addUnwantedLecture(courseName, courseNum, secNum, slot);
    }

    /**
     * Adds a pair constraint to the course pool.
     *
     * @param course1Name The name of the first course in the pair (e.g. CPSC, SENG).
     * @param course1Num The number of the first course in the pair (e.g. 203, 433).
     * @param is1Lab Is the first course a lab?
     * @param sec1Num The section number (lec or tut #) for the first course.
     * @param course2Name The name of the second course in the pair (e.g. CPSC, SENG).
     * @param course2Num The number of the second course in the pair (e.g. 203, 433).
     * @param is2Lab Is the second course a lab?
     * @param sec2Num The section number (lec or tut #) for the second course.
     */
    public void addPair(String course1Name, int course1Num, int sec1Num, boolean is1Lab,
                        String course2Name, int course2Num, int sec2Num, boolean is2Lab) {
        if (is1Lab) {
            if (is2Lab)
                courseTable.addPairLabs(course1Name, course1Num, sec1Num, course2Name, course2Num, sec2Num);
            else
                courseTable.addPairLecLab(course2Name, course2Num, sec2Num, course1Name, course1Num, sec1Num);
        }
        else if (is2Lab) {
            courseTable.addPairLecLab(course1Name, course1Num, sec1Num, course2Name, course2Num, sec2Num);
        }
        else {
            courseTable.addPairLectures(course1Name, course1Num, sec1Num, course2Name, course2Num, sec2Num);
        }
    }


    // ------------- Data Getters -------------

    /**
     * Get the TimeSlot for a given TimePair, if one exists. Null otherwise.
     *
     * @param time TimePair to search the TimeTable for.
     * @return The TimeSlot in the TimeTable that matches time, or null if it is not there.
     */
    public TimeSlot getSlot(TimePair time) { return timeTable.getSlot(time); }

    /**
     * Try to find a TimeSlot in the TimeTable that matches the given TimeSlot.
     * If a match is found, this will return the TimeSlot that equals (.equals()) the given TimeSlot.
     * This can be, but is not necessarily, the same TimeSlot reference passed to the method.
     *
     * @param timeSlot TimeSlot to search the TimeTable for.
     * @return A reference to the TimeSlot from the TimeTable which matches the TimeSlot given. Null if not found.
     */
    public TimeSlot getSlot(TimeSlot timeSlot) {
        return timeTable.getSlot(timeSlot);
    }

    /**
     * Try to find a TimeSlot in the TimeTable for a given slot type and time.
     * Returns the TimeSlot if it exists and null otherwise.
     *
     * @param type The type of slot to search for.
     * @param time The time to search for, as a double (H.MM).
     * @return The TimeSlot, if it exists. Null if it not.
     */
    public TimeSlot getSlot(SlotType type, double time) { return timeTable.getSlot(type, time); }

    /**
     * Try to find a lecture or lab TimeSlot for the given day string and time string.
     *
     * @param day The day to search for, as either MO, TU, or FR.
     * @param time The time to search for, as H:MM or HH:MM.
     * @param isLab Are we looking for a lab slot?
     * @return The TimeSlot if it exists, or null otherwise.
     */
    public TimeSlot getSlot(String day, String time, boolean isLab) { return timeTable.getSlot(day, time, isLab);}

    /**
     * Get all TimeSlots in the TimeTable as an ArrayList.
     * The returned collection is not guaranteed to be ordered.
     *
     * @return All TimeSlots in the TimeTable as an ArrayList.
     */
    public ArrayList<TimeSlot> getAllTimeSlots() { return timeTable.getAllSlots(); }
    
    /**
     * Get the TimeTable object.
     * 
     * @return The aggregated Time Table for the department.
     */
    public TimeTable getTimeTable() { return timeTable; }

    /**
     * Get all lecture TimeSlots in the TimeTable as an ArrayList.
     * The returned collection is not guaranteed to be ordered.
     *
     * @return All lab TimeSlots in the TimeTable as an ArrayList.
     */
    public ArrayList<TimeSlot> getAllLectureSlots() { return timeTable.getAllLectureSlots(); }

    /**
     * Get all lab TimeSlots in the TimeTable as an ArrayList.
     * The returned collection is not guaranteed to be ordered.
     *
     * @return All lab TimeSlots in the TimeTable as an ArrayList.
     */
    public ArrayList<TimeSlot> getAllLabTimeSlots() { return timeTable.getAllLabSlots(); }

    /**
     * Get all lectures in the CourseTable as a HashSet.
     *
     * @return The set of all lectures in the CourseTable.
     */
    public HashSet<Lecture> getAllLectures() { return courseTable.getAllLectures(); }

    /**
     * Get all labs in the CourseTable as a HashSet.
     *
     * @return The set of all labs in the CourseTable.
     */
    public HashSet<Lab> getAllLabs() { return courseTable.getAllLabs(); }

    /**
     * Get all lectures and labs in the CourseTable as a unified HashSet.
     *
     * @return The set of all courses in the CourseTable.
     */
    public HashSet<SlotItem> getAllCourses() { return courseTable.getAllCourses(); }

    /**
     * Retrieves a copy of the partial assignments managed by this Department. This may be somewhat inefficient if
     * there are a large number of partial assignments, so use with caution.
     *
     * @return A copy of the partial assignments managed by this Department.
     */
    public Assignments getPartialAssignments() { return new Assignments(partialAssignments); }

}
