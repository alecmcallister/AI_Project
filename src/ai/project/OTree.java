package ai.project;

import java.util.*;
import java.util.concurrent.*;


/**
 * OTree class
 *
 * This class represents an Or-Tree for the Or-Tree based search.
 * It is used to generate solutions, either from partial solutions or de novo.
 *
 * OTree is a recursive tree in that every node of the OTree is, itself, an Or Tree as well.
 */
public class OTree
{

	// Solution Enum
	public enum eSolution
	{
		NO,
		YES,
		UNKNOWN
	}

	/*
	 * Private Variables
	 */
	private ArrayList<SlotItem> m_pUnassignedList;

	private Department m_pDept;
	private Assignments m_pAssigned;
	private TimeTable m_pTbl;
	private ArrayList<OTree> m_pLeafs;
	private eSolution m_eSol;
	private boolean m_bInitialized;
	private Random m_pRand;
	static int bestTry = Integer.MAX_VALUE;

	/********************************************************************************\
	 * Getters/Setters																*
	 \********************************************************************************/
	public Assignments getAssignments()
	{
		return m_pAssigned;
	}

	/**
	 * Constructor - Initializes the Or-Tree with a partial or fresh solution. Will only use partial solution if both AssignedList and UnassignedList are provided.
	 * Otherwise, Or-Tree will initialize with a fresh solution. TODO: If given a partial assignment, could possibly populate an Unassigned list from that.
	 *
	 * @param pDept           A Department is required for generating the Unassigned list and TimeTable.
	 * @param pAssignedList   If null, then Or-Tree will run with a Fresh solution. Otherwise, will run with the given partial solution.
	 * @param pUnassignedList If null, then Or-Tree will run with a Fresh solution. Otherwise, will run with the given partial solution.
	 */
	public OTree(Department pDept, Assignments pAssignedList, Collection<SlotItem> pUnassignedList)
	{
		// Initialize local timetable		
		if ((m_bInitialized = (null != pDept)))
		{
			m_pDept = pDept;
			m_pTbl = pDept.getTimeTable();
			m_pLeafs = new ArrayList<>();
			m_pRand = new Random(System.currentTimeMillis());

			if (pAssignedList == null)
			{
				m_pAssigned = new Assignments(m_pTbl);        // Fresh Assignments
			}
			else
			{
				m_pAssigned = new Assignments(pAssignedList);
			}

			// Default: start from s0
			if (null == pUnassignedList)
			{
				m_pUnassignedList = new ArrayList<>(pDept.getAllCourses());        // Fresh List of all Courses and Labs
				m_eSol = eSolution.UNKNOWN;
			}
			else    // Start from given partial solution.
			{
				m_pUnassignedList = new ArrayList<>(pUnassignedList);
				m_eSol = checkGoal();
			}
		}
	}

	/**
	 * Copy Constructor.
	 *
	 * @param pInitialNode Other Node to copy from.
	 */
	public OTree(OTree pInitialNode)
	{
		m_pDept = pInitialNode.m_pDept;
		m_pTbl = pInitialNode.m_pTbl;
		m_pLeafs = new ArrayList<>();
		m_pLeafs.addAll(pInitialNode.m_pLeafs);
		m_pRand = pInitialNode.m_pRand;
		m_pAssigned = new Assignments(pInitialNode.m_pAssigned);
		m_pUnassignedList = new ArrayList<>(pInitialNode.m_pUnassignedList);
		m_eSol = pInitialNode.m_eSol;
		m_bInitialized = true;
	}

	/**
	 * Executes the Or-Tree functionality on its current assignment. Evaluates Depthfirst running altern to generate leafs.
	 * leafs are only generated if something can be successfully assigned to a timeslot. If nothing can be assigned, then leaf evaluates to no and returns.
	 * This function recursively checks all altern assignments, if they all evaluate to no then no solution can be found.
	 *
	 * @return Either a Generated Solution that may or may not be valid (check isValid()) or null if OTree wasn't initialized properly.
	 */
	public OTree genSolution()
	{
		// Local Variables
		OTree pReturnTree = null;

		if (m_bInitialized && !Thread.currentThread().isInterrupted())
		{
			if (m_eSol == eSolution.YES)
				return this; // Found a valid solution or we didn't

			// Generate Leaves
			this.altern();

			if (m_pLeafs.isEmpty())
			{
				m_eSol = eSolution.NO;

				if (m_pUnassignedList.size() < bestTry)
				{
					bestTry = m_pUnassignedList.size();
					System.out.println("New best found: " + bestTry);
					this.getAssignments().WriteToFile(m_pDept.departmentName + ".txt");
				}
				return this;
			}

			do    // Evaluate Leaves
			{
				// Grab Next Evaluation
				pReturnTree = m_pLeafs.remove(0);

				// Recurse
				pReturnTree = pReturnTree.genSolution();
			}
			while (pReturnTree != null && (eSolution.YES != pReturnTree.m_eSol) && !m_pLeafs.isEmpty());

			if (null != pReturnTree && eSolution.YES != pReturnTree.m_eSol)
			{
				m_eSol = eSolution.NO;
			}
		}

		return pReturnTree;
	}

	private void altern()
	{
		// Local Variables
		m_pLeafs.clear(); // Clear Leafs at this level to force Depth-first search

		// Check that Unassigned List is not empty, should have evaluated as valid solution before reaching here.
		if (m_pUnassignedList.size() > 0)
		{
			// Pull Item and get list of possible assignments
			SlotItem unassignedItem = m_pUnassignedList.remove(m_pRand.nextInt(m_pUnassignedList.size()));
			ArrayList<Evaluated> validSlots = m_pAssigned.getViableTimeSlots(m_pTbl, unassignedItem);

			// Generate Leafs based on evaluated assignments
			for(Evaluated slot : validSlots)
			{
				// New Prob with the Evaluated Assignment
				Assignments pNxtAssign = new Assignments(m_pAssigned);
				pNxtAssign.addAssignment(slot.getTimeSlot(), unassignedItem);

				// Generate Leaf base on that Prob
				m_pLeafs.add( new OTree(m_pDept, pNxtAssign, m_pUnassignedList));
			}
		}
	}

	/**
	 * Checks goal based on definition of Or-Tree Gv(s):
	 * Yes iff:
	 * 1) s = (pr', yes) => This Or-Tree has everything assigned.
	 * 2) s = (pr', ?, b1, ..., bn), ƎiGv(bi) = yes
	 * 3) All leafs of s have either sol-entry no or cannot be processed using Altern
	 *
	 * @return Result of this Or-Tree
	 */
	private eSolution checkGoal()
	{
		return (m_pUnassignedList.isEmpty()) ? eSolution.YES : (m_pLeafs.isEmpty()) ? eSolution.NO : eSolution.UNKNOWN;
	}

	/**
	 * Check to determine if result is a valid solution.
	 *
	 * @return True if the Assignment generated is a valid solution; False otherwise.
	 */
	public boolean isValid()
	{
		return m_bInitialized && (m_eSol == eSolution.YES);
	}
}
