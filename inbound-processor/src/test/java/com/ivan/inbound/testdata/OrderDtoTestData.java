package com.ivan.inbound.testdata;

import com.ivan.inbound.dto.OrderDto;

import static com.ivan.inbound.testutil.TestObjectMapperUtil.mapToJson;

public class OrderDtoTestData {
    public static OrderDto getOrderDto() {
        return new OrderDto(
                "product",
                1,
                100.0
        );
    }

    public static String getOrderDtoJson() {
        return mapToJson(getOrderDto());
    }
}
