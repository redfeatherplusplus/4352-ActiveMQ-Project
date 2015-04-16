package utd.claimsProcessing.messageProcessors;

import javax.jms.JMSException;

import utd.claimsProcessing.messageProcessors.MessageListener;

public class RetreiveProviderProcessor extends MessageProcessor implements MessageListener{
	
	private final static Logger logger = Logger.getLogger(RetrieveProviderProcessor.class);
	
	private MessageProducer producer;
	
	public RetrieveProviderProcessor(Session session){
		super(session);
	}
	
	public void initialize() throws JMSException{
		Queue queue = getSession().createQueue(QueueNames.retrievePolicy);
		producer = getSession().createProducer(queue);
	}
	
	public void onMessage(Message message){
		logger.debug("RetrieveProviderProcessor ReceivedMessage");
		
		try{
			Object object = ((ObjectMessage) message).getObject();
			ClaimFolder claimFolder = (ClaimFolder)object;
			
			String providerID = claimFolder.getClaim().getProviderID();
			Provider provider = ProviderDAO.getSingleton().retrieveProvider(providerID);
			if(provider == null){
				Claim claim = claimFolder.getClaim();
				RejectedClaimInfo rejectedClaimInfo = new RejectedClaimInfo("Provider Not Found: " + providerID);
				claimFolder.setRejectedClaimInfo(rejectedClaimInfo);
				if(!StringUtils.isBlank(claim.getReplyTo())){
					rejectedClaimInfo.setEmailAddr(claim.getReplyTo());
				}
				rejectClaim(claimFolder);
			}
			else{
				logger.debug("Found Provider: " + provider.getProviderName() + "  ID: " + provider.getID());
				
				claimFolder.setProvider(provider);
				
				Message claimMessage = getSession().createObjectMessage(claimFolder);
				producer.send(claimMessage);
				logger.debug("Finished Sending: " + provider.getProviderName + " ID: " + provider.getID());
			}
		}
		catch (Exception ex){
			logError("RetrieveProviderProcessor.onMessage() " + ex.getMessage(), ex);
		}
	}
}
