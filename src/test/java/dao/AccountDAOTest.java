package dao;import com.getgo.cloudtower.dao.AccountDAO;import com.getgo.cloudtower.model.Account;import junit.framework.TestCase;import java.util.List;/** * Created by amirnashat on 8/2/16. */public class AccountDAOTest extends TestCase {	public void testGetAccounts() {		List<Account> accounts = AccountDAO.getAccounts();		for (Account a: accounts		     ) {			System.out.println(a.toString());		}		assertEquals("Didn't find 2 accounts",2,accounts.size());	}}