package com.github.ivos.datomic.customer;

import com.github.ivos.datomic.support.EntityNotFoundException;
import com.github.ivos.datomic.utils.SchemaSetup;
import datomic.Connection;
import datomic.Peer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class CustomerServiceTest {

	private Connection connection;
	private CustomerService service;

	@Before
	public void setUpDb() {
		String uri = "datomic:mem://" + UUID.randomUUID();
		Peer.createDatabase(uri);
		connection = Peer.connect(uri);
		new SchemaSetup().setupSchema(connection);
	}

	@Before
	public void setUp() {
		CustomerRepository repository = new CustomerRepository();
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
	public void create() {
		Customer customer = customer(11);

		Customer result = service.create(customer);

		assertNotNull("Result id set", result.getId());

		Customer saved = service.get(result.getId());
		String expected = "Customer(id=null, version=1, name=name 11, email=email 11, phone=phone 11)";
		assertEquals("Customer", expected, saved.withId(null).toString());
	}

	@Test
	public void get() {
		Customer customer = customer(11);
		Long id = service.create(customer).getId();

		Customer result = service.get(id);

		assertNotNull("Result id set", result.getId());

		String expected = "Customer(id=null, version=1, name=name 11, email=email 11, phone=phone 11)";
		assertEquals("Customer", expected, result.withId(null).toString());
	}

	@Test
	public void get_NotFound() {
		try {
			service.get(-10000999888L);
			fail("Should fail");
		} catch (EntityNotFoundException e) {
			assertEquals("Entity with id -10000999888 was not found in the database.", e.getMessage());
		}
	}

	@Test
	public void list_ByPhone() {
		service.create(customer(11));
		service.create(customer(12).withPhone("phone1"));
		service.create(customer(13));
		service.create(customer(14).withPhone("phone1"));
		service.create(customer(15));

		Customer query = Customer.builder().phone("phone1").build();

		List<Customer> customers = service.list(query);

		List<Customer> customersWithoutIds = customers.stream()
				.map(customer -> customer.withId(null))
				.collect(toList());
		String expected = "[" +
				"Customer(id=null, version=1, name=name 12, email=email 12, phone=phone1), " +
				"Customer(id=null, version=1, name=name 14, email=email 14, phone=phone1)" +
				"]";
		assertEquals(expected, customersWithoutIds.toString());
	}

	@Test
	public void list_ByEmail() {
		service.create(customer(11));
		service.create(customer(12).withEmail("email1@server.com"));
		service.create(customer(13));
		service.create(customer(14).withEmail("email1@server.com"));
		service.create(customer(15));

		Customer query = Customer.builder().email("email1@server.com").build();

		List<Customer> customers = service.list(query);

		List<Customer> customersWithoutIds = customers.stream()
				.map(customer -> customer.withId(null))
				.collect(toList());
		String expected = "[" +
				"Customer(id=null, version=1, name=name 12, email=email1@server.com, phone=phone 12), " +
				"Customer(id=null, version=1, name=name 14, email=email1@server.com, phone=phone 14)" +
				"]";
		assertEquals(expected, customersWithoutIds.toString());
	}

	@Test
	public void list_ByPhoneAndEmail() {
		service.create(customer(11));
		service.create(customer(12).withEmail("email1@server.com").withPhone("phone1"));
		service.create(customer(13).withEmail("email1@server.com").withPhone("phone1"));
		service.create(customer(14).withEmail("email1@server.com"));
		service.create(customer(15).withPhone("phone1"));
		service.create(customer(16).withEmail("email1@server.com").withPhone("phone1"));
		service.create(customer(17));

		Customer query = Customer.builder()
				.email("email1@server.com")
				.phone("phone1")
				.build();

		List<Customer> customers = service.list(query);

		List<Customer> customersWithoutIds = customers.stream()
				.map(customer -> customer.withId(null))
				.collect(toList());
		String expected = "[" +
				"Customer(id=null, version=1, name=name 12, email=email1@server.com, phone=phone1), " +
				"Customer(id=null, version=1, name=name 13, email=email1@server.com, phone=phone1), " +
				"Customer(id=null, version=1, name=name 16, email=email1@server.com, phone=phone1)" +
				"]";
		assertEquals(expected, customersWithoutIds.toString());
	}

}
