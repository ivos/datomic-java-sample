package com.github.ivos.datomic.customer;

import datomic.Connection;
import datomic.Peer;
import datomic.Util;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
		Object id = Peer.resolveTempid(connection.db(), txResult.get(Connection.TEMPIDS), tempId);
		System.out.println("txResult " + txResult);
		System.out.println("id " + id);
		return customer;
	}

}
