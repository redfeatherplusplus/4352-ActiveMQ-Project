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

import utd.claimsProcessing.dao.PolicyDAO;
import utd.claimsProcessing.dao.ProcedureDAO;
import utd.claimsProcessing.domain.Claim;
import utd.claimsProcessing.domain.ClaimFolder;
import utd.claimsProcessing.domain.Policy;
import utd.claimsProcessing.domain.PolicyState;
import utd.claimsProcessing.domain.Procedure;
import utd.claimsProcessing.domain.ProcedureCategory;
import utd.claimsProcessing.domain.RejectedClaimInfo;

// Author: Daren Cheng
// Instructor: M. Christiansen
// Class: SE 4352
// Date: May 2015

// Description:
// This processor is responsible for sending claims to their appropriate processor.
// It is also responsible for rejecting suspended and expired policies.

public class RouteClaimProcessor extends MessageProcessor implements MessageListener{
	private final static Logger logger = Logger.getLogger(RouteClaimProcessor.class);  
	
	//we need a different message producer for each processor to send to
	private MessageProducer producerGP; 												
	private MessageProducer producerDental; 												   
	private MessageProducer producerRadiology; 												   
	private MessageProducer producerOptometry; 												      
	
	public RouteClaimProcessor(Session session) {
		super(session);
	}

	@Override
	public void initialize() throws JMSException {
		//these are the queue's we are sending messages to
		Queue queueGP = getSession().createQueue(QueueNames.processGPClaim);
		Queue queueDental = getSession().createQueue(QueueNames.processDentalClaim);
		Queue queueRadiology = getSession().createQueue(QueueNames.processRadiologyClaim);
		Queue queueOptometry = getSession().createQueue(QueueNames.processOptometryClaim);
		
		//set producers to send messages to their respective queues
		producerGP = getSession().createProducer(queueGP);
		producerDental = getSession().createProducer(queueDental);
		producerRadiology = getSession().createProducer(queueRadiology);
		producerOptometry = getSession().createProducer(queueOptometry);
	}

	@Override
	public void onMessage(Message message) {
		logger.debug("RouteClaimProcessor ReceivedMessage");

		try {
			Object object = ((ObjectMessage) message).getObject();
			ClaimFolder claimFolder = (ClaimFolder)object;
			
			//get ID's for the DAOs
			String memberID = claimFolder.getClaim().getMemberID();
			String providerID = claimFolder.getClaim().getProviderID();
			String policyID = claimFolder.getPolicy().getID();
			String procedureCode = claimFolder.getClaim().getProcedureCode();
			
			//get information about the claim from the DAOs
			Policy policy = PolicyDAO.getSingleton().retrievePolicy(policyID);
			Procedure procedure = ProcedureDAO.getSingleton().retrieveByCode(procedureCode);
			
			
			//check if the policy is not active
			if(!policy.getPolicyState().equals(PolicyState.active)) {
				//if the policy is not active find out why
				if (policy.getPolicyState().equals(PolicyState.suspended)) {
					//the policy is suspended, reject the claim and log the suspension
					
					Claim claim = claimFolder.getClaim();
					RejectedClaimInfo rejectedClaimInfo = new RejectedClaimInfo("Policy is suspended: " + policyID);
					claimFolder.setRejectedClaimInfo(rejectedClaimInfo);
					if(!StringUtils.isBlank(claim.getReplyTo())) {
						rejectedClaimInfo.setEmailAddr(claim.getReplyTo());
					}
					rejectClaim(claimFolder);
				} else if (policy.getPolicyState().equals(PolicyState.expired)) {
					//the policy has expired reject the claim and log the expiration
					
					Claim claim = claimFolder.getClaim();
					RejectedClaimInfo rejectedClaimInfo = new RejectedClaimInfo("Policy has expired: " + policyID);
					claimFolder.setRejectedClaimInfo(rejectedClaimInfo);
					if(!StringUtils.isBlank(claim.getReplyTo())) {
						rejectedClaimInfo.setEmailAddr(claim.getReplyTo());
					}
					rejectClaim(claimFolder);
				} else {
					//error, the policy should be either suspended or expired if it is not active
					//reject the message anyway and log the error
					
					Claim claim = claimFolder.getClaim();
					RejectedClaimInfo rejectedClaimInfo = new RejectedClaimInfo("Error, invalid PolicyState: " + policyID);
					claimFolder.setRejectedClaimInfo(rejectedClaimInfo);
					if(!StringUtils.isBlank(claim.getReplyTo())) {
						rejectedClaimInfo.setEmailAddr(claim.getReplyTo());
					}
					rejectClaim(claimFolder);
				}
			}
			
			
			//if the policy is active, send the claim folder to the correct processor
			else {
			
				logger.debug("Found Claim: " + memberID + " " + providerID + " " + policyID + " " + procedureCode);
			
				//send the claim to the appropriate queue
				if (procedure.getProcedureCategory().equals(ProcedureCategory.GeneralPractice)) {
					Message claimMessage = getSession().createObjectMessage(claimFolder);
					producerGP.send(claimMessage);
					logger.debug("Finished Sending: " + memberID + " " + providerID + " " + policyID + " " + procedureCode);
				} 
				else if (procedure.getProcedureCategory().equals(ProcedureCategory.Dental)) {
					Message claimMessage = getSession().createObjectMessage(claimFolder);
					producerDental.send(claimMessage);
					logger.debug("Finished Sending: " + memberID + " " + providerID + " " + policyID + " " + procedureCode);
				}
				else if (procedure.getProcedureCategory().equals(ProcedureCategory.Radiology)) {
					Message claimMessage = getSession().createObjectMessage(claimFolder);
					producerRadiology.send(claimMessage);
					logger.debug("Finished Sending: " + memberID + " " + providerID + " " + policyID + " " + procedureCode);
				}
				else if (procedure.getProcedureCategory().equals(ProcedureCategory.Optometry)) {
					Message claimMessage = getSession().createObjectMessage(claimFolder);
					producerOptometry.send(claimMessage);
					logger.debug("Finished Sending: " + memberID + " " + providerID + " " + policyID + " " + procedureCode);
				}
				else {
					//error, a processor does not exist for this procedure's category 
					//reject the claim as it is of invalid type
					
					Claim claim = claimFolder.getClaim();
					RejectedClaimInfo rejectedClaimInfo = new RejectedClaimInfo("Error, invalid ProcedureCategory: " + procedureCode);
					claimFolder.setRejectedClaimInfo(rejectedClaimInfo);
					if(!StringUtils.isBlank(claim.getReplyTo())) {
						rejectedClaimInfo.setEmailAddr(claim.getReplyTo());
					}
					rejectClaim(claimFolder);
					logger.debug("Error, invalid ProcedureCategory: " + procedureCode);
				}
			}
		}
		catch (Exception ex) {
			logError("RouteClaimProcessor.onMessage() " + ex.getMessage(), ex);
		}
	}
}

