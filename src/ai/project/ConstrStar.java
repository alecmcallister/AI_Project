package ai.project;

import java.util.HashMap;
import java.util.HashSet;

/*
 * Partially assigned solution is passed in and iterated over every single 
 * course/lab that is assigned to a particular slot to see if constr holds
 */

public class ConstrStar {
    public static boolean constr(Assignments partAssignedTable) {
        HashMap<TimeSlot, HashSet<SlotItem>> partAssigned = partAssignedTable.getAllAssignments();
        Assignments tempTable = new Assignments(partAssignedTable.getPenalties(), new TimeTable());
        
//        hm.entrySet().stream().forEach(item -> 
//                  System.out.println(item.getKey() + ": " + item.getValue())
//              );
       
        
        for (TimeSlot timeSlot : partAssigned.keySet()) {
            for (SlotItem slotItem : partAssigned.get(timeSlot)) {
                if (tempTable.constr(timeSlot, slotItem)) {
                    tempTable.addAssignment(timeSlot, slotItem);
                } else {
                    return false;
                }
            }
        }
        return true;
    }
}
