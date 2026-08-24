package com.ecommerce.notification.service;

import com.ecommerce.notification.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @KafkaListener(
            topics = "order-events",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: {} for order ID: {}", event.getEventType(), event.getOrderId());

        try {
            switch (event.getEventType()) {
                case "ORDER_CREATED":
                    handleOrderCreated(event);
                    break;
                case "ORDER_STATUS_UPDATED":
                    handleOrderStatusUpdated(event);
                    break;
                case "ORDER_CANCELLED":
                    handleOrderCancelled(event);
                    break;
                case "ORDER_FAILED":
                    handleOrderFailed(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing notification for order: {}", event.getOrderId(), e);
            handleNotificationError(event, e);
        }
    }

    private void handleOrderCreated(OrderEvent event) {
        log.info("Sending order confirmation notification for customer: {} order: {}",
                event.getCustomerId(), event.getOrderId());
        
        sendOrderConfirmationEmail(event);
        sendOrderConfirmationSMS(event);
        sendPushNotification(event, "Order Confirmed", 
                "Your order #" + event.getOrderId() + " has been confirmed!");
        
        log.info("Order confirmation notifications sent for order: {}", event.getOrderId());
    }

    private void handleOrderStatusUpdated(OrderEvent event) {
        log.info("Sending status update notification - Order: {}, Status: {}",
                event.getOrderId(), event.getStatus());
        
        String subject = "Order Status Updated: " + event.getStatus();
        String message = "Your order #" + event.getOrderId() + " is now " + event.getStatus();
        
        sendStatusUpdateEmail(event, subject);
        sendPushNotification(event, "Order " + event.getStatus(), message);
        
        log.info("Status update notifications sent for order: {}", event.getOrderId());
    }

    private void handleOrderCancelled(OrderEvent event) {
        log.info("Sending order cancellation notification for order: {}", event.getOrderId());
        sendCancellationEmail(event);
        sendPushNotification(event, "Order Cancelled", 
                "Your order #" + event.getOrderId() + " has been cancelled.");
        log.info("Cancellation notifications sent for order: {}", event.getOrderId());
    }

    private void handleOrderFailed(OrderEvent event) {
        log.error("Sending order failure notification for order: {}", event.getOrderId());
        sendFailureEmail(event);
        sendPushNotification(event, "Order Failed", 
                "Your order #" + event.getOrderId() + " processing failed. Please try again.");
        log.info("Failure notifications sent for order: {}", event.getOrderId());
    }

    private void sendOrderConfirmationEmail(OrderEvent event) {
        log.info("Sending confirmation email to customer: {}", event.getCustomerId());
        log.info("Email sent: Order confirmation for customer {}", event.getCustomerId());
    }

    private void sendOrderConfirmationSMS(OrderEvent event) {
        log.info("Sending confirmation SMS to customer: {}", event.getCustomerId());
        log.info("SMS sent: Order confirmation for customer {}", event.getCustomerId());
    }

    private void sendStatusUpdateEmail(OrderEvent event, String subject) {
        log.info("Sending status update email to customer: {} - Subject: {}",
                event.getCustomerId(), subject);
        log.info("Email sent: {} for order {}", subject, event.getOrderId());
    }

    private void sendCancellationEmail(OrderEvent event) {
        log.info("Sending cancellation email to customer: {}", event.getCustomerId());
        log.info("Email sent: Order cancellation for customer {}", event.getCustomerId());
    }

    private void sendFailureEmail(OrderEvent event) {
        log.error("Sending failure email to customer: {}", event.getCustomerId());
        log.info("Email sent: Order failure notification for customer {}", event.getCustomerId());
    }

    private void sendPushNotification(OrderEvent event, String title, String body) {
        log.info("Sending push notification - Title: {}, Body: {}", title, body);
        log.info("Push notification sent for order: {}", event.getOrderId());
    }

    private void handleNotificationError(OrderEvent event, Exception ex) {
        log.error("Failed to send notification for order: {} - Error: {}",
                event.getOrderId(), ex.getMessage(), ex);
    }
}
