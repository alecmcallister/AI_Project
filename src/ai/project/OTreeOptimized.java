package ai.project;

import java.util.*;
import java.io.*;

public class OTreeOptimized {
    
    // helper class to hold partial solution and unassigned courses+labs
    protected class Prob {
        
        public Assignments assignments;
        public Collection<SlotItem> unassigned;
        
        Prob(Assignments assignents, Collection<SlotItem> unassigned) {
            
            this.assignments = assignments;
            this.unassigned = unassigned;
        }
    }
        
    // Solution Enum
	public enum eSolution
	{
		NO,
		YES,
		UNKNOWN;
	}
    
    protected static eSolution solutionFound[];
    
    public OTreeOptimized (Department department, Assignments assignments, Collection<SlotItem> unassigned) {
        
        System.out.println("Initializing OTreeOptimized...");
        int maxThreads = Runtime.getRuntime().availableProcessors(); // set number of threads to available processors
        solutionFound = new eSolution[maxThreads]; // create a solution array for threads to scheck
        Arrays.fill(solutionFound, eSolution.UNKNOWN); // initiate solution array to false
        
        // start max number of threads
        for (int i=0; i < maxThreads; i++) {
//            Thread t = new Thread(new OTreeThread(i));
//            t.start();
        }
    }
    
    
    
    private class OTreeThread implements Runnable {
        
        private int currentDepth = 0;
        private int myNumber;
        private boolean lost = false;
        
        private ArrayList<SlotItem> m_pUnassignedList;
        private Department m_pDept;
        private Assignments m_pAssigned;
        private TimeTable m_pTbl;
        private Queue<OTree> m_pLeafs;
        private OTree.eSolution m_eSol;
        private boolean m_bInitialized;
        private Random m_pRand;
        
        public OTreeThread(int num) {
            myNumber = num;
        }
        
        // static function to generate leaves for OR-tree
        private Stack<Prob> altern(Stack<Prob> currentProbSet, String fileName, TimeTable timeTable) {
            Prob prob = currentProbSet.pop();
            Assignments assignments = prob.assignments;
            Collection<SlotItem> unassigned = prob.unassigned;
            boolean probSetSaved = false;
            Stack<Prob> newProbSet = new Stack<Prob>();        
            Stack<Prob> tempProbSet = new Stack<Prob>();


            // store the serialized object to avoid memory overflow
            if (!currentProbSet.empty() && fileName != "") {
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
                    oos.writeObject(currentProbSet);
                    oos.close();
                    probSetSaved = true;
                } 
                catch (IOException ex) {
                    System.out.println("Unable to save previous Prob Set to " + fileName);
                    System.out.println("Error:\n" + ex.getMessage());
                }
            }
            // get course/lab to assign
            SlotItem toAssign = unassigned.iterator().next();
            // remove it from the unassigned list
            unassigned.remove(toAssign);
            // get possible time slots to assign it to
            TreeSet<Evaluated> evaluatedTimeSlots = assignments.getViableTimeSlots(timeTable, toAssign);

            // assign it to all possible time slots and create temp prob set
            // need temp to reverse it in output
            for (Evaluated timeSlot : evaluatedTimeSlots) {
                assignments.addAssignment(timeSlot.getTimeSlot(), toAssign);
                tempProbSet.push(new Prob(assignments, unassigned));
            }

            // reverse tempProbSet and store into newProbSet for correct output order
            while (!tempProbSet.isEmpty()) {
                newProbSet.push(tempProbSet.pop());
            }

            return newProbSet;
        }
        
        public void run() {
            
//            for (long i = 0; i < 1000000000; i++) {
//                for (boolean s : OTreeOptimized.solutionFound){
//                    if (s == true) { 
//                        System.out.println("Thread " + myNumber + " lost");
//                        lost = true;
//                        break; 
//                    }
//                }
//                if (lost) { break; }
//            }
//            OTreeOptimized.solutionFound[myNumber] = true;
            if (!lost) { System.out.println("Thread " + myNumber + " won"); }
        }
        
        /**
        * Generates a list of leaf nodes
        */
        private void altern()
        {
            // Local Variables
            SlotItem pNewItem;
            TreeSet<Evaluated> pLeafs;
            Assignments pNxtAssign;
            m_pLeafs.clear(); // Clear Leafs at this level to force Depth-first search

            // Check that Unassigned List is not empty, should have evaluated as valid solution before reaching here.
            if ( !m_pUnassignedList.isEmpty() )
            {
                // Pull Item and get list of possible assignments
                pNewItem = m_pUnassignedList.remove( m_pRand.nextInt(m_pUnassignedList.size()) );
                pLeafs = m_pAssigned.getViableTimeSlots(m_pTbl, pNewItem);

                //System.out.println( "\tNum Leafs assigned: " + pLeafs.size() );

                // Generate Leafs based on evaluated assignments
                for( Evaluated pEval : pLeafs )
                {
                    // New Prob with the Evaluated Assignment
                    pNxtAssign = new Assignments( m_pAssigned );
                    pNxtAssign.addAssignment(pEval.getTimeSlot(), pNewItem);

                    // Generate Leaf base on that Prob
                    m_pLeafs.add( new OTree( m_pDept, pNxtAssign, m_pUnassignedList ) );
                }
            }
        }

        /**
         * Checks goal based on definition of Or-Tree Gv(s):
         * 			Yes iff:
         * 				1) s = (pr', yes) => This Or-Tree has everything assigned.
         * 				2) s = (pr', ?, b1, ..., bn), ƎiGv(bi) = yes
         * 				3) All leafs of s have either sol-entry no or cannot be processed using Altern
         * @return Result of this Or-Tree
         */
        private OTree.eSolution checkGoal()
        {
            return m_pUnassignedList.isEmpty() ? OTree.eSolution.YES : OTree.eSolution.UNKNOWN;
        }

        /**
         * Check to determine if result is a valid solution.
         * 
         * @return True if the Assignment generated is a valid solution; False otherwise.
         */
        public boolean isValid( )
        {
            return m_bInitialized && (m_eSol == OTree.eSolution.YES);
        }
    }
}
