/*

stimulus-package : Give money to players based on economic activity.

Copyright (c) 2018-2020 Queued Pixel <git@queuedpixel.com>

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.queuedpixel.stimuluspackage.StimulusUtil;

class StimulusUtilTest
{
    @Test
    void getMaxLength_testNoValues()
    {
        Assertions.assertEquals( 0, StimulusUtil.getMaxLength() );
    }

    @Test
    void getMaxLength_testSingleValue()
    {
        Assertions.assertEquals( 4, StimulusUtil.getMaxLength( "test" ));
    }

    @Test
    void getMaxLength_testMultipleValues()
    {
        Assertions.assertEquals( 7, StimulusUtil.getMaxLength( "this", "is", "an", "example" ));
    }

    @Test
    void round_negativePrecision()
    {
        Assertions.assertEquals( 5.12345, StimulusUtil.round( -23, 5.12345 ));
    }

    @Test
    void round_positivePrecision()
    {
        Assertions.assertEquals( 5.1235, StimulusUtil.round( 4, 5.12345 ));
    }
}
