package com.sbcoba.test.redis;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Example value object.
 *
 * @author Mark Paluch
 */
@Data
@RequiredArgsConstructor
public class Person {

	final String firstname;
	final String lastname;
}