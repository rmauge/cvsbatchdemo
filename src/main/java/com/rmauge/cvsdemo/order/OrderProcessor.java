package com.rmauge.cvsdemo.order;

import org.springframework.batch.item.ItemProcessor;

import java.util.HashMap;
import java.util.Map;

public class OrderProcessor implements ItemProcessor<Order, Bill> {

    private static Map<String, Double> shipping = new HashMap<>();

    static {
        shipping.put("10471", 5.99);
    }

    @Override
    public Bill process(final Order order) {
        return new Bill(order.getId(), shipping.get(order.getZip()));
    }
}