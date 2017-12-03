package ai.project;

import java.util.*;

/*
 * OTree is a recursive tree in that every node of the OTree is, itself, an Or Tree as well.
 */
public class OTree {
	
	// Solution Enum
	public enum eSolution
	{
		NO,
		YES,
		UNKNOWN;
	}
	
	/*
	 * Private Variables
	 */
	private ArrayList<SlotItem> m_pUnassignedList;
	private Department 			m_pDept;
	private Assignments 		m_pAssigned;
	private TimeTable 			m_pTbl;
	private Queue<OTree> 		m_pLeafs;
	private eSolution 			m_eSol;
	private boolean 			m_bInitialized;
	
	/********************************************************************************\
	 * Getters/Setters																*
	\********************************************************************************/
	public Assignments getAssignments( ) { return m_pAssigned; }
	
	/**
	 * Constructor - Initializes the Or-Tree with a partial or fresh solution. Will only use partial solution if both AssignedList and UnassignedList are provided.
	 * 	Otherwise, Or-Tree will initialize with a fresh solution. TODO: If given a partial assignment, could possibly populate an Unassigned list from that.
	 * @param pDept A Department is required for generating the Unassigned list and TimeTable.
	 * @param pAssignedList If null, then Or-Tree will run with a Fresh solution. Otherwise, will run with the given partial solution.
	 * @param pUnassignedList If null, then Or-Tree will run with a Fresh solution. Otherwise, will run with the given partial solution.
	 */
	public OTree ( Department pDept,
				   Assignments pAssignedList, 
				   Collection<SlotItem> pUnassignedList )
	{
		// Initialize local timetable		
		if( ( m_bInitialized = (null != pDept) ) )
		{	
			m_pDept = pDept;
			m_pTbl = pDept.getTimeTable();
			m_pLeafs = new ArrayDeque<OTree>();
			
			// Default: start from s0
			if( null == pAssignedList || null == pUnassignedList )
			{
				m_pAssigned = new Assignments(new Penalties(0,0,0,0), m_pTbl);		// Fresh Assignments TODO: What should Penalties be set to?
				m_pUnassignedList = new ArrayList<>( pDept.getAllCourses() ); 		// Fresh List of all Courses and Labs
			}
			else	// Start from given partial solution.
			{
				m_pAssigned = new Assignments( pAssignedList );
				m_pUnassignedList = new ArrayList<>( pUnassignedList );
				m_eSol = checkGoal( );
			}
		}
	}
	/**
	 * Executes the Or-Tree functionality on its current assignment. Evaluates Depthfirst running altern to generate leafs.
	 * leafs are only generated if something can be successfully assigned to a timeslot. If nothing can be assigned, then leaf evaluates to no and returns.
	 * This function recursively checks all altern assignments, if they all evaluate to no then no solution can be found.
	 * 
	 * @return Either a Generated Solution that may or may not be valid (check isValid()) or null if OTree wasn't initialized properly.
	 */
	public OTree genSolution( )
	{
		// Local Variables
		OTree pReturnTree = null;
		
		if( m_bInitialized )
		{
			if( m_eSol == eSolution.YES )
				return this; // Found a valid solution
		
			// Generate Leafs
			this.altern();
			
			// No Leafs could be generated? No Valid Solution.
			if( m_pLeafs.isEmpty() )
			{
				m_eSol = eSolution.NO;
				return this;
			}
			
			do	// Evaluate Leafs
			{
				// Grab Next Evaluation
				OTree pNextEval = m_pLeafs.remove();
				
				// Recurse
				pReturnTree = pNextEval.genSolution();
			} while( (pReturnTree.m_eSol != eSolution.YES) && !m_pLeafs.isEmpty() );
		}
		
		// Return Result.
		return pReturnTree;
	}
	
	/**
	 * Generates a list of leaf nodes
	 */
	private void altern()
	{
		// Local Variables
		Random pRand;
		SlotItem pNewItem;
		TreeSet<Evaluated> pLeafs;
		Assignments pNxtAssign;
		m_pLeafs.clear(); // Clear Leafs at this level to force Depth-first search
		
		// Check that Unassigned List is not empty, should have evaluated as valid solution before reaching here.
		if( !m_pUnassignedList.isEmpty() )
		{
			// Pull Item and get list of possible assignments
			pRand = new Random();
			pNewItem = m_pUnassignedList.remove( pRand.nextInt(m_pUnassignedList.size()) );
			pLeafs = m_pAssigned.assign(m_pTbl, pNewItem);
			 
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
	private eSolution checkGoal()
	{
		return m_pUnassignedList.isEmpty() ? eSolution.YES : eSolution.UNKNOWN;
	}
	
	/**
	 * Check to determine if result is a valid solution.
	 * 
	 * @return True if the Assignment generated is a valid solution; False otherwise.
	 */
	public boolean isValid( )
	{
		return m_bInitialized && (m_eSol == eSolution.YES);
	}	
}
