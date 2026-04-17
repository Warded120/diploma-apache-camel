package com.ivan.outbound.mapper;

import com.ivan.outbound.entity.Order;
import com.ivan.outbound.message.OrderMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public abstract class OrderMapper {

    @Mapping(target = "id", ignore = true)
    public abstract Order map(OrderMessage orderDto);
}
