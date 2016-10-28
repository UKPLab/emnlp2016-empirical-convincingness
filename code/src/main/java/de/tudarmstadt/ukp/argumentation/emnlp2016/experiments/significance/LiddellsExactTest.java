/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tudarmstadt.ukp.argumentation.emnlp2016.experiments.significance;

import org.apache.commons.math.MathException;
import org.apache.commons.math.special.Beta;

/**
 * Java implementation of the Liddel's Exact Test. Based on the following Matlab code.
 * <p/>
 * {@code http://www.mathworks.com/matlabcentral/fileexchange/22024-liddell-s-exact-test}
 * <p/>
 * {@code http://www.jstor.org/stable/2988087}
 * <p/>
 * {@code http://code.metager.de/source/xref/gnu/octave/scripts/statistics/distributions/fcdf.m}
 * <p/>
 * {@code http://octave.sourceforge.net/octave/function/betainc.html}
 * <p/>
 * {@code http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/special/Beta.html#regularizedBeta%28double,%20double,%20double%29}
 *
 * @author Ivan Habernal
 */
public class LiddellsExactTest
{
    public static double pValue(int b, int d)
            throws MathException
    {
        double r = Math.max(b, d);
        double s = Math.min(b, d);

        double f = r / (s + 1);

        double mm = 2 * (s + 1);
        double nn = 2 * r;

        double betaPar1 = 1.0 / (1 + mm * f / nn);
        double betaPar2 = nn / 2;
        double betaPar3 = mm / 2;

        double beta = Beta.regularizedBeta(betaPar1, betaPar2, betaPar3);

        double pValue = 2 * (1 - (1 - beta));

        //        if (pValue < 0.1) {
        //            System.out.println(r);
        //            System.out.println(s);
        //        }

        return pValue;
    }
}
