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

package de.tudarmstadt.ukp.argumentation.emnlp2016.experiments.features;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * Quick, very simplified approximation of usage of past tense
 * in comparison to present/future tense in the text.
 * <p/>
 * Works for Penn Treebank POS tags only.
 * <p/>
 * Captures the ratio of all verbs to
 * "VBD" (verb praeterite) and "VBN" (verb past participle) as past and
 * "VB" (verb base form), "VBP" (verb present) and "VBZ" (verb present 3rd pers sg) as present/future.
 * The output is multiplied by 100 as the values are usually very small.
 */
public class PastVsFutureFeature
        extends AbstractArgumentPairFeature
{
    public static final String FN_PAST_RATIO = "PastVerbRatio";
    public static final String FN_FUTURE_RATIO = "FutureVerbRatio";
    public static final String FN_FUTURE_VS_PAST_RATIO = "FutureVsPastVerbRatio";

    @Override
    protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
            throws AnalysisEngineProcessException
    {
        double pastRatio = 0.0;
        double futureRatio = 0.0;
        double futureToPastRatio = 0.0;
        int pastVerbs = 0;
        int futureVerbs = 0;
        int verbs = 0;

        for (V tag : JCasUtil.selectCovered(V.class, paragraph)) {
            verbs++;
            if (tag.getPosValue().contains("VBD") || tag.getPosValue().contains("VBN")) {
                pastVerbs++;
            }
            if (tag.getPosValue().contains("VB") || tag.getPosValue().contains("VBP")
                    || tag.getPosValue().contains("VBZ")) {
                futureVerbs++;
            }
        }
        if (verbs > 0) {
            pastRatio = (double) pastVerbs * 100 / verbs;
            futureRatio = (double) futureVerbs * 100 / verbs;
        }
        if ((pastRatio > 0) && (futureRatio > 0)) {
            futureToPastRatio = futureRatio / pastRatio;
        }

        List<Feature> features = new ArrayList<Feature>();
        features.add(new Feature(prefix + FN_PAST_RATIO, pastRatio));
        features.add(new Feature(prefix + FN_FUTURE_RATIO, futureRatio));
        features.add(new Feature(prefix + FN_FUTURE_VS_PAST_RATIO, futureToPastRatio));

        return features;
    }
}

