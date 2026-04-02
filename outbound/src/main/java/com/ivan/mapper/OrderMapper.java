package com.ivan.mapper;

import com.ivan.dto.OrderDto;
import com.ivan.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public abstract class OrderMapper {

    @Mapping(target = "id", ignore = true)
    public abstract Order map(OrderDto orderDto);
}
