package ai.project;

import java.util.ArrayList;
import java.util.HashSet;

/*
* Course
* Type: cpsc
* Num: 433
* Lec: 01
*
* Lab
* Tut: 01
* Association: Type: cpsc
*              Num: 433
*              Lec: null if all Lec slots
* */

public class Department {
    private String departmentName;

    private TimeTable timeTable;
    private CourseTable courseTable;

    private ArrayList<String> partials;


    /**
     * Base constructor. Sets name and initializes tables.
     *
     * @param departmentName The name of the new department.
     */
    public Department(String departmentName) {
        this.departmentName = departmentName;
        timeTable = new TimeTable();
        courseTable = new CourseTable();
    }


    // ------------- Table Adders -------------

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


    // ------------- Table Getters -------------

    public TimeSlot getSlot(TimePair time) { return timeTable.getSlot(time); }

    public TimeSlot getSlot(TimeSlot timeSlot) {
        return timeTable.getSlot(timeSlot);
    }

    public TimeSlot getSlot(SlotType type, double time) { return timeTable.getSlot(type, time); }

    public TimeSlot getSlot(String day, String time, boolean isLab) { return timeTable.getSlot(day, time, isLab);}

    public ArrayList<TimeSlot> getAllTimeSlots() { return timeTable.getAllSlots(); }

    public ArrayList<TimeSlot> getAllLabTimeSlots() { return timeTable.getAllLabSlots(); }

    public ArrayList<TimeSlot> getAllLectureSlots() { return timeTable.getAllLectureSlots(); }

    public HashSet<Lecture> getAllLectures() { return courseTable.getAllLectures(); }

    public HashSet<Lab> getAllLabs() { return courseTable.getAllLabs(); }

    public HashSet<SlotItem> getAllCourses() { return courseTable.getAllCourses(); }

}
