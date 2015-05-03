package utd.claimsProcessing.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Message;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import utd.claimsProcessing.dao.ProcedureDAO;

/**
 * This class maintains claims restrictions for each claim.
 * Note that a claim of '0' is unrestricted. 
 */
public class ClaimRestrictions {
	private final static Logger logger = Logger.getLogger(ClaimRestrictions.class);
	private static HashMap<String, Map<String, Integer>> claimCount = new HashMap<String, Map<String, Integer>>();
	
	//map from procedures to procedureLimits
	public final static Map<String,Integer> procedureLimits;
	
	//initialize map of procedures to ProcedureLimits
	static {
		procedureLimits = new HashMap<String, Integer>();
		try {
			//get a collection of procedures and map their claimLimits if they exist
			Collection<Procedure> procedures = ProcedureDAO.getSingleton().retrieveAllProcedures();
			for (Procedure procedure : procedures) {
				//check if the procedure does not have a claim limit
				//note that a claim restriction of zero indicates no claim limit
				if(procedure.getClaimRestriction() == 0) {
					logger.debug("Procedure " + procedure.getProcedureCode() + " does not have claim restrictions.");
				}
				else {
					procedureLimits.put(procedure.getProcedureCode(), procedure.getClaimRestriction());
				}
			}
		} catch (Exception ex) {
			logger.debug("Error, could not get ClaimRestrictions: " + ex.getMessage());
		}
	}
	
	public static boolean overClaimLimit(String memberID, String procedureCode) {
		//check if this member has filed a claim before
    	if(claimCount.containsKey(memberID)) {
    		//check if the member has filed this claim before
    		if(claimCount.get(memberID).containsKey(procedureCode)) {
    			//check if the member is over the claim limit for this claim
    			int currentClaimCount = claimCount.get(memberID).get(procedureCode);
    			if(ClaimRestrictions.overClaimLimit(procedureCode, currentClaimCount)) {
    				return (true);
    			}
    		}
    	}
    	//the member is not over the claim limit or has not filed this claim before
		return (false);
	}
	
	public static void incrementClaimCount(String memberID, String procedureCode) {
		if(claimCount.containsKey(memberID)) {
    		//check if the member has filed this claim before
    		if(claimCount.get(memberID).containsKey(procedureCode)) {
    			//the member has filed this claim before, increment the claimCount
    			int previousClaimCount = claimCount.get(memberID).get(procedureCode);
    			claimCount.get(memberID).replace(procedureCode, previousClaimCount + 1);
    		}
    		else {
	    		//this is the first time the member has filed this claim
    			claimCount.get(memberID).put(procedureCode, 1);
    		}
    	}
    	else {
	    	//this is the first time the member has filed a claim
    		HashMap<String, Integer> firstClaim = new HashMap<String, Integer>();
    		firstClaim.put(procedureCode, 1);
			claimCount.put(memberID, firstClaim);
    	}
	}
	
	public static boolean overClaimLimit(String procedureCode, int claimCount) {
		//if the claims' limit is zero, it has no limit
		if(getClaimLimit(procedureCode) == 0) {
			return (false);
		}
		else if (claimCount >= getClaimLimit(procedureCode)) {
			return (true);
		} 
		else {
			return (false);
		}
	}
	
	public static int getClaimLimit(String procedureCode) {
		//check if the procedure has a procedureLimit
		//if there is no procedure limit, return zero to indicate such
		if (!procedureLimits.containsKey(procedureCode)) {
			return 0;
		} 
		else {
			return (procedureLimits.get(procedureCode));
		}
	}
}
