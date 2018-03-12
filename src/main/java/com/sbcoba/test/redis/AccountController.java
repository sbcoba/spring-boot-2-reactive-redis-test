package com.sbcoba.test.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("accounts")
@RestController
public class AccountController {

    /*
     * Non blocking  
     */
    private final ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;
    /*
     * Blocking
     */
    private final RedisConnectionFactory redisConnectionFactory;

    /*
     * Non blocking
     */
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    
    private GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer("_type");

    public AccountController(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory,
                             RedisConnectionFactory redisConnectionFactory, 
                             @Qualifier("jsonObject") ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.reactiveRedisConnectionFactory = reactiveRedisConnectionFactory;
        this.redisConnectionFactory = redisConnectionFactory;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public void setJsonRedisSerializer(GenericJackson2JsonRedisSerializer jsonRedisSerializer) {
        this.jsonRedisSerializer = jsonRedisSerializer;
    }

    @GetMapping("{username}")
    public Mono<Object> findByUsername(@PathVariable String username) {

//        reactiveRedisConnectionFactory.getReactiveConnection()
//                .stringCommands().set(t -> t.onNext());
        return reactiveRedisTemplate.opsForValue()
                .get(getPrefix() + username);
    }

    @PostMapping
    public Mono<String> create(@RequestBody Account account) {
        return reactiveRedisTemplate.opsForValue()
                .set(getPrefix() + account.getUsername(), account)
                .thenReturn("success");
    }

    /**
     * keys로 논블로킹 형태가 가능하지만 
     * 실제 서비스에서는 사용하면 안되는 형태
     * @return
     */
    @GetMapping
    public Flux<Object> list() {
        ReactiveValueOperations<String, Object> reactiveValueOperations = reactiveRedisTemplate.opsForValue();
        return reactiveRedisTemplate.keys(getPrefix() + "*")
                .flatMap((String key) -> {
                    Mono<Object> mono = reactiveValueOperations.get(key);
                    return mono;
                });
    }

    /**
     * scan은 블로킹으로 작동함
     * @return
     */
    @GetMapping("scan")
    public Flux<Object> listByScan() {
        RedisConnection connection = redisConnectionFactory.getConnection();
        Cursor<byte[]> scan = connection.scan(ScanOptions.scanOptions().match(getPrefix() + "*").count(20).build());
        List<byte[]> keys = new ArrayList<>();
        scan.forEachRemaining(keys::add);
        List<byte[]> values = connection.mGet(keys.toArray(new byte[][]{}));
        return Flux.just(values.stream().map(jsonRedisSerializer::deserialize).toArray());
    }
    
    private String getPrefix() {
        return "Account:";
    }
}
