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

package com.queuedpixel.stimuluspackage;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class PaymentQueue
{
    private final PaymentHandler paymentHandler;
    private final Deque< UUID > playerList = new LinkedList<>();
    private final Map< UUID, Double > paymentMap = new HashMap<>();

    public PaymentQueue( PaymentHandler paymentHandler )
    {
        this.paymentHandler = paymentHandler;
    }

    public void addPayment( UUID playerId, double payment )
    {
        if ( playerId == null ) throw new IllegalArgumentException( "Parameter 'playerId' cannot be null." );
        if ( payment <= 0 ) throw new IllegalArgumentException( "Parameter 'payment' must be greater than 0." );

        double totalPayment = payment;
        if ( paymentMap.containsKey( playerId )) totalPayment += this.paymentMap.get( playerId );
        else this.playerList.add( playerId );

        this.paymentMap.put( playerId, totalPayment );
    }

    public void makePayment()
    {
        if ( playerList.isEmpty() ) return;
        UUID playerId = this.playerList.removeFirst();
        this.paymentHandler.handlePayment( playerId, this.paymentMap.remove( playerId ));
    }

    public void makeAllPayments()
    {
        while ( !this.playerList.isEmpty() ) this.makePayment();
    }
}
