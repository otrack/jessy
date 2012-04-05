package fr.inria.jessy.benchmark.tpcc;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import fr.inria.jessy.LocalJessy;
import fr.inria.jessy.benchmark.tpcc.entities.*;
import fr.inria.jessy.transaction.*;

/**
 * @author WANG Haiyun & ZHAO Guang
 * 
 */
public class TpccTestNewOrder {

	LocalJessy jessy;
	NewOrder no; 
	Warehouse wh;
	District di;
	Item it;
	Customer cu;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		jessy = LocalJessy.getInstance();
		no = new NewOrder(jessy);
	}

	/**
	 * Test method for {@link fr.inria.jessy.BenchmarkTpcc.NewOrder}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNewOrder() throws Exception {

		ExecutionHistory result = no.execute();
		/* test execution */
		assertEquals("Result", TransactionState.COMMITTED,
				result.getTransactionState());

			

	}

}