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

import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.plugin.java.JavaPlugin;

public class StimulusPackagePlugin extends JavaPlugin
{
    private LinkedList< Transaction > transactions = new LinkedList< Transaction >();

    public void onEnable()
    {
        getLogger().info( "onEnable is called!" );
        this.getCommand( "stimulus" ).setExecutor( new StimulusCommand( this ));
        this.getServer().getPluginManager().registerEvents( new StimulusPackageListener( this ), this );
    }

    public void onDisable()
    {
        getLogger().info( "onDisable is called!" );
    }

    protected void addTransaction( Transaction transaction )
    {
        this.transactions.add( transaction );
    }

    protected Iterator< Transaction > getTransactionIterator()
    {
        return this.transactions.descendingIterator();
    }
}
