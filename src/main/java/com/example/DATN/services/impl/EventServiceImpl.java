package com.example.DATN.services.impl;

import com.example.DATN.dtos.EventsDTO;
import com.example.DATN.models.SalesEvent;
import com.example.DATN.repositories.EventRepository;
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

@Service
public class EventServiceImpl implements EventService {

   @Autowired
   private EventRepository eventRepository;

   @Autowired
   private ModelMapper modelMapper;

   private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // Ký tự an toàn, tránh nhầm lẫn

   private static final Random RANDOM = new Random();

   @Override
   public Page<EventsDTO> findAll(int page, int size) {
       Pageable pageable = PageRequest.of(page, size);
       System.out.println(pageable + "data");
       Page<SalesEvent> events = eventRepository.findAll(pageable);
       return events.map(entity -> modelMapper.map(entity, EventsDTO.class));
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
                       .maxDiscountValue(SalesEvent.getMaxDiscountValue())
                       .startDate(SalesEvent.getStartDate())
                       .endDate(SalesEvent.getEndDate())
                       .isActive(SalesEvent.getIsActive())
                       .build()).collect(Collectors.toList());
   }

   @Override
   public boolean toggleStatus(Integer id) {
       SalesEvent events = eventRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá "));
       events.setIsActive(!events.getIsActive());
       eventRepository.save(events);
       return events.getIsActive();
   }

   @Override
   public boolean addEvents(EventsRequest eventRequest) {
       try {
           SalesEvent events = fromRequest(eventRequest);
           events.setDiscountType(eventRequest.getDiscountType()); // Nếu model có field type
           eventRepository.save(events);
           return true;
       } catch (Exception e) {
           e.printStackTrace();
           return false;
       }
   }

   @Override
   public boolean updateEvents(Integer id, EventsRequest eventRequest) {
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
               eventRepository.save(events);
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

   public SalesEvent fromRequest(EventsRequest eventRequest) {


       SalesEvent.SalesEventBuilder EventBuilder = SalesEvent.builder()
               .id(eventRequest.getId())
               .name(eventRequest.getName())
               .code(eventRequest.getCode() != null && !eventRequest.getCode().trim().isEmpty()
                       ? eventRequest.getCode()
                       : generateDiscountCode())
               .discountValue(eventRequest.getDiscountValue())
               .discountType(eventRequest.getDiscountType())
               .maxDiscountValue(eventRequest.getMaxDiscountValue())
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
}

