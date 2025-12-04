package com.example.DATN.services.impl;

import com.example.DATN.dtos.EventsDTO;
import com.example.DATN.dtos.SaleEventProductDTO;
import com.example.DATN.models.SalesEvent;
import com.example.DATN.models.SaleEventProduct;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.repositories.EventRepository;
import com.example.DATN.repositories.SaleEventProductRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.services.EventService;
import com.example.DATN.request.EventsRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
public class EventServiceImpl implements EventService {

   @Autowired
   private EventRepository eventRepository;

   @Autowired
   private SaleEventProductRepository saleEventProductRepository;

   @Autowired
   private ProductVariantRepository productVariantRepository;

   @Autowired
   private ModelMapper modelMapper;

   private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // Ký tự an toàn, tránh nhầm lẫn

   private static final Random RANDOM = new Random();

   @Override
   public Page<EventsDTO> findAll(int page, int size) {
       Pageable pageable = PageRequest.of(page, size);
       System.out.println(pageable + "data");
       Page<SalesEvent> events = eventRepository.findAll(pageable);
       return events.map(entity -> {
           EventsDTO dto = modelMapper.map(entity, EventsDTO.class);
           // Load sale event products
           List<SaleEventProduct> seps = saleEventProductRepository.findBySalesEventId(entity.getId());
           List<SaleEventProductDTO> sepDtos = seps.stream()
                   .map(sep -> SaleEventProductDTO.builder()
                           .id(sep.getId())
                           .salesEventId(sep.getSalesEvent().getId())
                           .productVariantId(sep.getProductVariant().getId())
                           .productVariantName(sep.getProductVariant().getVariantCode()) // Assuming variantCode as name
                           .finalPrice(sep.getFinalPrice())
                           .build())
                   .collect(Collectors.toList());
           dto.setSaleEventProducts(sepDtos);
           // Set product variant IDs for easy access in templates
           dto.setProductVariantIds(seps.stream()
                   .map(sep -> sep.getProductVariant().getId())
                   .collect(Collectors.toList()));
           return dto;
       });
   }

   @Override
   public List<EventsDTO> getEvents(String keyword) {
       List<SalesEvent> events;

       if (keyword != null && !keyword.isBlank()) {
           events = eventRepository.findByCodeContainingIgnoreCase(keyword);
       } else {
           events = eventRepository.findAll();
       }

       return events.stream()
               .map(SalesEvent -> EventsDTO.builder()
                       .id(SalesEvent.getId())
                       .name(SalesEvent.getName())
                       .code(SalesEvent.getCode())
                       .discountValue(SalesEvent.getDiscountValue())
                       .discountType(SalesEvent.getDiscountType())
                       .startDate(SalesEvent.getStartDate())
                       .endDate(SalesEvent.getEndDate())
                       .isActive(SalesEvent.getIsActive())
                       .build()).collect(Collectors.toList());
   }

   @Override
   public boolean toggleStatus(Integer id) {
       if (id == null) return false;
       Optional<SalesEvent> eventOpt = eventRepository.findById(id);
       if (eventOpt.isPresent()) {
           SalesEvent events = eventOpt.get();
           events.setIsActive(!events.getIsActive());
           eventRepository.save(events);
           return events.getIsActive();
       }
       return false;
   }

   @Override
   public boolean addEvents(EventsRequest eventRequest) {
       try {
           SalesEvent events = fromRequest(eventRequest);
           events.setDiscountType(eventRequest.getDiscountType());
           SalesEvent savedEvent = eventRepository.save(events);

           // Save SaleEventProducts if productVariantIds provided
           if (eventRequest.getProductVariantIds() != null && !eventRequest.getProductVariantIds().isEmpty()) {
               for (Integer variantId : eventRequest.getProductVariantIds()) {
                   if (variantId != null) {
                       Optional<ProductVariant> variantOpt = productVariantRepository.findById(variantId);
                       if (variantOpt.isPresent()) {
                           SaleEventProduct sep = new SaleEventProduct();
                           sep.setSalesEvent(savedEvent);
                           sep.setProductVariant(variantOpt.get());
                           sep.setFinalPrice(calculateFinalPrice(variantOpt.get(), events));
                           saleEventProductRepository.save(sep);
                       }
                   }
               }
           }

           return true;
       } catch (Exception e) {
           e.printStackTrace();
           return false;
       }
   }

   @Override
   public boolean updateEvents(Integer id, EventsRequest eventRequest) {
       if (id == null) return false;
       try {
           Optional<SalesEvent> optionalEvent = eventRepository.findById(id);
           if (optionalEvent.isPresent()) {
               SalesEvent events = optionalEvent.get();
               modelMapper.map(eventRequest, events); // Map các field chung
               events.setId(id);
               if (eventRequest.getCode() != null && !eventRequest.getCode().trim().isEmpty()) {
                   events.setCode(eventRequest.getCode());
               }
               events.setDiscountType(eventRequest.getDiscountType()); // Giữ type
               SalesEvent savedEvent = eventRepository.save(events);

               // Update SaleEventProducts
               saleEventProductRepository.deleteBySalesEventId(id); // Delete old ones
               if (eventRequest.getProductVariantIds() != null && !eventRequest.getProductVariantIds().isEmpty()) {
                   for (Integer variantId : eventRequest.getProductVariantIds()) {
                       if (variantId != null) {
                           Optional<ProductVariant> variantOpt = productVariantRepository.findById(variantId);
                           if (variantOpt.isPresent()) {
                               SaleEventProduct sep = new SaleEventProduct();
                               sep.setSalesEvent(savedEvent);
                               sep.setProductVariant(variantOpt.get());
                               sep.setFinalPrice(calculateFinalPrice(variantOpt.get(), events));
                               saleEventProductRepository.save(sep);
                           }
                       }
                   }
               }

               return true;
           }
           return false;
       } catch (Exception e) {
           e.printStackTrace();
           return false;
       }
   }
   @Override
   public Page<EventsDTO> searchEvents(String keyword, Boolean isActive, Pageable pageable) {
       Page<SalesEvent> Events = eventRepository.findBySearch(keyword, isActive, pageable);
       return Events.map(entity -> modelMapper.map(entity, EventsDTO.class));
   }

   @Override
   public long countAll() {
       return eventRepository.count();
   }

   @Override
   public EventsDTO findById(Integer id) {
       if (id == null) return null;
       Optional<SalesEvent> eventOpt = eventRepository.findById(id);
       if (eventOpt.isPresent()) {
           SalesEvent event = eventOpt.get();
           EventsDTO dto = modelMapper.map(event, EventsDTO.class);
           // Load sale event products
           List<SaleEventProduct> seps = saleEventProductRepository.findBySalesEventId(event.getId());
           List<SaleEventProductDTO> sepDtos = seps.stream()
                   .map(sep -> SaleEventProductDTO.builder()
                           .id(sep.getId())
                           .salesEventId(sep.getSalesEvent().getId())
                           .productVariantId(sep.getProductVariant().getId())
                           .productVariantName(sep.getProductVariant().getVariantCode())
                           .finalPrice(sep.getFinalPrice())
                           .build())
                   .collect(Collectors.toList());
           dto.setSaleEventProducts(sepDtos);
           // Set product variant IDs for easy access in templates
           dto.setProductVariantIds(seps.stream()
                   .map(sep -> sep.getProductVariant().getId())
                   .collect(Collectors.toList()));
           return dto;
       }
       return null;
   }

   public SalesEvent fromRequest(EventsRequest eventRequest) {


       SalesEvent.SalesEventBuilder EventBuilder = SalesEvent.builder()
               .id(eventRequest.getId())
               .name(eventRequest.getName())
               .code(eventRequest.getCode() != null && !eventRequest.getCode().trim().isEmpty()
                       ? eventRequest.getCode()
                       : generateDiscountCode())
               .discountValue(eventRequest.getDiscountValue())
               .discountType(eventRequest.getDiscountType())
               .startDate(eventRequest.getStartDate() != null ? eventRequest.getStartDate().atStartOfDay() : null)
               .endDate(eventRequest.getEndDate() != null ? eventRequest.getEndDate().atTime(23, 59, 59) : null)  // Kết thúc ngày: 23:59:59
               .isActive(eventRequest.getIsActive());


       return EventBuilder.build();
   }


   public String generateDiscountCode() {
       int length = RANDOM.nextBoolean() ? 6 : 8;

       StringBuilder code = new StringBuilder();
       for (int i = 0; i < length; i++) {
           int index = RANDOM.nextInt(CHARSET.length());
           code.append(CHARSET.charAt(index));
       }

       return code.toString();
   }

   private BigDecimal calculateFinalPrice(ProductVariant variant, SalesEvent event) {
       BigDecimal originalPrice = variant.getPrice();
       BigDecimal discountValue = event.getDiscountValue();
       String discountType = event.getDiscountType();

       if ("PERCENT".equals(discountType)) {
           BigDecimal discountAmount = originalPrice.multiply(discountValue.divide(BigDecimal.valueOf(100)));
           return originalPrice.subtract(discountAmount);
       } else if ("FIXED".equals(discountType)) {
           return originalPrice.subtract(discountValue);
       }
       return originalPrice;
   }
}

