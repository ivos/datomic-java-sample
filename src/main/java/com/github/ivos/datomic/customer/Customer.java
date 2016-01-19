package com.github.ivos.datomic.customer;

import lombok.*;
import lombok.experimental.Wither;

@Getter
@Wither
@Builder
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Customer {

	private Long id;
	private String name;
	private String email;
	private String phone;

}
