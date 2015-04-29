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
import utd.claimsProcessing.domain.Policy;
import utd.claimsProcessing.domain.PolicyState;
import utd.claimsProcessing.domain.ProcedureCategory;
import utd.claimsProcessing.domain.RejectedClaimInfo;


public class GeneralPracticeClaimProcessor extends MedicalClaimProcessor implements MessageListener
{

	private MessageProducer producer;

	public GeneralPracticeClaimProcessor(Session session) {
		super(session, ProcedureCategory.GeneralPractice);
	}

	public void initialize() throws JMSException {

	}
	
	public void onMessage(Message message)
	{
	  logger.debug("GeneralPracticeClaimProcessor ReceivedMessage");

	  try {
		    Object object = ((ObjectMessage) message).getObject();
		    ClaimFolder claimFolder = (ClaimFolder) object;
		    
		    //Check if policy is active -- commented out because I figured out that daren implemented this
		    //if(validatePolicy(claimFolder)) 
		   // {  
		    	//Policy active
		    	//logger.debug("GeneralPracticeClaimProcessor PolicyActive");
		    	
			    if(validateProcedure(claimFolder)) 
			    {
			    	//Accept - procedure covered
			    	logger.debug("GeneralPracticeClaimProcessor ProcedureValid");
			    	
					Message claimMessage = getSession().createObjectMessage(claimFolder);
					payProducer.send(claimMessage);
			    }
			    else
			    {
			    	//Deny - procedure not covered
			    	logger.debug("GeneralPracticeClaimProcessor ProcedureInvalid");
			    	
					RejectedClaimInfo rejectedClaimInfo = new RejectedClaimInfo("The requested procedure is not covered");
					rejectedClaimInfo.setDescription(claimFolder.getProcedure().getDescription());
					claimFolder.setRejectedClaimInfo(rejectedClaimInfo);
					if(!StringUtils.isBlank(claimFolder.getClaim().getReplyTo())) {
						rejectedClaimInfo.setEmailAddr(claimFolder.getClaim().getReplyTo());
					}
			    	
			    	Message claimMessage = getSession().createObjectMessage(claimFolder);
					denyProducer.send(claimMessage);
			    }
		   // }
		   // else
		   // {
		   // 	//Deny - policy inactive (suspended, expired, etc)
		   //	logger.debug("GeneralPracticeClaimProcessor PolicyNotActive");
		   //	
					//	RejectedClaimInfo rejectedClaimInfo = new RejectedClaimInfo("Your policy is not active. Status: " + claimFolder.getPolicy().getPolicyState().toString());;
			//	claimFolder.setRejectedClaimInfo(rejectedClaimInfo);
			//	if(!StringUtils.isBlank(claimFolder.getClaim().getReplyTo())) {
			//		rejectedClaimInfo.setEmailAddr(claimFolder.getClaim().getReplyTo());
			//	}
		    	
			//	Message claimMessage = getSession().createObjectMessage(claimFolder);
			//	denyProducer.send(claimMessage);
		    //}
		    
		  }
		  catch (Exception ex) {
		    logError("RadiologyClaimProcessor.onMessage() " + ex.getMessage(), ex);
		  }
	}
}
