import java.util.ArrayList;

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
    private ArrayList<String> courseSlots;
    private ArrayList<String> labSlots;
    private ArrayList<String> courses;
    private ArrayList<String> labs;
    private ArrayList<String> notCompatible;
    private ArrayList<String> unwanted;
    private ArrayList<String> preferences;
    private ArrayList<String> pairs;
    private ArrayList<String> partials;

    public Department(String departmentName,
            ArrayList<String> courseSlots,
            ArrayList<String> labSlots,
            ArrayList<String> courses,
            ArrayList<String> labs,
            ArrayList<String> notCompatible,
            ArrayList<String> unwanted,
            ArrayList<String> preferences,
            ArrayList<String> pairs,
            ArrayList<String> partials) {
        this.departmentName = departmentName;
        this.courseSlots = courseSlots;
        this.labSlots = labSlots;
        this.courses = courses;
        this.labs = labs;
        this.notCompatible = notCompatible;
        this.unwanted = unwanted;
        this.preferences = preferences;
        this.pairs = pairs;
        this.partials = partials;
    }

    public void getCourseLabs() {

    }
    public void getPreference() {

    }
    public void getIncompatibles() {

    }
    public void getUnwanted() {

    }
    public void getPair() {

    }
}
