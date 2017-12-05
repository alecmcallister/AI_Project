package ai.project;

import java.util.*;

public class OTreeOptimized {
    protected static boolean solutionFound[];
    
    public OTreeOptimized (Department department, Assignments assignments, Collection<SlotItem> unassigned) {
        System.out.println("Initializing OTreeOptimized...");
        int maxThreads = Runtime.getRuntime().availableProcessors(); // set number of threads to available processors
        solutionFound = new boolean[maxThreads]; // create a solution array for threads to scheck
        Arrays.fill(solutionFound, false); // initiate solution array to false
        
        // start max number of threads
        for (int i=0; i < maxThreads; i++) {
            Thread t = new Thread(new OTreeThread(i));
            t.start();
        }
    }
    
    private class OTreeThread implements Runnable {
        int myNumber;
        boolean lost = false;
        OTreeThread(int num) {
            myNumber = num;
        }
        public void run() {
            for (long i = 0; i < 1000000000; i++) {
                for (boolean s : OTreeOptimized.solutionFound){
                    if (s == true) { 
                        System.out.println("Thread " + myNumber + " lost");
                        lost = true;
                        break; 
                    }
                }
                if (lost) { break; }
            }
            OTreeOptimized.solutionFound[myNumber] = true;
            if (!lost) { System.out.println("Thread " + myNumber + " won"); }
        }
    }
}
