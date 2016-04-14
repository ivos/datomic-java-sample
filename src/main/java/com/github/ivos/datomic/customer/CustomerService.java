package com.github.ivos.datomic.customer;

import lombok.Setter;

import java.util.List;

public class CustomerService {

	@Setter
	private CustomerRepository repository;

	public Customer create(Customer customer) {
		return repository.create(customer);
	}

	public Customer update(Customer customer) {
		return repository.update(customer);
	}

	public Customer get(Long id) {
		return repository.get(id);
	}

	public List<Customer> list(Customer query) {
		return repository.list(query);
	}

}
