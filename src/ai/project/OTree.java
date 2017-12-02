package ai.project;

import java.util.*;

/*
 * OTree is a recursive tree in that every node of the OTree is, itself, an Or Tree as well.
 */
public class OTree {
	
	// Solution Enum
	public enum eSolution
	{
		NO((char)0),
		YES((char)1),
		UNKNOWN((char)-1),
		NOSOLUTION((char)-2);
		
		private final char m_cValue;
		eSolution( char cVal ) { m_cValue = cVal; }
	}
	
	/*
	 * Private Variables
	 */
	private ArrayList<SlotItem> m_pUnassignedList;
	private Assignments m_pAssigned;
	private TimeTable m_pTbl;
	private Queue<OTree> m_pLeafs;
	private eSolution m_eSol;
	
	/********************************************************************************\
	 * Getters/Setters																*
	\********************************************************************************/
	public Assignments getAssignments( ) { return m_pAssigned; }
	public eSolution getSolution() { return m_eSol; }
	
	/**
	 * Constructor - Initializes the Or-Tree with a partial or fresh solution. Will only use partial solution if both AssignedList and UnassignedList are provided.
	 * 	Otherwise, Or-Tree will initialize with a fresh solution. TODO: If given a partial assignment, could possibly populate an Unassigned list from that.
	 * @param pAssignedList If null, then Or-Tree will run with a Fresh solution. Otherwise, will run with the given partial solution.
	 * @param pUnassignedList If null, then Or-Tree will run with a Fresh solution. Otherwise, will run with the given partial solution.
	 */
	public OTree ( Assignments pAssignedList, 
				   ArrayList<SlotItem> pUnassignedList )
	{
		// Initialize local timetable
		m_pTbl = new TimeTable();
		m_pTbl.initializeTable();
		
		// Default: start from s0
		if( null == pAssignedList || null == pUnassignedList )
		{
			m_pAssigned = new Assignments(new Penalties(0,0,0,0), m_pTbl);		// Fresh Assignments TODO: What should Penalties be set to?
			m_pUnassignedList = new ArrayList<>(); 	// Fresh List of all Courses and Labs (TODO: pull from Department class)
		}
		else	// Start from given partial solution.
		{
			m_pAssigned = new Assignments( pAssignedList );
			m_pUnassignedList = new ArrayList<>( pUnassignedList );
			m_eSol = checkGoal( );
		}
	}
	/**
	 * Executes the Or-Tree functionality on its current assignment. Evaluates Depthfirst running altern to generate leafs.
	 * leafs are only generated if something can be successfully assigned to a timeslot. If nothing can be assigned, then leaf evaluates to no and returns.
	 * This function recursively checks all altern assignments, if they all evaluate to no then no solution can be found.
	 * 
	 * @return a Generated, possibly complete assignment. Check OTree.getSolution() == eSolution.YES.
	 */
	public OTree genSolution( )
	{
		// Local Variables
		OTree pReturnTree;
		
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
		} while( pReturnTree.getSolution() != eSolution.YES && !m_pLeafs.isEmpty() );
		
		// No Leafs could be evaluated to YES? No Solution.
		if( pReturnTree.getSolution() != eSolution.YES )
			pReturnTree.m_eSol = eSolution.NOSOLUTION;
		
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
				m_pLeafs.add( new OTree( pNxtAssign, m_pUnassignedList ) );
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
	
}
