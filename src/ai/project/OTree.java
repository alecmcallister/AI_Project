package ai.project;

import java.util.*;
import java.util.concurrent.*;


/*
 * OTree is a recursive tree in that every node of the OTree is, itself, an Or Tree as well.
 */
public class OTree
{

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

	private Department m_pDept;
	private Assignments m_pAssigned;
	private TimeTable m_pTbl;
	private ArrayList<OTree> m_pLeafs;
	private eSolution m_eSol;
	private boolean m_bInitialized;
	private Random m_pRand;
	private final char MAX_THREADS = 8;

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
			m_pLeafs = new ArrayList<OTree>();
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
	 * Reset the OTree with a new Department and a potential Partial assignment. **Does NOT check Assignment/Unassigned association**
	 * If pAssignedList OR pUnassignedList is null, a fresh assignment is generated.
	 *
	 * @param pDept           New Department to generate Solution for.
	 * @param pAssignedList   Partial Assignment for this department.
	 * @param pUnassignedList Unassigned List for pAssignedList
	 */
	public void resetTree(Department pDept,
	                      Assignments pAssignedList,
	                      Collection<SlotItem> pUnassignedList)
	{
		m_pDept = pDept;
		m_pTbl = pDept.getTimeTable();
		m_pLeafs.clear();
		m_pRand = new Random(System.currentTimeMillis());

		// Default: start from s0
		if (null == pAssignedList || null == pUnassignedList)
		{
			m_pAssigned = new Assignments(m_pTbl);        // Fresh Assignments
			m_pUnassignedList = new ArrayList<>(pDept.getAllCourses());        // Fresh List of all Courses and Labs
			m_eSol = eSolution.UNKNOWN;
		}
		else    // Start from given partial solution.
		{
			m_pAssigned = new Assignments(pAssignedList);
			m_pUnassignedList = new ArrayList<>(pUnassignedList);
			m_eSol = checkGoal();
		}
	}

	/**
	 * Generate a new partial assignment from the same department. **Does NOT check Assignment/Unassigned association**
	 *
	 * @param pAssignedList   Partial assignment.
	 * @param pUnassignedList Unassigned List of Courses U Labs
	 */
	public void resetTree(Assignments pAssignedList,
	                      Collection<SlotItem> pUnassignedList)
	{
		m_pLeafs.clear();
		m_pRand = new Random(System.currentTimeMillis());

		// Default: start from s0
		if (null == pAssignedList || null == pUnassignedList)
		{
			m_pAssigned = new Assignments(m_pTbl);        // Fresh Assignments
			m_pUnassignedList = new ArrayList<>(m_pDept.getAllCourses());        // Fresh List of all Courses and Labs
			m_eSol = eSolution.UNKNOWN;
		}
		else    // Start from given partial solution.
		{
			m_pAssigned = new Assignments(pAssignedList);
			m_pUnassignedList = new ArrayList<>(pUnassignedList);
			m_eSol = checkGoal();
		}
	}

	/**
	 * Generate a Fresh starting point to generate from.
	 */
	public void resetTree()
	{
		m_pLeafs.clear();
		m_pRand = new Random(System.currentTimeMillis());
		m_pAssigned = new Assignments(m_pTbl);        // Fresh Assignments
		m_pUnassignedList = new ArrayList<>(m_pDept.getAllCourses());    // Fresh List of all Courses and Labs
		m_eSol = eSolution.UNKNOWN;
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
		m_pLeafs = new ArrayList<OTree>();
		m_pLeafs.addAll(pInitialNode.m_pLeafs);
		m_pRand = pInitialNode.m_pRand;
		m_pAssigned = new Assignments(pInitialNode.m_pAssigned);
		m_pUnassignedList = new ArrayList<>(pInitialNode.m_pUnassignedList);
		m_eSol = pInitialNode.m_eSol;
		m_bInitialized = true;
	}

	/**
	 * Runs MAX_THREADS Threads on this OTree. Will return the first solution that's found.
	 *
	 * @return An OTree with a solution
	 */
	public OTree genSolutionAsync()
	{
		// Local Variables
		ExecutorService pExecService = Executors.newFixedThreadPool(MAX_THREADS);
		Set<CallableBranch> pCallables = new HashSet<CallableBranch>();
		OTree pReturnSolution = null;

		// Add Callable Elements (Threads)
		for (int i = 0; i < MAX_THREADS; ++i)
		{
			pCallables.add(new CallableBranch(this, i));
		}

		// Await a return solution.
		try
		{
			pReturnSolution = pExecService.invokeAny(pCallables);
		}
		catch (InterruptedException | ExecutionException e1)
		{
			e1.printStackTrace();
		}


		// Shutdown threads.
		try
		{
			pExecService.shutdown();
			pExecService.awaitTermination(5, TimeUnit.SECONDS);
		}
		catch (InterruptedException e2)
		{
			System.err.println("tasks interrupted");
		}
		finally
		{
			pExecService.shutdownNow();
		}

		// Return found solution
		return pReturnSolution;
	}

	/**
	 * Executes the Or-Tree functionality on its current assignment. Evaluates Depthfirst running altern to generate leafs.
	 * leafs are only generated if something can be successfully assigned to a timeslot. If nothing can be assigned, then leaf evaluates to no and returns.
	 * This function recursively checks all altern assignments, if they all evaluate to no then no solution can be found.
	 *
	 * @return Either a Generated Solution that may or may not be valid (check isValid()) or null if OTree wasn't initialized properly.
	 */
	public OTree genSolution(int iID)
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
				return this;
			}

			do    // Evaluate Leaves
			{
				// Grab Next Evaluation
				pReturnTree = m_pLeafs.remove(0);

				// Recurse
				pReturnTree = pReturnTree.genSolution(iID);
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
	 * Generates a list of leaf nodes
	 */
	private void altaltern()
	{
		// Local Variables
//		SlotItem pNewItem;

		ArrayList<Evaluated> pLeafs = new ArrayList<>();
		Assignments pNxtAssign;
		m_pLeafs.clear(); // Clear Leafs at this level to force Depth-first search

		// Check that Unassigned List is not empty, should have evaluated as valid solution before reaching here.
		if (!m_pUnassignedList.isEmpty())
		{
			// Pull Item and get list of possible assignments
//			pNewItem = m_pUnassignedList.remove(m_pRand.nextInt(m_pUnassignedList.size()));

			for (SlotItem pNewItem : m_pUnassignedList)
			{
				pLeafs = m_pAssigned.getViableTimeSlots(m_pTbl, pNewItem);

				// Generate Leafs based on evaluated assignments
				for (Evaluated pEval : pLeafs)
				{
					// New Prob with the Evaluated Assignment
					pNxtAssign = new Assignments(m_pAssigned);
					pNxtAssign.addAssignment(pEval.getTimeSlot(), pNewItem);
					ArrayList<SlotItem> unassCopy = new ArrayList<>();
					unassCopy.addAll(m_pUnassignedList);
					unassCopy.remove(pNewItem);
					// Generate Leaf base on that Prob
					m_pLeafs.add(new OTree(m_pDept, pNxtAssign, unassCopy));
				}
			}

			// No Leafs could be generated? No Valid Solution.
			if (pLeafs.isEmpty())
			{
				m_eSol = eSolution.NO;
				return;
			}
			m_pUnassignedList.clear();

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

	/**
	 * Callable Branch class for Threading with a return value.
	 *
	 * @author James
	 */
	private class CallableBranch implements Callable<OTree>
	{
		// Callable Branch private variables
		private OTree m_pInitialNode;
		private int iID;

		/**
		 * Constructor - Give this a starting Tree to evaluate from
		 *
		 * @param pInitialNode Initial Tree to begin generating Solution from.
		 */
		public CallableBranch(OTree pInitialNode, int iNewID)
		{
			m_pInitialNode = new OTree(pInitialNode);
			iID = iNewID;

			if (null == pInitialNode)
			{
				throw new ExceptionInInitializerError();
			}
		}

		@Override
		/**
		 * Inherited abstract call function, returns an OTree if One could be generated in time.
		 */
		public OTree call() throws Exception
		{
			return m_pInitialNode.genSolution(iID);
		}
	}
}
