package utd.claimsProcessing.messageProcessors;

import javax.jms.JMSException;

public class RetrievePolicyProcessor extends MessageProcessor implements MessageListener{
	private final static Logger logger = Logger.getLogger(RetrievePolicyProcessor.class);
	
	private MessageProducer producer;
	
	public RetrievePolicyProcessor(Session session){
		super(session);
	}
	
	public void initialize() throws JMSException{
		Queue queue = getSession().createQueue(QueueNames.retrieveProcedure);
		producer = getSession().createProducer(queue);
	}
	
	public void onMessage(Message message){
		logger.debug("RetrievePolicyProcessor ReceivedMessage");
		
		try{
			Object object = ((ObjectMessage) message).getObject();
			ClaimFolder claimFolder = (ClaimFolder)object;
			
			String policyID = claimFolder.getPolicy();
			Policy policy = PolicyDAO.getSingleton().retrievePolicy(policyID);
			
			//check that policy is valid
			if(policy == null){
				Claim claim = claimFolder.getClaim();
				RejectedClaimInfo rejectedClaimInfo = new RejectedClaimInfo("Policy Not Found: " + policyID);
				claimFolder.setRejectedClaimInfo(rejectedClaimInfo);
				if(!StringUtils.isBlank(claim.getReplyTo())){
					rejectedClaimInfo.setEmailAddr(claim.getReplyTo());
				}
				rejectClaim(claimFolder);
			}
			else{
				logger.debug("Found Policy: " + policy.retrievePolicy());
				
				claimFolder.setPolicy(policy);
				
				Message claimMessage = getSession().createObjectMessage(claimFolder);
				producer.send(claimMessage);
				logger.debug("Finished Sending: " + policy.retrievePolicy());
			}
			
		}
		catch (Exception ex){
			logError("RetrievePolicyProcessor.onMessage() " + ex.getMessage(), ex);
		}
	}
}
