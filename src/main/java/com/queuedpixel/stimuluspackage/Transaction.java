package com.queuedpixel.stimuluspackage;

public class Transaction
{
    // milliseconds since epoch
    private final long timestamp;

    // amount of the transaction
    private final double amount;

    public Transaction( long timestamp, double amount )
    {
        this.timestamp = timestamp;
        this.amount = amount;
    }

    public long getTimestamp()
    {
        return this.timestamp;
    }

    public double getAmount()
    {
        return this.amount;
    }
}
