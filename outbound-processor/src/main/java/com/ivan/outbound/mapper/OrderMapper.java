package com.ivan.outbound.mapper;

import com.ivan.avro.OrderMessage;
import com.ivan.outbound.entity.Order;
import com.ivan.outbound.enumeration.OrderType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public abstract class OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "priceUsd", ignore = true) // set from HEADER_PRICE_USD in EnricherProcessor
    @Mapping(target = "name", source = "orderMessage.nm")
    @Mapping(target = "quantity", source = "orderMessage.qty")
    @Mapping(target = "type", expression = "java(mapOrderType(orderMessage.getTp()))")
    @Mapping(target = "shippingAddress", source = "orderMessage.addr")
    @Mapping(target = "firstPurchaseDiscountApplied", source = "orderMessage.fp")
    public abstract Order map(OrderMessage orderMessage);

    public OrderType mapOrderType(int tp) {
        return OrderType.fromCode(tp);
    }
}
