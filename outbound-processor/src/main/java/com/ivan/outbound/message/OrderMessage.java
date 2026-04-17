package com.ivan.outbound.message;

public record OrderMessage(
        String cid,   // customerId
        String pid,   // productId
        String nm,    // name
        int qty,
        double pr,    // original price
        String cur,   // original currency
        byte tp,      // OrderType.byteCode — compact category encoding
        String addr,  // shippingAddress
        boolean fp    // firstPurchase make a discount for firstPurchase
) {}