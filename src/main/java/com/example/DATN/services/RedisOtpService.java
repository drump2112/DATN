package com.example.DATN.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisOtpService {

  @Autowired
  StringRedisTemplate redisTemplate;

  public RedisOtpService(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public void saveOtp(String email, String otp, Integer orderId) {
    String key = "OTP:" + orderId + ":" + email;
    redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(10)); // TTL 10 phút
  }

  public String getOtp(String email, Integer orderId) {
    String key = "OTP:" + orderId + ":" + email;
    return redisTemplate.opsForValue().get(key);
  }

  public void deleteOtp(String email, Integer orderId) {
    String key = "OTP:" + orderId + ":" + email;
    redisTemplate.delete(key);
  }
}
