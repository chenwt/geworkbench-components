package org.geworkbench.components.promoter.modulediscovery;

import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSSeqPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSSeqRegistration;

import java.util.Comparator;

/**
 * Note: this comparator imposes orderings that are inconsistent with equals."
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class PatternMatchComparator implements Comparator {


    /**
     * equals
     *
     * @param obj Object
     * @return boolean
     */
    public boolean equals(Object obj) {
        return false;
    }

    /**
     * compare
     *
     * @param o1 Object
     * @param o2 Object
     * @return int
     */
    public int compare(Object o1, Object o2) {
        DSSeqPatternMatch m1 = (DSSeqPatternMatch) o1;
        DSSeqPatternMatch m2 = (DSSeqPatternMatch) o2;
        DSSeqRegistration reg1 = m1.getRegistration();
        DSSeqRegistration reg2 = m2.getRegistration();
        if (reg1.x1 > reg2.x1)
            return 1;
        else if (reg1.x1 < reg2.x1) return -1;
        return 0;
    }
}
