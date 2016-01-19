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
	public void find() {
		s.create(customer(21));
		s.create(customer(22).withPhone("123456"));
		s.create(customer(23));
		s.create(customer(24).withPhone("123456"));
		s.create(customer(25));

		Customer query = Customer.builder().phone("123456").build();

		List<Customer> customers = s.find(query);

		List<Customer> customersWithoutIds = customers.stream()
				.map(customer -> customer.withId(null))
				.collect(toList());
		String expected = "[" +
				"Customer(id=null, name=name 22, email=email 22, phone=123456), " +
				"Customer(id=null, name=name 24, email=email 24, phone=123456)" +
				"]";
		assertEquals(expected, customersWithoutIds.toString());
	}

}
