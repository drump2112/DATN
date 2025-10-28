package com.example.DATN.controllers.admin;

import com.example.DATN.dtos.OrderDTO;
import com.example.DATN.models.OrderItem;

import org.springframework.ui.Model;

import com.example.DATN.services.OrderItemService;
import com.example.DATN.services.OrderService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/order/")
public class OrderController {

   @Autowired
   private OrderService orderService;

   @Autowired
   private OrderItemService orderItemService;

   @GetMapping("Online/")
   public String getAllOrdersOnline(
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "5") int size,
         Model model) {
      Page<OrderDTO> ordersDTOS = orderService.getOnlineOrders(page, size);
      model.addAttribute("pageTitle", "Danh sách Hóa Đơn Online");
      model.addAttribute("orders", ordersDTOS.getContent());
      model.addAttribute("currentPage", ordersDTOS.getNumber());
      model.addAttribute("totalPages", ordersDTOS.getTotalPages());
      model.addAttribute("totalItems", ordersDTOS.getTotalElements());
      model.addAttribute("pageSize", ordersDTOS.getSize());

      return "admin/order/listonline";
   }

   @GetMapping("offline/")
   public String getAllOrdersOffline(
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "5") int size,
         Model model) {
      Page<OrderDTO> ordersDTOS = orderService.getOfflineOrders(page, size);
      model.addAttribute("pageTitle", "Danh sách Hóa Đơn Online");
      model.addAttribute("orders", ordersDTOS.getContent());
      model.addAttribute("currentPage", ordersDTOS.getNumber());
      model.addAttribute("totalPages", ordersDTOS.getTotalPages());
      model.addAttribute("totalItems", ordersDTOS.getTotalElements());
      model.addAttribute("pageSize", ordersDTOS.getSize());

      return "admin/order/listoffline";
   }

   @GetMapping("{orderId}/items")
   public String getOrderItems(@PathVariable Integer orderId, Model model) {
      OrderDTO order = orderService.getOrderById(orderId);
      List<OrderItem> items = orderItemService.getItemsByOrderId(orderId);
      model.addAttribute("order", order);
      model.addAttribute("items", items);
      return "admin/order/orderitems :: orderItems";
   }

   @PutMapping("/{orderId}/status")
   public ResponseEntity<?> updateOrderStatus(
         @PathVariable Integer orderId,
         @RequestParam String status) {
      try {
         orderService.updateOrderStatus(orderId, status);
         return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái thành công"));
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
      }
   }

}
