/*

stimulus-package : Give money to players based on economic activity.

Copyright (c) 2018-2021 Queued Pixel <git@queuedpixel.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/

package com.queuedpixel.stimuluspackage.test;

import com.queuedpixel.stimuluspackage.PaymentQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class PaymentQueueTest
{
    @Test
    void addPayment_nullPlayerId()
    {
        TestPaymentHandler paymentHandler = new TestPaymentHandler();
        PaymentQueue paymentQueue = new PaymentQueue( paymentHandler );
        Exception exception = Assertions.assertThrows(
                IllegalArgumentException.class, () -> paymentQueue.addPayment( null, 5 ));
        Assertions.assertEquals( "Parameter 'playerId' cannot be null.", exception.getMessage(),
                                 "Unexpected exception message." );
    }

    @Test
    void addPayment_negativePayment()
    {
        TestPaymentHandler paymentHandler = new TestPaymentHandler();
        PaymentQueue paymentQueue = new PaymentQueue( paymentHandler );
        UUID playerId = UUID.fromString( "4440445f-f669-45a6-8865-46ca78e820a9" );
        Exception exception = Assertions.assertThrows(
                IllegalArgumentException.class, () -> paymentQueue.addPayment( playerId, -5 ));
        Assertions.assertEquals( "Parameter 'payment' must be greater than 0.", exception.getMessage(),
                                 "Unexpected exception message." );
    }

    @Test
    void makePayment_noPayments()
    {
        TestPaymentHandler paymentHandler = new TestPaymentHandler();
        PaymentQueue paymentQueue = new PaymentQueue( paymentHandler );
        paymentQueue.makePayment();
        Assertions.assertEquals( 0, paymentHandler.payments.size(), "Unexpected number of payments." );
    }

    @Test
    void makePayment_onePayment()
    {
        TestPaymentHandler paymentHandler = new TestPaymentHandler();
        PaymentQueue paymentQueue = new PaymentQueue( paymentHandler );
        UUID playerId = UUID.fromString( "f6ab1d55-be73-4958-bcaf-9efa7c494a30" );
        paymentQueue.addPayment( playerId, 5 );
        paymentQueue.makePayment();
        paymentQueue.makePayment();
        Assertions.assertEquals( 1, paymentHandler.payments.size(), "Unexpected number of payments." );
        this.verifyPaymentData( playerId, 5, paymentHandler.payments.get( 0 ), "Payment 0" );
    }

    @Test
    void makePayment_multiplePayments()
    {
        TestPaymentHandler paymentHandler = new TestPaymentHandler();
        PaymentQueue paymentQueue = new PaymentQueue( paymentHandler );
        UUID playerId0 = UUID.fromString( "59f907f5-a6a6-4cc7-916b-e7542e1c152c" );
        UUID playerId1 = UUID.fromString( "aeaed089-a8fe-4944-9020-1e1361287156" );
        paymentQueue.addPayment( playerId0, 10 );
        paymentQueue.addPayment( playerId1, 11 );
        paymentQueue.makePayment();
        paymentQueue.makePayment();
        paymentQueue.makePayment();
        Assertions.assertEquals( 2, paymentHandler.payments.size(), "Unexpected number of payments." );
        this.verifyPaymentData( playerId0, 10, paymentHandler.payments.get( 0 ), "Payment 0" );
        this.verifyPaymentData( playerId1, 11, paymentHandler.payments.get( 1 ), "Payment 1" );
    }

    @Test
    void makePayment_doublePayment()
    {
        TestPaymentHandler paymentHandler = new TestPaymentHandler();
        PaymentQueue paymentQueue = new PaymentQueue( paymentHandler );
        UUID playerId = UUID.fromString( "0b7cb3d3-fa85-47f6-9073-b758d31cd24f" );
        paymentQueue.addPayment( playerId, 3 );
        paymentQueue.addPayment( playerId, 5 );
        paymentQueue.makePayment();
        paymentQueue.makePayment();
        Assertions.assertEquals( 1, paymentHandler.payments.size(), "Unexpected number of payments." );
        this.verifyPaymentData( playerId, 8, paymentHandler.payments.get( 0 ), "Payment 0" );
    }

    private void verifyPaymentData( UUID playerId, double payment, TestPaymentData paymentData, String messagePrefix )
    {
        Assertions.assertEquals( playerId, paymentData.playerId, messagePrefix + ": Unexpected player ID." );
        Assertions.assertEquals( payment,  paymentData.payment,  messagePrefix + ": Unexpected payment."   );
    }
}
