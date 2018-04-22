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

package com.queuedpixel.stimuluspackage.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.queuedpixel.stimuluspackage.SortedLine;

public class SortedLineTest
{
    @Test
    void testNull()
    {
        SortedLine< Integer > line = new SortedLine< Integer >( 5, "foo" );
        Assertions.assertThrows( NullPointerException.class, ()-> { line.compareTo( null ); } );
        Assertions.assertNotEquals( line, null, "Line equal to null." );
    }

    @Test
    void testDifferentSortValue()
    {
        SortedLine< Integer > lineOne = new SortedLine< Integer >( 5, "foo" );
        SortedLine< Integer > lineTwo = new SortedLine< Integer >( 12, "bar" );
        Assertions.assertTrue( lineOne.compareTo( lineTwo ) < 0, "Line one not less than line two." );
        Assertions.assertTrue( lineTwo.compareTo( lineOne ) > 0, "Line two not greater than line one." );
        Assertions.assertNotEquals( lineOne, lineTwo, "Two lines equal." );
    }

    @Test
    void testDifferentLine()
    {
        SortedLine< Integer > lineOne = new SortedLine< Integer >( 5, "foo" );
        SortedLine< Integer > lineTwo = new SortedLine< Integer >( 5, "bar" );
        Assertions.assertTrue( lineOne.compareTo( lineTwo ) > 0, "Line one not greater than line two." );
        Assertions.assertTrue( lineTwo.compareTo( lineOne ) < 0, "Line two not less than line one." );
        Assertions.assertNotEquals( lineOne, lineTwo, "Two lines equal." );
    }

    @Test
    void testEqual()
    {
        SortedLine< Integer > lineOne = new SortedLine< Integer >( 5, "foo" );
        SortedLine< Integer > lineTwo = new SortedLine< Integer >( 5, "foo" );
        Assertions.assertEquals( 0, lineOne.compareTo( lineTwo ));
        Assertions.assertEquals( 0, lineTwo.compareTo( lineOne ));
        Assertions.assertEquals( lineOne, lineTwo, "Two lines not equal." );
    }

    @Test
    void testWrongType()
    {
        SortedLine< Integer > lineOne = new SortedLine< Integer >( 5, "foo" );
        String lineTwo = "bar";
        Assertions.assertNotEquals( lineOne, lineTwo, "Two lines equal." );
    }
}
