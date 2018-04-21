/*

stimulus-package : Give money to players based on economic activity.

Copyright (c) 2018 Queued Pixel <git@queuedpixel.com>

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

public class StimulusPackageConfiguration
{
    private final long   economicInterval     = 604800; // seconds; one week
    private final long   stimulusInterval     = 604800; // seconds; one week
    private final double desiredVolume        = 1000;   // currency; desired volume
    private final double desiredStimulus      = 1;      // currency; desired stimulus per player per payment
    private final double minimumPaymentFactor = 0;      // payment factor for the wealthiest player
    private final double claimBlockValue      = 1;      // currency; value of claim blocks

    long getEconomicInterval()
    {
        return this.economicInterval;
    }

    long getStimulusInterval()
    {
        return this.stimulusInterval;
    }

    double getDesiredVolume()
    {
        return this.desiredVolume;
    }

    double getDesiredStimulus()
    {
        return this.desiredStimulus;
    }

    double getMinimumPaymentFactor()
    {
        return this.minimumPaymentFactor;
    }

    double getClaimBlockValue()
    {
        return this.claimBlockValue;
    }
}
