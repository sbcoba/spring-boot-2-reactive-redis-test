package com.sbcoba.test.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializationContext.RedisSerializationContextBuilder;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.PreDestroy;

@SpringBootApplication
public class RedisTestConfiguration {

	@Autowired RedisConnectionFactory factory;

	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory();
	}

	/**
	 * Configures a {@link ReactiveRedisTemplate} with {@link String} keys and values.
	 */
	@Bean
	public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
		return new ReactiveRedisTemplate<>(connectionFactory, RedisSerializationContext.string());
	}

	/**
	 * Configures a {@link ReactiveRedisTemplate} with {@link String} keys and a typed
	 * {@link Jackson2JsonRedisSerializer}.
	 */
	@Bean
	public ReactiveRedisTemplate<String, Person> reactiveJsonPersonRedisTemplate(
			ReactiveRedisConnectionFactory connectionFactory) {

		Jackson2JsonRedisSerializer<Person> serializer = new Jackson2JsonRedisSerializer<>(Person.class);
		RedisSerializationContextBuilder<String, Person> builder = RedisSerializationContext
				.newSerializationContext(new StringRedisSerializer());

		RedisSerializationContext<String, Person> serializationContext = builder.value(serializer).build();

		return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
	}

	/**
	 * Configures a {@link ReactiveRedisTemplate} with {@link String} keys and {@link GenericJackson2JsonRedisSerializer}.
	 */
	@Bean
	public ReactiveRedisTemplate<String, Object> reactiveJsonObjectRedisTemplate(
			ReactiveRedisConnectionFactory connectionFactory) {

		RedisSerializationContextBuilder<String, Object> builder = RedisSerializationContext
				.newSerializationContext(new StringRedisSerializer());

		RedisSerializationContext<String, Object> serializationContext = builder
				.value(new GenericJackson2JsonRedisSerializer("_type")).build();

		return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
	}

	/**
	 * Clear database before shut down.
	 */
	public @PreDestroy
	void flushTestDb() {
		factory.getConnection().flushDb();
	}
	
	
	public static void main(String[] args) {
		SpringApplication.run(RedisTestConfiguration.class, args);
	}
}
