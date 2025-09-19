//implement color 

package com.example.DATN.services.impl;


import java.util.List;
import java.util.stream.Collectors;


import com.example.DATN.dtos.ColorDTO;
import com.example.DATN.exception.BusinessException;
import com.example.DATN.models.Color;
import com.example.DATN.repositories.ColorRepository;
import com.example.DATN.request.ColorRequest;
import com.example.DATN.services.ColorService;


import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class ColorServiceImpl implements ColorService {

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static final String PREFIX = "Color";

    @Override
    public Page<ColorDTO> findAll(int page, int Color) {


        Pageable pageable = PageRequest.of(page, Color);


        Page<Color> color = colorRepository.findAll(pageable);


        return color.map(entity -> modelMapper.map(entity, ColorDTO.class));
    }

    @Override
    public List<ColorDTO> getColors(String keyword) {
        List<Color> colors;

        if (keyword != null && !keyword.isBlank()) {
            colors = colorRepository.findByNameContainingIgnoreCase(keyword);
        } else {
            colors = colorRepository.findAll();
        }

        return colors.stream()
                .map(color -> ColorDTO.builder()
                        .id(color.getId())
                        .colorCode(color.getColorCode())
                        .colorName(color.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public boolean toggleStatus(Integer id) {
        Color Color = colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc"));
        Color.setIsActive(!Color.getIsActive());
        colorRepository.save(Color);
        return Color.getIsActive();
    }

    @Override
    public boolean addColor(ColorRequest ColorRequest) {
        if (colorRepository.existsByName(ColorRequest.getName())) {
            throw new BusinessException("Màu sắc đã tồn tại");
        }
        Color Color = fromRequest(ColorRequest);
        colorRepository.save(Color);
        return true;
    }

    @Override
    public boolean updateColor(Integer id, ColorRequest ColorRequest) {
//         check tồn tại
        Color existingColor = colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước có id: " + id));
        existingColor.setName(ColorRequest.getName());
        existingColor.setColorCode(ColorRequest.getColorCode());
        colorRepository.save(existingColor);
        return true;
    }

    @Override
    public Page<ColorDTO> searchColor(String keyword, Boolean isActive, Pageable pageable) {
        Page<Color> Colors = colorRepository.search(keyword, isActive, pageable);
        return Colors.map(entity -> modelMapper.map(entity, ColorDTO.class));
    }

    public Color fromRequest(ColorRequest req) {

        Color.ColorBuilder ColorBuilder = Color.builder()
                .colorCode(req.getColorCode())
                .name(req.getName())
                .isActive(true);

        String ColorCode = generateColorCode();
        ColorBuilder.colorCode(ColorCode);
        return ColorBuilder.build();
    }

    private String generateColorCode() {
        Long maxId = colorRepository.findMaxId();
        if (maxId == null) {
            maxId = 0L;
        }
        return String.format("%s-%03d", PREFIX, maxId + 1);
    }
}

