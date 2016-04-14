package com.github.ivos.datomic.support;

public class OptimisticLockException extends RuntimeException {

	public OptimisticLockException(String message) {
		super(message);
	}

}
