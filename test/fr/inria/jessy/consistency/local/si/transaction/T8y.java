package fr.inria.jessy.consistency.local.si.transaction;

import org.apache.log4j.Logger;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.entity.Sample2EntityClass;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.Transaction;

public class T8y extends Transaction{
	
	private static Logger logger = Logger
	.getLogger(T8y.class);
	
	public T8y(Jessy jessy) throws Exception {
		super(jessy);
		setRetryCommitOnAbort(false);
	}
	
	@Override
	public ExecutionHistory execute() {

		try {
			
			Thread.sleep(1000);
			
			Sample2EntityClass se=read(Sample2EntityClass.class, "2");			
			se.setData("8y");
			
			logger.debug("transaction T8y started with :"+se.getLocalVector().getSelfValue()+" "+se.getLocalVector());
			
			write(se);
			
			Thread.sleep(2000);
			
			return commitTransaction();	
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}		
	}

}
