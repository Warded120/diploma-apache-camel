package com.ivan.outbound.mapper;

import com.ivan.outbound.dto.OrderDto;
import com.ivan.outbound.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public abstract class OrderMapper {

    @Mapping(target = "id", ignore = true)
    public abstract Order map(OrderDto orderDto);
}
