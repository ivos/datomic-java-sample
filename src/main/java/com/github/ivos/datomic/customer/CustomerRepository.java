package com.github.ivos.datomic.customer;

import com.github.ivos.datomic.support.DatomicRepository;

public class CustomerRepository extends DatomicRepository<Customer> {

	public CustomerRepository() {
		super("sample", Customer.class);
	}

}
