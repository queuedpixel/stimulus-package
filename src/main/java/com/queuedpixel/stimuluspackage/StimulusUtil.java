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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.LinkedList;

public class StimulusUtil
{
    public static void appendToFile( Path file, String line )
    {
        try
        {
            BufferedWriter writer = Files.newBufferedWriter(
                    file, StandardOpenOption.CREATE, StandardOpenOption.APPEND );
            writer.write( line );
            writer.newLine();
            writer.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    public static int getMaxLength( String... values )
    {
        Collection< String > list = new LinkedList< String >();
        for ( String value : values ) list.add( value );
        return StimulusUtil.getMaxLength( list );
    }

    public static int getMaxLength( Collection< String > values )
    {
        int result = 0;
        for ( String value : values ) if ( value.length() > result ) result = value.length();
        return result;
    }

    public static double round( int precision, double value )
    {
        if ( precision < 0 ) return value;
        double precisionFactor = Math.pow( 10, precision );
        return Math.round( value * precisionFactor ) / precisionFactor;
    }
}
