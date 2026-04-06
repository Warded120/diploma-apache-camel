package com.ivan.inbound.mapper;

import com.ivan.inbound.dto.OrderDto;
import com.ivan.inbound.entity.Order;
import org.mapstruct.Mapper;

//TODO: add more logic to mapper (make mapping more complex: e.g. add enrichment logic etc.)
@Mapper
public interface OrderMapper {
    Order map(OrderDto orderDto);
}
