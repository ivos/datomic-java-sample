package com.github.ivos.datomic.customer;

import lombok.*;
import lombok.experimental.Wither;

@Getter
@Wither
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Customer {

	private Long id;
	private Long version;
	private String name;
	private String email;
	private String phone;

}
