package com.alvin.bookingsystem.util;

import org.springframework.stereotype.Component;

@Component
public class MockService {
    
    /**
     * Mock function for adding payment card
     * @param cardNumber Card number
     * @param cardHolderName Card holder name
     * @param expiryDate Expiry date
     * @param cvv CVV
     * @return true if successful
     * @throws RuntimeException if payment fails
     */
    public boolean addPaymentCard(String cardNumber, String cardHolderName, String expiryDate, String cvv) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            throw new RuntimeException("Invalid card number");
        }
        return true;
    }
    
    /**
     * Mock function for payment charge
     * @param amount Amount to charge
     * @param paymentMethod Payment method
     * @param paymentCardId Payment card ID
     * @return true if successful
     * @throws RuntimeException if payment fails
     */
    public boolean paymentCharge(Long amount, String paymentMethod, String paymentCardId) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Invalid payment amount");
        }
        return true;
    }
    
    /**
     * Mock function for sending verification email
     * @param email Email address
     * @param token Verification token
     * @param type Type of email (VERIFICATION or PASSWORD_RESET)
     * @return true if successful
     * @throws RuntimeException if email sending fails
     */
    public boolean sendVerifyEmail(String email, String token, String type) {
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Invalid email address");
        }
        return true;
    }
}
