package com.example.DATN.services;

import com.example.DATN.dtos.EventsDTO;
import com.example.DATN.request.EventsRequest;
import com.sun.jdi.request.EventRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface EventService {

  Page<EventsDTO> findAll(int page, int size);

  List<EventsDTO> getEvents(String keyword);

  boolean toggleStatus(Integer id);

  boolean addEvents(EventsRequest eventsRequest);

  boolean updateEvents(Integer id, EventsRequest eventsRequest);

  Page<EventsDTO> searchEvents(String keyword, Boolean isActive, Pageable pageable);

  long countAll();
}
