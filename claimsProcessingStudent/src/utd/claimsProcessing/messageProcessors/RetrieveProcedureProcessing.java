package utd.claimsProcessing.messageProcessors;

import javax.jms.JMSException;

public class RetrieveProcedureProcessor extends MessageProcessor implements MessageListener{
	private final static Logger logger = Logger.getLogger(RetrieveProcedureProcessor.class);
	
	private MessageProducer producer;
	
	public RetrieveProcedureProcessor(Session session){
		super(session);
	}
	
	public void initialize() throws JMSException{
		Queue queue = getSession().createQueue(QueueNames.routeClaim);
		producer = getSession().createProducer(queue);
	}
	
	public void onMessage(Message message){
		logger.debug("RetrieveProcedureProcessor ReceivedMessage");
		
		try{
			Object object = ((ObjectMessage) message).getObject();
			ClaimFolder claimFolder = (ClaimFolder)object;
			
			String procedureCode = claimFolder.getClaim().getProcedureCode();
			Procedure procedure = ProcedureDAO.getSingleton.retrieveByCode(procedureCode);
			if(procedure == null){
				Claim claim = claimFolder.getClaim();
				RejectedClaimInfo rejectedClaimInfo = new RejectedClaimInfo("Procedure Not Found: " + procedureCode);
				claimFolder.setRejectedClaimInfo(rejectedClaimInfo);
				if(!StringUtils.isBlank(claim.getReplyTo())){
					rejectedClaimInfo.setEmailAddr(claim.getReplyTo());
				}
				rejectClaim(claimFolder);
			}
			else{
				logger.debug("Found Procedure: " + procedure.retrieveByCode(procedureCode));
				
				claimFolder.setProcedure(procedure);
				
				Message claimMessage = getSession().createObjectMessage(claimFolder);
				producer.send(claimMessage);
				logger.debug("Finished Sending: " + procedure.retrieveByCode(procedureCode));
			}
		}
		catch (Exception ex){
			logError("RetrieveProcedureProcessor.onMessage() " + ex.getMessage(), ex);
		}
	}

}
