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


public class DentalClaimProcessor extends MedicalClaimProcessor implements MessageListener
{

	private MessageProducer producer;

	public DentalClaimProcessor(Session session) {
		super(session, ProcedureCategory.Dental);
	}

	public void initialize() throws JMSException {

	}
	
	public void onMessage(Message message)
	{
	  logger.debug("DentalClaimProcessor ReceivedMessage");

	  try {
		    Object object = ((ObjectMessage) message).getObject();
		    ClaimFolder claimFolder = (ClaimFolder) object;
		    
		    if(validateProcedure(claimFolder)) 
		    {
		    	//Accept - procedure covered
		    	logger.debug("DentalClaimProcessor ProcedureValid");
		    	
				Message claimMessage = getSession().createObjectMessage(claimFolder);
				payProducer.send(claimMessage);
		    }
		    else
		    {
		    	//Deny - procedure not covered
		    	logger.debug("DentalClaimProcessor ProcedureInvalid");
		    	
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
		    logError("DentalClaimProcessor.onMessage() " + ex.getMessage(), ex);
		  }
	}
}
