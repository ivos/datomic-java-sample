package com.github.ivos.datomic.customer;

import com.github.ivos.datomic.utils.SchemaPrinter;
import com.github.ivos.datomic.utils.SchemaSetup;
import datomic.Connection;
import datomic.Peer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CustomerServiceTest {

	private static Connection connection;
	private CustomerService s;

	@BeforeClass
	public static void setUpDb() {
		String uri = "datomic:mem://sample";
		Peer.createDatabase(uri);
		connection = Peer.connect(uri);
		new SchemaSetup().setupSchema(connection);
	}


	@Before
	public void setUp() {
		s = new CustomerService();
		s.setConnection(connection);
	}

	@Test
	public void test() {
		Customer customer = new Customer();
		customer.setName("name1");
		customer.setEmail("email1");
		customer.setPhone("phone1");
		s.create(customer);

		new SchemaPrinter().printDatabaseContent(connection.db(), "sample");
	}

}
