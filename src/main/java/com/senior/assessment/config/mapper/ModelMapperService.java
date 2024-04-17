package com.senior.assessment.config.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModelMapperService {
    private final ModelMapper modelMapper;

    public <T> List<T> toList(Class<T> clazz, List<?> items) {
        return items.stream()
                .map(item -> modelMapper.map(item, clazz))
                .toList();
    }

    public <T> Set<T> toSet(Class<T> clazz, Set<?> items) {
        return items.stream()
                .map(item -> modelMapper.map(item, clazz))
                .collect(Collectors.toSet());
    }

    public <T> T toObject(Class<T> clazz, Object item) {
        if (item == null) return null;
        return modelMapper.map(item, clazz);
    }

//    public <T> PageResult<T> toPage(Class<T> clazz, Page<?> page) {
//        if (page == null) return new PageResult<>();
//        return new PageResult<>(
//                page.getTotalPages(),
//                page.getTotalElements(),
//                toList(clazz, page.getContent())
//        );
//    }

}