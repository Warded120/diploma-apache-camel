package com.ivan.inbound.mapper;

import com.ivan.avro.OrderMessage;
import com.ivan.inbound.dto.OrderDto;
import com.ivan.inbound.enumeration.OrderType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public abstract class OrderMessageMapper {
    @Mapping(target = "cid", source = "customerId")
    @Mapping(target = "pid", source = "productId")
    @Mapping(target = "nm", source = "name")
    @Mapping(target = "qty", source = "quantity")
    @Mapping(target = "pr", source = "price")
    @Mapping(target = "cur", source = "currency")
    @Mapping(target = "tp", expression = "java(mapOderType(orderDto.type()))")
    @Mapping(target = "addr", source = "shippingAddress")
    @Mapping(target = "fp", source = "firstPurchase")
    public abstract OrderMessage map(OrderDto orderDto);

    protected int mapOderType(String code) {
        return OrderType.fromCode(code);
    }
}
