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

import java.util.Map;
import java.util.UUID;

class StimulusInformation
{
    final int economicPlayers;
    final int stimulusPlayers;
    final double maximumEconomicActivity;
    final double actualEconomicActivity;
    final double economicActivityDelta;
    final double economicActivityFactor;
    final double maximumStimulus;
    final double availableStimulus;
    final Map< UUID, StimulusPlayerInformation > stimulusPlayerInformationMap;

    StimulusInformation( int economicPlayers, int stimulusPlayers,
                         double maximumEconomicActivity, double actualEconomicActivity,
                         double economicActivityDelta, double economicActivityFactor,
                         double maximumStimulus, double availableStimulus,
                         Map< UUID, StimulusPlayerInformation > stimulusPlayerInformationMap )
    {
        this.economicPlayers              = economicPlayers;
        this.stimulusPlayers              = stimulusPlayers;
        this.maximumEconomicActivity      = maximumEconomicActivity;
        this.actualEconomicActivity       = actualEconomicActivity;
        this.economicActivityDelta        = economicActivityDelta;
        this.economicActivityFactor       = economicActivityFactor;
        this.maximumStimulus              = maximumStimulus;
        this.availableStimulus            = availableStimulus;
        this.stimulusPlayerInformationMap = stimulusPlayerInformationMap;
    }
}
