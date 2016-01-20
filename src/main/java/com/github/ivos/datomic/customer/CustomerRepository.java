package com.github.ivos.datomic.customer;

import com.github.ivos.datomic.support.DatomicRepository;
import datomic.Connection;
import datomic.Peer;
import datomic.Util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CustomerRepository extends DatomicRepository<Customer> {

	public CustomerRepository() {
		super(Customer.class);
	}

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

}
