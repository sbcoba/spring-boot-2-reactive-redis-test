package com.sbcoba.test.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisConfiguration.class)
public class WebTests {
    
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testWelcome() {
        this.webTestClient.get().uri("/accounts").accept(MediaType.TEXT_PLAIN).exchange()
                .expectBody(String.class).isEqualTo("Hello World");
    }

    @Test
    public void testPut() {
        Account account = Account.builder()
                .email("sbcoba@gmail.com")
                .phone("01054821564")
                .username("sbcoba").build();
        this.webTestClient.post().uri("/accounts").contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(account), Account.class).exchange()
                .expectBody(String.class).isEqualTo("success");
    }

    @Test
    public void testActuatorStatus() {
        this.webTestClient.get().uri("/accounts/sbcoba").accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk().expectBody()
                .json("{\"email\":\"sbcoba@gmail.com\"}");
    }
}
