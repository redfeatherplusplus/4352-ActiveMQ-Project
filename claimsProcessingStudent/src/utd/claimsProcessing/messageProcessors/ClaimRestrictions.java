package utd.claimsProcessing.messageProcessors;

import java.util.HashMap;
import java.util.Map;

/**
 * This class maintains claims restrictions for each claim.
 * Note that a claim of '0' is unrestricted. 
 */
public class ClaimRestrictions {
	//list of claim limits for each procedure
	public final static int GP101 = 0;
	public final static int GP102 = 0;
	public final static int GP103 = 0;

	public final static int R201 = 0;
	public final static int R202 = 0;
	public final static int R203 = 0;
	
	public final static int D401 = 0;
	public final static int D402 = 0;
	public final static int D403 = 0;
	
	public final static int O601 = 0;
	public final static int O602 = 0;
	public final static int O603 = 0;
	
	//map from procedures to procedureLimits
	public final static Map<String,Integer> procedureLimits;
	
	//initialize map of procedures to ProcedureLimits
	static {
		procedureLimits = new HashMap<String,Integer>();
		procedureLimits.put("GP101",GP101);
		procedureLimits.put("GP102",GP102);
		procedureLimits.put("GP103",GP103);
		
		procedureLimits.put("R201", R201);
		procedureLimits.put("R202", R202);
		procedureLimits.put("R203", R203);
		
		procedureLimits.put("D401", D401);
		procedureLimits.put("D402", D402);
		procedureLimits.put("D403", D403);

		procedureLimits.put("O601", O601);
		procedureLimits.put("O602", O602);
		procedureLimits.put("O603", O603);
	}
	
	public boolean overClaimLimit(String procedureID, int claimCount) {
		//if the claims' limit is zero, it has no limit
		if(getClaimLimit(procedureID) == 0) {
			return (false);
		}
		else if (claimCount > getClaimLimit(procedureID)) {
			return (true);
		} 
		else {
			return (false);
		}
	}
	
	public int getClaimLimit(String procedureID) {
		//check if the procedure has a procedureLimit
		//if there is no procedure limit, return zero to indicate such
		if (!procedureLimits.containsKey(procedureID)) {
			return 0;
		} 
		else {
			return (procedureLimits.get(procedureID));
		}
	}
}
