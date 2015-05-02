package utd.claimsProcessing.messageProcessors;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import utd.claimsProcessing.domain.Claim;
import utd.claimsProcessing.domain.ClaimFolder;
import utd.claimsProcessing.domain.ClaimRestrictions;
import utd.claimsProcessing.domain.Policy;
import utd.claimsProcessing.domain.PolicyState;
import utd.claimsProcessing.domain.ProcedureCategory;
import utd.claimsProcessing.domain.RejectedClaimInfo;


public class OptometryClaimProcessor extends MedicalClaimProcessor implements MessageListener
{

	private MessageProducer producer;

	public OptometryClaimProcessor(Session session) {
		super(session, ProcedureCategory.Optometry);
	}

	public void initialize() throws JMSException {

	}
	
	public void onMessage(Message message)
	{
	  logger.debug("OptometryClaimProcessor ReceivedMessage");

	  try {
		    Object object = ((ObjectMessage) message).getObject();
		    ClaimFolder claimFolder = (ClaimFolder) object;
		    String memberID = claimFolder.getClaim().getMemberID();
		    String procedureCode = claimFolder.getClaim().getProcedureCode();
		    
		    //check if the member is over the claim limit for this procedure
			if(ClaimRestrictions.overClaimLimit(memberID, procedureCode)) {
				//Deny - over claim limit
		    	logger.debug("OptometryClaimProcessor OverClaimLimit");
		    	
				RejectedClaimInfo rejectedClaimInfo = new RejectedClaimInfo(memberID +
						" is over the claim limit for " + procedureCode);
				rejectedClaimInfo.setDescription(claimFolder.getProcedure().getDescription());
				claimFolder.setRejectedClaimInfo(rejectedClaimInfo);
				if(!StringUtils.isBlank(claimFolder.getClaim().getReplyTo())) {
					rejectedClaimInfo.setEmailAddr(claimFolder.getClaim().getReplyTo());
				}
		    	
		    	Message claimMessage = getSession().createObjectMessage(claimFolder);
				denyProducer.send(claimMessage);
	    	}
			else if(validateProcedure(claimFolder)) 
		    {
		    	//Accept - procedure covered
		    	logger.debug("OptometryClaimProcessor ProcedureValid");
		    	
		    	//increment claimCount corresponding to that member's claim
		    	ClaimRestrictions.incrementClaimCount(memberID, procedureCode);
		    	
				Message claimMessage = getSession().createObjectMessage(claimFolder);
				payProducer.send(claimMessage);
		    }
		    else
		    {
		    	//Deny - procedure not covered
		    	logger.debug("OptometryClaimProcessor ProcedureInvalid");
		    	
				RejectedClaimInfo rejectedClaimInfo = new RejectedClaimInfo("The requested procedure is not covered");
				rejectedClaimInfo.setDescription(claimFolder.getProcedure().getDescription());
				claimFolder.setRejectedClaimInfo(rejectedClaimInfo);
				if(!StringUtils.isBlank(claimFolder.getClaim().getReplyTo())) {
					rejectedClaimInfo.setEmailAddr(claimFolder.getClaim().getReplyTo());
				}
		    	
		    	Message claimMessage = getSession().createObjectMessage(claimFolder);
				denyProducer.send(claimMessage);
		    }
		  }
		  catch (Exception ex) {
		    logError("OptometryClaimProcessor.onMessage() " + ex.getMessage(), ex);
		  }
	}
}
