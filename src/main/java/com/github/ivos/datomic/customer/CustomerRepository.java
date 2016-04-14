package com.github.ivos.datomic.customer;

import com.github.ivos.datomic.support.DatomicRepository;
import com.github.ivos.datomic.support.OptimisticLockException;
import datomic.Connection;
import datomic.Database;
import datomic.Entity;
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
		Object tempId = Peer.tempid("sample");
		List tx = Util.list(Util.map(
				"db/id", tempId,
				"customer/version", 1L,
				"customer/name", customer.getName(),
				"customer/email", customer.getEmail(),
				"customer/phone", customer.getPhone()
		));
		Map txResult;
		try {
			txResult = connection.transact(tx).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		Long id = (Long) Peer.resolveTempid(connection.db(), txResult.get(Connection.TEMPIDS), tempId);
//		return customer.withId(id).withVersion(1L);
		Database db = (Database) txResult.get(Connection.DB_AFTER);
		Entity entity = db.entity(id).touch();
		return map(entity);
	}

	public Customer update(Customer customer) {
		Customer previous = get(customer.getId());
		long newVersion = previous.getVersion() + 1;
		List tx = Util.list(Util.map(
				"db/id", customer.getId(),
				"customer/email", customer.getEmail(),
				"customer/name", customer.getName(),
				"customer/phone", customer.getPhone()
				),
				Util.list("db.fn/cas", customer.getId(),
						"customer/version", previous.getVersion(), newVersion)
		);
		Map txResult;
		try {
			txResult = connection.transact(tx).get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new OptimisticLockException("TBD");
		}
//		return customer.withVersion(newVersion);
		Database db = (Database) txResult.get(Connection.DB_AFTER);
		Entity entity = db.entity(customer.getId()).touch();
		return map(entity);
	}

}
