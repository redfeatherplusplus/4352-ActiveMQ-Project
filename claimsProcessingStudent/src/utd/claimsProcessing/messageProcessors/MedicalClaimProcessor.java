package utd.claimsProcessing.messageProcessors;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import utd.claimsProcessing.domain.Claim;
import utd.claimsProcessing.domain.ClaimFolder;
import utd.claimsProcessing.domain.Policy;
import utd.claimsProcessing.domain.PolicyState;
import utd.claimsProcessing.domain.Procedure;
import utd.claimsProcessing.domain.ProcedureCategory;
import utd.claimsProcessing.domain.RejectedClaimInfo;


public abstract class MedicalClaimProcessor extends MessageProcessor 
{

	private ProcedureCategory procedureCategory;
	Queue payQueue, denyQueue;
	MessageProducer payProducer, denyProducer;
	protected final static Logger logger = Logger.getLogger(RetrieveMemberProcessor.class);
	
	MedicalClaimProcessor(Session session, ProcedureCategory procedureCategory) {
		super(session);
		this.procedureCategory = procedureCategory;
		
		Queue payQueue;
		try {
			payQueue = getSession().createQueue(QueueNames.payClaim);
			Queue denyQueue = getSession().createQueue(QueueNames.denyClaim);
			payProducer = getSession().createProducer(payQueue);
			denyProducer = getSession().createProducer(denyQueue);
		} catch (JMSException e) {
			logger.error("Unable to create pay/deny queues " + e.getMessage(), e);
		}
	}
	
	
	//Validate that the is covered by the policy
	protected boolean validateProcedure(ClaimFolder claimFolder)
	{
		boolean validProcedure = false;
		
		Claim claim = claimFolder.getClaim();
		Policy policy = claimFolder.getPolicy();
		Procedure procedure = claimFolder.getProcedure();
		List<ProcedureCategory> coveredCategories = policy.getCategories();

		if(coveredCategories.contains(procedure.getProcedureCategory()))
		{
			//Procedure category is covered by policy
			validProcedure = true;
		}
		
		return validProcedure;
	}

}
