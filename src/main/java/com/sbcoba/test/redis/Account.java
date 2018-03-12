package com.sbcoba.test.redis;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Account {
    
    private String username;
    private String phone;
    private String email;
}
