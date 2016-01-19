package com.github.ivos.datomic.customer;

import datomic.Connection;
import datomic.Entity;
import datomic.Peer;
import datomic.Util;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;

public class CustomerService {

	@Setter
	private Connection connection;

	public Customer create(Customer customer) {
		Object tempId = Peer.tempid(":sample");
		List tx = Util.list(Util.map(
				":db/id", tempId,
				":customer/name", customer.getName(),
				":customer/email", customer.getEmail(),
				":customer/phone", customer.getPhone()
		));
		Map txResult = null;
		try {
			txResult = connection.transact(tx).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		Long id = (Long) Peer.resolveTempid(connection.db(), txResult.get(Connection.TEMPIDS), tempId);
		return customer.withId(id);
	}

	public Customer get(Long id) {
		Entity entity = connection.db().entity(id);
		return Customer.builder()
				.id(id)
				.name((String) entity.get(":customer/name"))
				.email((String) entity.get(":customer/email"))
				.phone((String) entity.get(":customer/phone"))
				.build();
	}

	public List<Customer> find(Customer query) {
		String edn = "[:find ?e :in $ ?phone :where [?e :customer/phone ?phone]]";
		Set<?> data = (Set) Peer.q(edn, connection.db(), query.getPhone());
		return data.stream()
				.sorted()
				.map(instance -> get((Long) ((List) instance).get(0)))
				.collect(toList());
	}

}
