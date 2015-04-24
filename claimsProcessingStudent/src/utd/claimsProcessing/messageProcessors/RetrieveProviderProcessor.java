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

import utd.claimsProcessing.dao.ProviderDAO;
import utd.claimsProcessing.domain.Claim;
import utd.claimsProcessing.domain.ClaimFolder;
import utd.claimsProcessing.domain.Provider;
import utd.claimsProcessing.domain.RejectedClaimInfo;

public class RetrieveProviderProcessor extends MessageProcessor implements MessageListener{
	
	public RetrieveProviderProcessor(Session session) {
		super(session);
	}

	private final static Logger logger = Logger.getLogger(RetrieveProviderProcessor.class);
	
	private MessageProducer producer;
	
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
				logger.debug("Finished Sending: " + provider.getProviderName() + " ID: " + provider.getID());
			}
		}
		catch (Exception ex){
			logError("RetrieveProviderProcessor.onMessage() " + ex.getMessage(), ex);
		}
	}
}
