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

public class SortedLine< T extends Comparable< T >> implements Comparable< SortedLine< T >>
{
    public final T sortValue;
    public final String line;

    public SortedLine( T sortValue, String line )
    {
        this.sortValue = sortValue;
        this.line = line;
    }

    public int compareTo( SortedLine< T > other )
    {
        if ( other == null ) throw new NullPointerException();
        int result = sortValue.compareTo( other.sortValue );
        if ( result != 0 ) return result;
        return line.compareTo( other.line );
    }

    public boolean equals( Object other )
    {
        if ( !( other instanceof SortedLine )) return false;
        SortedLine< ? > otherLine = (SortedLine< ? >) other;
        if ( !( this.sortValue.equals( otherLine.sortValue ))) return false;
        if ( !( this.line.equals( otherLine.line ))) return false;
        return true;
    }
}
