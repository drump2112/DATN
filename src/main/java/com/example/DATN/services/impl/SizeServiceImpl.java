package com.example.DATN.services.impl;


import java.util.List;
import java.util.stream.Collectors;


import com.example.DATN.dtos.SizeDTO;
import com.example.DATN.exception.BusinessException;
import com.example.DATN.models.Size;
import com.example.DATN.repositories.SizeRepository;
import com.example.DATN.request.SizeRequest;
import com.example.DATN.services.SizeService;


import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class SizeServiceImpl implements SizeService {


    @Autowired
    private SizeRepository sizeRepository;
    @Autowired
    private ModelMapper modelMapper;


    private static final String PREFIX = "SIZE";


    @Override
    public Page<SizeDTO> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);


        Page<Size> sizes = sizeRepository.findAll(pageable);


        return sizes.map(entity -> modelMapper.map(entity, SizeDTO.class));


    }


    @Override
    public List<SizeDTO> getSizes(String keyword) {
        List<Size> sizes;


        if (keyword != null && !keyword.isBlank()) {
            sizes = sizeRepository.findByNameContainingIgnoreCase(keyword);
        } else {
            sizes = sizeRepository.findAll();
        }


        return sizes.stream()
                .map(size -> SizeDTO.builder()
                        .id(size.getId())
                        .name(size.getName())
                        .build())
                .collect(Collectors.toList());
    }


    @Override
    public boolean toggleStatus(Integer id) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước"));
        size.setIsActive(!size.getIsActive());
        sizeRepository.save(size);
        return size.getIsActive();
    }


    @Override
    public boolean addSize(SizeRequest sizerequest) {
        if (sizeRepository.existsByName(sizerequest.getName())) {
            throw new BusinessException("Kích thước đã tồn tại");
        }
        Size size = fromRequest(sizerequest);
        sizeRepository.save(size);
        return true;
    }


    @Override
    public boolean updateSize(Integer id, SizeRequest sizerequet) {
//         check tồn tại
        Size existingSize = sizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước có id: " + id));
        existingSize.setName(sizerequet.getName());
        existingSize.setSizeCode(sizerequet.getSizeCode());
        sizeRepository.save(existingSize);
        return true;
    }


    @Override
    public Page<SizeDTO> searchSize(String keyword, Boolean isActive, Pageable pageable) {
        Page<Size> sizes = sizeRepository.search(keyword, isActive, pageable);
        return sizes.map(entity -> modelMapper.map(entity, SizeDTO.class));
    }


    @Override
	public long countAll() {
        return sizeRepository.count();
    }

    public Size fromRequest(SizeRequest req) {


        Size.SizeBuilder sizeBuilder = Size.builder()
                .sizeCode(req.getSizeCode())
                .name(req.getName())
                .isActive(true);


        String sizeCode = generateSizeCode();
        sizeBuilder.sizeCode(sizeCode);
        return sizeBuilder.build();
    }




    public String generateSizeCode() {
        Long maxId = sizeRepository.findMaxId();
        if (maxId == null) {
            maxId = 0L;
        }
        return String.format("%s-%03d", PREFIX, maxId + 1);
    }
}

