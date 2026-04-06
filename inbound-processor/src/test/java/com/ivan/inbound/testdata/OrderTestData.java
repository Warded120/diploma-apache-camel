package com.ivan.inbound.testdata;

import com.ivan.inbound.entity.Order;

import static com.ivan.inbound.testutil.TestObjectMapperUtil.mapToJson;

public class OrderTestData {
    public static Order getOrder() {
        return new Order(
                "product",
                1,
                100.0
        );
    }

    public static String getOrderJson() {
        return mapToJson(getOrder());
    }
}
