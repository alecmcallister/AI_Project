package ai.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * CourseTable Class
 *
 * This class is to represent the pool of all course components (that is, lectures and labs) available to a given
 * department. The general use is to add all lectures as they are encountered by the parser, followed by all labs.
 *
 * This also contains some helper methods to make it easier to set up constraints on the contain SlotItems.
 *
 * Internally, this is a map of maps of pairs of maps (slightly less complicated than it sounds). The CourseTable is
 * arranged this way to allow the fastest lookup possible for the type of input we'll be receiving from the
 * parser. Specific lookup is necessary because we occasionally need to create mappings between different
 * elements on the table, e.g. parenting a tutorial to a lecture or connecting a pair.
 *
 * An example structure looks something like this:
 *
 *  categoryMap: Map&lt;String, Map&gt;
 *  |- CPSC: Map&lt;int, LectureLabPair&gt;
 *  |   |- 433:
 *  |   |   |- lectures: Map&lt;Lecture Number, Lecture&gt;
 *  |   |   |- labs: Map&lt;Lab Number, Lab&gt;
 *  |   |- 457:
 *  |   |   |- lectures: Map&lt;Lecture Number, Lecture&gt;
 *  |   |   |- labs: Map&lt;Lab Number, Lab&gt;
 *  |- SENG: Map&lt;int, LectureLabPair&gt;
 *  |   |- 301:
 *  |   |   |- lectures: Map&lt;Lecture Number, Lecture&gt;
 *  |   |   |- labs: Map&lt;Lab Number, Lab&gt;
 */

public class CourseTable {

    private HashMap<String, HashMap<Integer, LectureLabPair>> categoryMap;

    public CourseTable() {
        categoryMap = new HashMap<>();
    }

    public void addLecture(String courseName, int courseNum, int lecNum) {
        Lecture newLec = new Lecture(courseName, courseNum, lecNum);
        LectureLabPair pair = getPair(courseName, courseNum);
        pair.lectures.put(lecNum, newLec);
    }

    public void addLab(String courseName, int courseNum, int labNum) {
        Lab newLab = new Lab(courseName, courseNum, labNum);
        LectureLabPair pair = getPair(courseName, courseNum);
        pair.labs.put(labNum, newLab);
    }

    public void addLab(String courseName, int courseNum, int lecNum, int labNum) {
        Lecture parent = getLecture(courseName, courseNum, lecNum);
        if (parent != null) {
            Lab newLab = new Lab(courseName, courseNum, labNum, parent);
            LectureLabPair pair = getPair(courseName, courseNum);
            pair.labs.put(labNum, newLab);
        }
    }

    // ------------- Constraint methods -------------

    public void addIncompatibleLectures(String course1Name, int course1Num, int lec1Num,
                                        String course2Name, int course2Num, int lec2Num) {
        Lecture lecture1 = getLecture(course1Name, course1Num, lec1Num);
        Lecture lecture2 = getLecture(course2Name, course2Num, lec2Num);
        lecture1.addIncompatibility(lecture2);
    }

    public void addIncompatibleLabs(String course1Name, int course1Num, int lab1Num,
                                    String course2Name, int course2Num, int lab2Num) {
        Lab lab1 = getLab(course1Name, course1Num, lab1Num);
        Lab lab2 = getLab(course2Name, course2Num, lab2Num);
        lab1.addIncompatibility(lab2);

    }

    public void addIncompatibleLecLab(String course1Name, int course1Num, int lecNum,
                                      String course2Name, int course2Num, int labNum) {
        Lecture lecture = getLecture(course1Name, course1Num, lecNum);
        Lab lab = getLab(course2Name, course2Num, labNum);
        lecture.addIncompatibility(lab);
    }

    public void addPreferenceLecture(String courseName, int courseNum, int lecNum, TimeSlot time, int preference) {
        Lecture lecture = getLecture(courseName, courseNum, lecNum);
        lecture.addPreference(time, preference);
    }

    public void addPreferenceLab(String courseName, int courseNum, int labNum, TimeSlot time, int preference) {
        Lab lab = getLab(courseName, courseNum, labNum);
        lab.addPreference(time, preference);
    }

    public void addUnwantedLecture(String courseName, int courseNum, int lecNum, TimeSlot time) {
        Lecture lecture = getLecture(courseName, courseNum, lecNum);
        lecture.addUnwanted(time);
    }

    public void addUnwantedLab(String courseName, int courseNum, int labNum, TimeSlot time) {
        Lab lab = getLab(courseName, courseNum, labNum);
        lab.addUnwanted(time);
    }

    public void addPairLectures(String course1Name, int course1Num, int lec1Num,
                                String course2Name, int course2Num, int lec2Num) {
        Lecture lec1 = getLecture(course1Name, course1Num, lec1Num);
        Lecture lec2 = getLecture(course2Name, course2Num, lec2Num);
        lec1.addPair(lec2);
    }

    public void addPairLabs(String course1Name, int course1Num, int lab1Num,
                            String course2Name, int course2Num, int lab2Num) {
        Lab lab1 = getLab(course1Name, course1Num, lab1Num);
        Lab lab2 = getLab(course2Name, course2Num, lab2Num);
        lab1.addPair(lab2);
    }

    public void addPairLecLab(String course1Name, int course1Num, int lecNum,
                              String course2Name, int course2Num, int labNum) {
        Lecture lec = getLecture(course1Name, course1Num, lecNum);
        Lab lab = getLab(course2Name, course2Num, labNum);
        lec.addPair(lab);
    }



    // ------------- Getters -------------

    public Lecture getLecture(String courseName, int courseNum, int lecNum) {
        return getPair(courseName, courseNum).lectures.get(lecNum);
    }

    public Lab getLab(String courseName, int courseNum, int labNum) {
        return getPair(courseName, courseNum).labs.get(labNum);
    }

    public HashSet<Lecture> getAllLectures() {
        HashSet<Lecture> rv = new HashSet<>();
        Collection<HashMap<Integer, LectureLabPair>> courses = categoryMap.values();

        for (HashMap<Integer, LectureLabPair> ele : courses) {
            for (LectureLabPair pair : ele.values()) {
                rv.addAll(pair.lectures.values());
            }
        }

        return rv;
    }

    public HashSet<Lab> getAllLabs() {
        HashSet<Lab> rv = new HashSet<>();
        Collection<HashMap<Integer, LectureLabPair>> courses = categoryMap.values();

        for (HashMap<Integer, LectureLabPair> ele : courses) {
            for (LectureLabPair pair : ele.values()) {
                rv.addAll(pair.labs.values());
            }
        }

        return rv;
    }

    public HashSet<SlotItem> getAllCourses() {
        HashSet<SlotItem> rv = new HashSet<>();
        Collection<HashMap<Integer, LectureLabPair>> courses = categoryMap.values();

        for (HashMap<Integer, LectureLabPair> ele : courses) {
            for (LectureLabPair pair : ele.values()) {
                rv.addAll(pair.lectures.values());
                rv.addAll(pair.labs.values());
            }
        }

        return rv;
    }

    // ------------- Internal Getters -------------

    private HashMap<Integer, LectureLabPair> getCourseMap(String courseName) {
        HashMap<Integer, LectureLabPair> temp = categoryMap.get(courseName);
        if (temp == null) {
            temp = new HashMap<>();
            categoryMap.put(courseName, temp);
        }
        return temp;
    }

    private LectureLabPair getPair(String courseName, int courseNum) {
        HashMap<Integer,LectureLabPair> courseMap = getCourseMap(courseName);
        LectureLabPair pair = courseMap.get(courseNum);
        if (pair == null) {
            pair = new LectureLabPair();
            courseMap.put(courseNum, pair);
        }
        return pair;
    }

    /**
     * Simple struct-like internal class to create a pair of Lecture and Lab sets.
     */
    private class LectureLabPair {
        private HashMap<Integer, Lecture> lectures = new HashMap<>();
        private HashMap<Integer, Lab> labs = new HashMap<>();
    }
}
