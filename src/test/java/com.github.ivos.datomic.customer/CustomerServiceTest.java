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
	private CustomerService service;
	private CustomerRepository repository;

	@BeforeClass
	public static void setUpDb() {
		String uri = "datomic:mem://sample";
		Peer.createDatabase(uri);
		connection = Peer.connect(uri);
		new SchemaSetup().setupSchema(connection);
	}


	@Before
	public void setUp() {
		repository = new CustomerRepository();
		repository.setConnection(connection);
		service = new CustomerService();
		service.setRepository(repository);
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

		Customer result = service.create(customer);

		assertNotNull("Result id set", result.getId());

		Customer saved = service.get(result.getId());
		String expected = "Customer(id=null, name=name 11, email=email 11, phone=phone 11)";
		assertEquals("Customer", expected, saved.withId(null).toString());
	}

	@Test
	public void list_ByPhone() {
		service.create(customer(21));
		service.create(customer(22).withPhone("phone1"));
		service.create(customer(23));
		service.create(customer(24).withPhone("phone1"));
		service.create(customer(25));

		Customer query = Customer.builder().phone("phone1").build();

		List<Customer> customers = service.list(query);

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
	public void list_ByEmail() {
		service.create(customer(31));
		service.create(customer(32).withEmail("email1@server.com"));
		service.create(customer(33));
		service.create(customer(34).withEmail("email1@server.com"));
		service.create(customer(35));

		Customer query = Customer.builder().email("email1@server.com").build();

		List<Customer> customers = service.list(query);

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
	public void list_ByPhoneAndEmail() {
		service.create(customer(41));
		service.create(customer(42).withEmail("email2@server.com").withPhone("phone2"));
		service.create(customer(43).withEmail("email2@server.com").withPhone("phone2"));
		service.create(customer(44).withEmail("email2@server.com"));
		service.create(customer(45).withPhone("phone2"));
		service.create(customer(46).withEmail("email2@server.com").withPhone("phone2"));
		service.create(customer(47));

		Customer query = Customer.builder()
				.email("email2@server.com")
				.phone("phone2")
				.build();

		List<Customer> customers = service.list(query);

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
