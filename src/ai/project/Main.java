/**
 * Created by dre3k on 2017-10-24.
 */
package ai.project;

import java.io.*;
import java.util.ArrayList;


public class Main {
    public static void main(String args[]) {
        String fileName1 = System.getProperty("user.dir") + "\\deptinst1.txt";
        String fileName2 = System.getProperty("user.dir") + "\\deptinst2.txt";
        readFile(fileName1);
        readFile(fileName2);

    }
    public static void readFile(String fileName) {
        String line = null;
        int currentInfo = 0;

        String deptName = "";
        ArrayList<String> courseSlots = new ArrayList<String>();
        ArrayList<String> labSlots = new ArrayList<String>();
        ArrayList<String> courses = new ArrayList<String>();
        ArrayList<String> labs = new ArrayList<String>();
        ArrayList<String> notCompatible = new ArrayList<String>();
        ArrayList<String> unwanted = new ArrayList<String>();
        ArrayList<String> preferences = new ArrayList<String>();
        ArrayList<String> pairs = new ArrayList<String>();
        ArrayList<String> partials = new ArrayList<String>();


        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                if (line.equals("Name:")) {
                    deptName = bufferedReader.readLine();
                }
                if (line.equals("Course Slots:")) {
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

                if (currentInfo == 1) {
                    courseSlots.add(line);
                } else if (currentInfo == 2) {
                    labSlots.add(line);
                } else if (currentInfo == 3) {
                    courses.add(line);
                } else if (currentInfo == 4) {
                    labs.add(line);
                } else if (currentInfo == 5) {
                    notCompatible.add(line);
                } else if (currentInfo == 6) {
                    unwanted.add(line);
                } else if (currentInfo == 7) {
                    preferences.add(line);
                } else if (currentInfo == 8) {
                    pairs.add(line);
                } else if (currentInfo == 9) {
                    partials.add(line);
                }
            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file, file not found '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }
        
        System.out.println("NOT COMPATIBLE COURSES ARE");
        for (String course : notCompatible) {
            System.out.print(course + ";\n");
        }
        System.out.println();
    }
}
