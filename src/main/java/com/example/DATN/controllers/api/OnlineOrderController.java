package com.example.DATN.controllers.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders/online")
public class OnlineOrderController {

  @GetMapping("/")
  public String getMethodName() {
    return "shop/checkout";
  }

}
