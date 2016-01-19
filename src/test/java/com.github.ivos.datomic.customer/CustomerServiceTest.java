package com.github.ivos.datomic.customer;

import com.github.ivos.datomic.utils.SchemaSetup;
import datomic.Connection;
import datomic.Peer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

	private Customer customer(int i) {
		return Customer.builder()
				.name("name " + i)
				.email("email " + i)
				.phone("phone " + i)
				.build();
	}

	@Test
	public void createGet() {
		Customer customer = customer(11);

		Customer result = s.create(customer);

		assertNotNull("Result id set", result.getId());

		Customer saved = s.get(result.getId());
		String expected = "Customer(id=null, name=name 11, email=email 11, phone=phone 11)";
		assertEquals("Customer", expected, saved.withId(null).toString());
	}

	@Test
	public void find_ByPhone() {
		s.create(customer(21));
		s.create(customer(22).withPhone("phone1"));
		s.create(customer(23));
		s.create(customer(24).withPhone("phone1"));
		s.create(customer(25));

		Customer query = Customer.builder().phone("phone1").build();

		List<Customer> customers = s.find(query);

		List<Customer> customersWithoutIds = customers.stream()
				.map(customer -> customer.withId(null))
				.collect(toList());
		String expected = "[" +
				"Customer(id=null, name=name 22, email=email 22, phone=phone1), " +
				"Customer(id=null, name=name 24, email=email 24, phone=phone1)" +
				"]";
		assertEquals(expected, customersWithoutIds.toString());
	}

	@Test
	public void find_ByEmail() {
		s.create(customer(31));
		s.create(customer(32).withEmail("email1@server.com"));
		s.create(customer(33));
		s.create(customer(34).withEmail("email1@server.com"));
		s.create(customer(35));

		Customer query = Customer.builder().email("email1@server.com").build();

		List<Customer> customers = s.find(query);

		List<Customer> customersWithoutIds = customers.stream()
				.map(customer -> customer.withId(null))
				.collect(toList());
		String expected = "[" +
				"Customer(id=null, name=name 32, email=email1@server.com, phone=phone 32), " +
				"Customer(id=null, name=name 34, email=email1@server.com, phone=phone 34)" +
				"]";
		assertEquals(expected, customersWithoutIds.toString());
	}

	@Test
	public void find_ByPhoneAndEmail() {
		s.create(customer(41));
		s.create(customer(42).withEmail("email2@server.com").withPhone("phone2"));
		s.create(customer(43).withEmail("email2@server.com").withPhone("phone2"));
		s.create(customer(44).withEmail("email2@server.com"));
		s.create(customer(45).withPhone("phone2"));
		s.create(customer(46).withEmail("email2@server.com").withPhone("phone2"));
		s.create(customer(47));

		Customer query = Customer.builder()
				.email("email2@server.com")
				.phone("phone2")
				.build();

		List<Customer> customers = s.find(query);

		List<Customer> customersWithoutIds = customers.stream()
				.map(customer -> customer.withId(null))
				.collect(toList());
		String expected = "[" +
				"Customer(id=null, name=name 42, email=email2@server.com, phone=phone2), " +
				"Customer(id=null, name=name 43, email=email2@server.com, phone=phone2), " +
				"Customer(id=null, name=name 46, email=email2@server.com, phone=phone2)" +
				"]";
		assertEquals(expected, customersWithoutIds.toString());
	}

}
