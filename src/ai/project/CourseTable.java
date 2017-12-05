package ai.project;

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

    /**
     * Default constructor just initializes the category map (the top-level map).
     */
    public CourseTable() {
        categoryMap = new HashMap<>();
    }

    /**
     * Add a new lecture to the CourseTable.
     * New courses are initialized without any special constraints.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG)
     * @param courseNum The number of the course (e.g. 201, 433)
     * @param lecNum The section number of the lecture (e.g. 1, 2, 99)
     */
    public void addLecture(String courseName, int courseNum, int lecNum) {
        Lecture newLec = new Lecture(courseName, courseNum, lecNum);
        LectureLabPair pair = getPair(courseName, courseNum);
        pair.lectures.put(lecNum, newLec);
    }

    /**
     * Adds a new lab/tutorial to the CourseTable. This method is specifically for labs that do not have a parent;
     * i.e. they are not associated with any particular lecture section.
     *
     * New courses are initialized without any special constraints.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG)
     * @param courseNum The number of the course (e.g. 201, 433)
     * @param labNum The section number of the lab/tutorial (e.g. 1, 2, 99)
     */
    public void addLab(String courseName, int courseNum, int labNum) {
        Lab newLab = new Lab(courseName, courseNum, labNum);
        LectureLabPair pair = getPair(courseName, courseNum);
        pair.labs.put(labNum, newLab);
    }

    /**
     * Adds a new lab/tutorial to the CourseTable that has a parent lecture; i.e. it is associated with a particular
     * lecture section.
     *
     * The given lecture section must already exist in the CourseTable, or else this will not result in any change.
     * New courses are initialized without any special constraints.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG)
     * @param courseNum The number of the course (e.g. 201, 433)
     * @param lecNum The section number of the parent lecture (e.g. 1, 2, 99)
     * @param labNum The section number of the lab/tutorial (e.g. 1, 2, 99)
     */
    public void addLab(String courseName, int courseNum, int lecNum, int labNum) {
        Lecture parent = getLecture(courseName, courseNum, lecNum);
        if (parent != null) {
            Lab newLab = new Lab(courseName, courseNum, labNum, parent);
            LectureLabPair pair = getPair(courseName, courseNum);
            pair.labs.put(labNum, newLab);
        }
    }

    // ------------- Constraint methods -------------

    /**
     * Adds a Not-Compatible constraint between two lectures already in the CourseTable.
     * If either of the lectures is not already in the CourseTable, this will not result in a change.
     *
     * @param course1Name The name of the first course in the non-compatibility pair (e.g. CPSC, SENG).
     * @param course1Num The number of the first course in the non-compatibility pair (e.g. 203, 433).
     * @param lec1Num The section number (lec #) for the first lecture.
     * @param course2Name The name of the second course in the non-compatibility pair (e.g. CPSC, SENG).
     * @param course2Num The number of the second course in the non-compatibility pair (e.g. 203, 433).
     * @param lec2Num The section number (lec #) for the second lecture.
     */
    public void addIncompatibleLectures(String course1Name, int course1Num, int lec1Num,
                                        String course2Name, int course2Num, int lec2Num) {
        Lecture lecture1 = getLecture(course1Name, course1Num, lec1Num);
        Lecture lecture2 = getLecture(course2Name, course2Num, lec2Num);
        if (lecture1 != null && lecture2 != null)
            lecture1.addIncompatibility(lecture2);
    }

    /**
     * Adds a Not-Compatible constraint between two labs already in the CourseTable.
     * If either of the labs is not already in the CourseTable, this will not result in a change.
     *
     * @param course1Name The name of the first course in the non-compatibility pair (e.g. CPSC, SENG).
     * @param course1Num The number of the first course in the non-compatibility pair (e.g. 203, 433).
     * @param lab1Num The section number (tut #) for the first lab.
     * @param course2Name The name of the second course in the non-compatibility pair (e.g. CPSC, SENG).
     * @param course2Num The number of the second course in the non-compatibility pair (e.g. 203, 433).
     * @param lab2Num The section number (tut #) for the second lab.
     */
    public void addIncompatibleLabs(String course1Name, int course1Num, int lab1Num,
                                    String course2Name, int course2Num, int lab2Num) {
        Lab lab1 = getLab(course1Name, course1Num, lab1Num);
        Lab lab2 = getLab(course2Name, course2Num, lab2Num);
        if (lab1 != null && lab2 != null)
            lab1.addIncompatibility(lab2);
    }

    /**
     * Adds a Not-Compatible constraint between a lecture and a lab already in the CourseTable.
     * If either of the lab or lecture is not already in the CourseTable, this will not result in a change.
     *
     * @param course1Name The name of the first course in the non-compatibility pair (e.g. CPSC, SENG).
     * @param course1Num The number of the first course in the non-compatibility pair (e.g. 203, 433).
     * @param lecNum The section number (lec #) for the lecture.
     * @param course2Name The name of the second course in the non-compatibility pair (e.g. CPSC, SENG).
     * @param course2Num The number of the second course in the non-compatibility pair (e.g. 203, 433).
     * @param labNum The section number (tut #) for the lab.
     */
    public void addIncompatibleLecLab(String course1Name, int course1Num, int lecNum,
                                      String course2Name, int course2Num, int labNum) {
        Lecture lecture = getLecture(course1Name, course1Num, lecNum);
        Lab lab = getLab(course2Name, course2Num, labNum);
        if (lecture != null && lab != null)
            lecture.addIncompatibility(lab);
    }

    /**
     * Adds a Preference constraint to a lecture already in the CourseTable.
     * If the lecture is not already in the CourseTable, this will not result in a change.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG).
     * @param courseNum The number of the course (e.g. 203, 433).
     * @param lecNum The section number (lec #) for the lecture.
     * @param time The preferred TimeSlot.
     * @param preference The preference value, as an integer.
     */
    public void addPreferenceLecture(String courseName, int courseNum, int lecNum, TimeSlot time, int preference) {
        Lecture lecture = getLecture(courseName, courseNum, lecNum);
        if (lecture != null)
            lecture.addPreference(time, preference);
    }

    /**
     * Adds a Preference constraint to a lab already in the CourseTable.
     * If the lab is not already in the CourseTable, this will not result in a change.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG).
     * @param courseNum The number of the course (e.g. 203, 433).
     * @param labNum The section number (tut #) for the lab.
     * @param time The preferred TimeSlot.
     * @param preference The preference value, as an integer.
     */
    public void addPreferenceLab(String courseName, int courseNum, int labNum, TimeSlot time, int preference) {
        Lab lab = getLab(courseName, courseNum, labNum);
        if (lab != null)
            lab.addPreference(time, preference);
    }

    /**
     * Adds an Unwanted constraint to a lecture already in the CourseTable.
     * If the lecture is not already in the CourseTable, this will not result in a change.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG).
     * @param courseNum The number of the course (e.g. 203, 433).
     * @param lecNum The section number (lec #) for the lecture.
     * @param time The unwanted TimeSlot.
     */
    public void addUnwantedLecture(String courseName, int courseNum, int lecNum, TimeSlot time) {
        Lecture lecture = getLecture(courseName, courseNum, lecNum);
        if (lecture != null)
            lecture.addUnwanted(time);
    }

    /**
     * Adds an Unwanted constraint to a lab already in the CourseTable.
     * If the lab is not already in the CourseTable, this will not result in a change.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG).
     * @param courseNum The number of the course (e.g. 203, 433).
     * @param labNum The section number (tut #) for the lab.
     * @param time The unwanted TimeSlot.
     */
    public void addUnwantedLab(String courseName, int courseNum, int labNum, TimeSlot time) {
        Lab lab = getLab(courseName, courseNum, labNum);
        if (lab != null)
            lab.addUnwanted(time);
    }

    /**
     * Adds a Pair constraint between two lectures already in the CourseTable.
     * If either of the lectures is not already in the CourseTable, this will not result in a change.
     *
     * @param course1Name The name of the first course in the pair (e.g. CPSC, SENG).
     * @param course1Num The number of the first course in the pair (e.g. 203, 433).
     * @param lec1Num The section number (lec #) for the first lecture.
     * @param course2Name The name of the second course in the pair (e.g. CPSC, SENG).
     * @param course2Num The number of the second course in the pair (e.g. 203, 433).
     * @param lec2Num The section number (lec #) for the second lecture.
     */
    public void addPairLectures(String course1Name, int course1Num, int lec1Num,
                                String course2Name, int course2Num, int lec2Num) {
        Lecture lec1 = getLecture(course1Name, course1Num, lec1Num);
        Lecture lec2 = getLecture(course2Name, course2Num, lec2Num);
        if (lec1 != null && lec2 != null)
            lec1.addPair(lec2);
    }

    /**
     * Adds a Pair constraint between two labs already in the CourseTable.
     * If either of the labs is not already in the CourseTable, this will not result in a change.
     *
     * @param course1Name The name of the first course in the pair (e.g. CPSC, SENG).
     * @param course1Num The number of the first course in the pair (e.g. 203, 433).
     * @param lab1Num The section number (tut #) for the first lab.
     * @param course2Name The name of the second course in the pair (e.g. CPSC, SENG).
     * @param course2Num The number of the second course in the pair (e.g. 203, 433).
     * @param lab2Num The section number (tut #) for the second lab.
     */
    public void addPairLabs(String course1Name, int course1Num, int lab1Num,
                            String course2Name, int course2Num, int lab2Num) {
        Lab lab1 = getLab(course1Name, course1Num, lab1Num);
        Lab lab2 = getLab(course2Name, course2Num, lab2Num);
        if (lab1 != null && lab2 != null)
            lab1.addPair(lab2);
    }

    /**
     * Adds a Not-Compatible constraint between a lecture and a lab already in the CourseTable.
     * If either of the lab or lecture is not already in the CourseTable, this will not result in a change.
     *
     * @param course1Name The name of the first course in the pair (e.g. CPSC, SENG).
     * @param course1Num The number of the first course in the pair (e.g. 203, 433).
     * @param lecNum The section number (lec #) for the lecture.
     * @param course2Name The name of the second course in the pair (e.g. CPSC, SENG).
     * @param course2Num The number of the second course in the pair (e.g. 203, 433).
     * @param labNum The section number (tut #) for the lab.
     */
    public void addPairLecLab(String course1Name, int course1Num, int lecNum,
                              String course2Name, int course2Num, int labNum) {
        Lecture lec = getLecture(course1Name, course1Num, lecNum);
        Lab lab = getLab(course2Name, course2Num, labNum);
        if (lec != null && lab != null)
            lec.addPair(lab);
    }



    // ------------- Getters -------------

    /**
     * Gets a Lecture from the CourseTable.
     * May return null if the Lecture is not in the table.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG).
     * @param courseNum The number of the course (e.g. 203, 433).
     * @param lecNum The section number (lec #) for the lecture.
     * @return A reference to the Lecture in the table, or null if nothing in the table matches the params.
     */
    public Lecture getLecture(String courseName, int courseNum, int lecNum) {
        return getPair(courseName, courseNum).lectures.get(lecNum);
    }

    /**
     * Gets a Lab from the CourseTable.
     * May return null if the Lab is not in the table.
     *
     * @param courseName The name of the course (e.g. CPSC, SENG).
     * @param courseNum The number of the course (e.g. 203, 433).
     * @param labNum The section number (tut #) for the lab.
     * @return A reference to the Lab in the table, or null if nothing in the table matches the params.
     */
    public Lab getLab(String courseName, int courseNum, int labNum) {
        return getPair(courseName, courseNum).labs.get(labNum);
    }

    /**
     * Gets the set of all Lectures in the CourseTable.
     * There is no guarantee on sorting for the returned set; Lectures may be in any order, but are guaranteed to
     * be unique.
     *
     * @return A set of all Lectures in the CourseTable.
     */
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

    /**
     * Gets the set of all Labs in the CourseTable.
     * There is no guarantee on sorting for the returned set; Labs may be in any order, but are guaranteed to
     * be unique.
     *
     * @return A set of all Labs in the CourseTable.
     */
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


    /**
     * Gets the set of all courses (SlotItems) in the CourseTable, i.e. a set of all Lectures and Labs together.
     * There is no guarantee on sorting for the returned set; SlotItems may be in any order, but are guaranteed to
     * be unique.
     *
     * @return A set of all SlotItems in the CourseTable (Labs U Lectures)
     */
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

    /**
     * Get the map of all numbered courses within a given set of courses.
     * For example, we can get all maps corresponding to "CPSC", which may include a map for 201, 443, etc.
     * If there is no map for this course name yet, we make one here and return it.
     *
     * @param courseName The name of the course (e.g. CPSC or SENG).
     * @return The map of LectureLabPairs mapped to all course numbers for the given course name.
     */
    private HashMap<Integer, LectureLabPair> getCourseMap(String courseName) {
        HashMap<Integer, LectureLabPair> temp = categoryMap.get(courseName);
        if (temp == null) {
            temp = new HashMap<>();
            categoryMap.put(courseName, temp);
        }
        return temp;
    }

    /**
     * Get the LectureLabPair corresponding to the given course name + course number.
     * For example, we can get the pair of all lectures and labs associated with "CPSC 443".
     * If the Pair does not exist yet, we create one here and return it.
     *
     * @param courseName The name of the course (e.g. CPSC or SENG).
     * @param courseNum The number of the course (e.g. 201, 443).
     * @return The LectureLabPair corresponding to this course.
     */
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

    private class LabSecPair {
        public int lecNum;
        public int labNum;

        LabSecPair(int lecNum, int labNum) {
            this.lecNum = lecNum;
            this.labNum = labNum;
        }

        LabSecPair(int labNum) {
            this.labNum = labNum;
            lecNum = -1;
        }

        @Override
        public boolean equals(Object other) {
            // Self comparison
            if (this == other) return true;

            // Other LabSecPair comparison. Check if both slots are the same.
            if (other instanceof LabSecPair) {
                return ((this.lecNum == ((LabSecPair) other).lecNum)
                        && (this.labNum == ((LabSecPair) other).labNum));
            }

            // Other is not a LabSecPair
            return false;
        }
    }
}
