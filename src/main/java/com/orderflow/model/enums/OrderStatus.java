package com.orderflow.model.enums;

public enum OrderStatus {
    PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED;
    
    public boolean canTransitionTo(OrderStatus newStatus) {
        switch (this) {
            case PENDING: 
                return newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED: 
                return newStatus == PROCESSING || newStatus == CANCELLED;
            case PROCESSING: 
                return newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED: 
                return newStatus == DELIVERED || newStatus == REFUNDED;
            default: 
                return false;
        }
    }
}
