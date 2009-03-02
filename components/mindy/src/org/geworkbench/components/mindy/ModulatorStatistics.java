package org.geworkbench.components.mindy;

import java.io.Serializable;

/**
 * Represents the statistics of a modulator. The statistics consist of:
 * Count (M#)
 * Mover (M+)
 * Munder(M-)
 *
 * @author mhall
 * @version $Id: ModulatorStatistics.java,v 1.1 2009-03-02 16:30:45 oshteynb Exp $
 */
public class ModulatorStatistics implements Serializable {
    protected int count;
    protected int mover;
    protected int munder;

    /**
     * Constructor.  Sets all three stats -- count (M#), mover(M+), and munder (M-)
     *
     * @param count
     * @param mover
     * @param munder
     */
    public ModulatorStatistics(int count, int mover, int munder) {
        this.count = count;
        this.mover = mover;
        this.munder = munder;
    }

    /**
     * Get the count (M#).
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * Get the mover (M+)
     *
     * @return the mover
     */
    public int getMover() {
        return mover;
    }

    /**
     * Get the munder (M-)
     *
     * @return the munder
     */
    public int getMunder() {
        return munder;
    }
}