package com.ivan.outbound.mapper;

import com.ivan.avro.OrderMessage;
import com.ivan.outbound.entity.Customer;
import com.ivan.outbound.entity.Order;
import com.ivan.outbound.entity.Product;
import com.ivan.outbound.enumeration.OrderType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public abstract class OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", expression = "java(mapCustomer(orderMessage.getCid()))")
    @Mapping(target = "product", expression = "java(mapProduct(orderMessage.getPid()))")
    @Mapping(target = "name", source = "nm")
    @Mapping(target = "quantity", source = "qty")
    @Mapping(target = "priceUsd", source = "pr")
    @Mapping(target = "type", expression = "java(mapOrderType(orderMessage.getTp()))")
    @Mapping(target = "shippingAddress", source = "addr")
    @Mapping(target = "firstPurchaseDiscountApplied", source = "fp")
    public abstract Order map(OrderMessage orderMessage);

    public Customer mapCustomer(Long cid) {
        return null;
    }

    public Product mapProduct(Long pid) {
        return null;
    }

    public OrderType mapOrderType(int tp) {
        return OrderType.fromCode(tp);
    }
}
