package com.ivan.outbound.mapper;

import com.ivan.avro.OrderMessage;
import com.ivan.outbound.entity.Order;
import com.ivan.outbound.enumeration.OrderType;
import org.apache.camel.Exchange;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static com.ivan.outbound.constants.ExchangeConstants.DISCOUNT;

@Mapper
public abstract class OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "name", source = "orderMessage.nm")
    @Mapping(target = "quantity", source = "orderMessage.qty")
    @Mapping(target = "priceUsd", expression = "java(mapPrice(orderMessage.getPr(), orderMessage.getFp()))")
    @Mapping(target = "type", expression = "java(mapOrderType(orderMessage.getTp()))")
    @Mapping(target = "shippingAddress", source = "orderMessage.addr")
    @Mapping(target = "firstPurchaseDiscountApplied", source = "orderMessage.fp")
    public abstract Order map(OrderMessage orderMessage);

    public OrderType mapOrderType(int tp) {
        return OrderType.fromCode(tp);
    }

    public double mapPrice(double basePrice, boolean firstPurchase) {
        return firstPurchase ? basePrice * (1 - DISCOUNT) : basePrice;
    }
}
