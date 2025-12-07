package com.example.DATN.controllers.admin;

import com.example.DATN.dtos.OrderDTO;
import com.example.DATN.models.OrderItem;

import org.springframework.ui.Model;

import com.example.DATN.services.OrderItemService;
import com.example.DATN.services.OrderService;
import com.example.DATN.services.OrderStatusService;
import com.example.DATN.enums.OrderStatus;
import com.example.DATN.models.Order;

import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/order/")
public class OrderController {

   @Autowired
   private OrderService orderService;

   @Autowired
   private OrderItemService orderItemService;

   @Autowired
   private OrderStatusService orderStatusService;

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

   @GetMapping("completed/")
   public String getAllCompletedOrders(
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "5") int size,
         Model model) {
      Page<OrderDTO> ordersDTOS = orderService.getCompletedOrders(page, size);
      model.addAttribute("pageTitle", "Danh sách Đơn Hàng Đã Hoàn Thành");
      model.addAttribute("orders", ordersDTOS.getContent());
      model.addAttribute("currentPage", ordersDTOS.getNumber());
      model.addAttribute("totalPages", ordersDTOS.getTotalPages());
      model.addAttribute("totalItems", ordersDTOS.getTotalElements());
      model.addAttribute("pageSize", ordersDTOS.getSize());

      return "admin/order/list";
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

   @GetMapping("search")
   public String searchOrders(
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "5") int size,
         @RequestParam(required = false) String keyword,
         @RequestParam(required = false) String orderType,
         @RequestParam(required = false) String paymentMethod,
         @RequestParam(required = false) String dateStart,
         @RequestParam(required = false) String dateEnd,
         HttpServletRequest request,
         Model model) {

      System.out.println("Order search request - Page: " + page + ", Type: " + orderType + ", Keyword: " + keyword);

      // Xác định request đến từ trang nào dựa trên referer
      String referer = request.getHeader("Referer");
      boolean isFromCompletedPage = referer != null && referer.contains("/completed/");

      LocalDate startDate = null;
      LocalDate endDate = null;

      try {
         if (dateStart != null && !dateStart.isEmpty()) {
            startDate = LocalDate.parse(dateStart);
         }
         if (dateEnd != null && !dateEnd.isEmpty()) {
            endDate = LocalDate.parse(dateEnd);
         }
      } catch (Exception e) {
         System.out.println("Invalid date format ignored");
      }

      // Chuyển đổi payment method từ frontend sang backend
      String backendPaymentMethod = null;
      if ("Tiền Mặt".equals(paymentMethod)) {
         backendPaymentMethod = "CASH";
      } else if ("Chuyển Khoản".equals(paymentMethod)) {
         backendPaymentMethod = "TRANSFER";
      } else if ("VNPay".equals(paymentMethod)) {
         backendPaymentMethod = "VNPAY";
      }

      Page<OrderDTO> ordersDTOS;
      String templatePath;

      if (isFromCompletedPage) {
         // Từ trang completed - luôn tìm trong completed orders, có thể filter theo orderType
         System.out.println("Searching COMPLETED orders from completed page with filter: " + orderType);
         ordersDTOS = orderService.searchCompletedOrdersWithTypeFilter(keyword, backendPaymentMethod, orderType, startDate, endDate, page, size);
         templatePath = "admin/order/table :: table";
      } else if ("Online".equals(orderType)) {
         System.out.println("Searching ONLINE orders");
         ordersDTOS = orderService.searchOnlineOrders(keyword, backendPaymentMethod, startDate, endDate, page, size);
         templatePath = "admin/order/tableonline :: table";
      } else if ("Offline".equals(orderType)) {
         System.out.println("Searching OFFLINE orders");
         ordersDTOS = orderService.searchOfflineOrders(keyword, backendPaymentMethod, startDate, endDate, page, size);
         templatePath = "admin/order/table :: table";
      } else {
         // Fallback - mặc định tìm completed orders
         System.out.println("Fallback - Searching COMPLETED orders");
         ordersDTOS = orderService.searchCompletedOrders(keyword, backendPaymentMethod, startDate, endDate, page, size);
         templatePath = "admin/order/table :: table";
      }

      System.out.println("Found " + ordersDTOS.getTotalElements() + " orders, " + ordersDTOS.getTotalPages() + " pages");

      model.addAttribute("orders", ordersDTOS.getContent());
      model.addAttribute("currentPage", ordersDTOS.getNumber());
      model.addAttribute("totalPages", ordersDTOS.getTotalPages());
      model.addAttribute("totalItems", ordersDTOS.getTotalElements());
      model.addAttribute("pageSize", ordersDTOS.getSize());

      return templatePath;
   }

   @PostMapping("valid-transitions")
   public ResponseEntity<Map<String, Object>> getValidTransitions(@RequestBody Map<String, String> request) {
      String currentStatus = request.get("currentStatus");

      List<OrderStatus> validTransitions = orderStatusService.getValidTransitions(currentStatus);

      Map<String, Object> response = Map.of(
         "validTransitions", validTransitions,
         "success", true
      );

      return ResponseEntity.ok(response);
   }

   @PostMapping("check-confirmation")
   public ResponseEntity<Map<String, Object>> checkConfirmation(@RequestBody Map<String, String> request) {
      String fromStatus = request.get("fromStatus");
      String toStatus = request.get("toStatus");

      boolean requiresConfirmation = orderStatusService.requiresConfirmation(fromStatus, toStatus);
      String message = requiresConfirmation ? orderStatusService.getConfirmationMessage(fromStatus, toStatus) : null;

      Map<String, Object> response = Map.of(
         "requiresConfirmation", requiresConfirmation,
         "message", message != null ? message : "",
         "success", true
      );

      return ResponseEntity.ok(response);
   }

   /**
    * Admin xác nhận đơn hàng (PENDING -> SHIPPING)
    */
   @PostMapping("{orderId}/confirm")
   public ResponseEntity<Map<String, Object>> confirmOrder(@PathVariable Integer orderId) {
      try {
         Order order = orderService.findById(orderId);
         if (order == null) {
            return ResponseEntity.badRequest().body(Map.of(
               "success", false,
               "message", "Đơn hàng không tồn tại"
            ));
         }

         // Kiểm tra trạng thái
         if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of(
               "success", false,
               "message", "Chỉ có thể xác nhận đơn hàng ở trạng thái chờ xử lý"
            ));
         }

         // Cập nhật trạng thái
         orderService.updateOrderStatus(orderId, OrderStatus.SHIPPING.name());

         return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Đã xác nhận đơn hàng và chuyển sang trạng thái giao hàng"
         ));

      } catch (Exception e) {
         return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Có lỗi xảy ra: " + e.getMessage()
         ));
      }
   }

   /**
    * Admin hủy đơn hàng (PENDING -> CANCELLED)
    */
   @PostMapping("{orderId}/cancel")
   public ResponseEntity<Map<String, Object>> cancelOrderByAdmin(@PathVariable Integer orderId) {
      try {
         Order order = orderService.findById(orderId);
         if (order == null) {
            return ResponseEntity.badRequest().body(Map.of(
               "success", false,
               "message", "Đơn hàng không tồn tại"
            ));
         }

         // Kiểm tra trạng thái
         if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of(
               "success", false,
               "message", "Chỉ có thể hủy đơn hàng ở trạng thái chờ xử lý"
            ));
         }

         // Cập nhật trạng thái
         orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED.name());

         return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Đã hủy đơn hàng thành công"
         ));

      } catch (Exception e) {
         return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Có lỗi xảy ra: " + e.getMessage()
         ));
      }
   }

}
