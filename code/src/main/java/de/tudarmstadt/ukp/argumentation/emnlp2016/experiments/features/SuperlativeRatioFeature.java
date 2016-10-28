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

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class SuperlativeRatioFeature
        extends AbstractArgumentPairFeature
{
    public static final String FN_SUPERLATIVE_RATIO_ADJ = "SuperlativeRatioAdj";
    public static final String FN_SUPERLATIVE_RATIO_ADV = "SuperlativeRatioAdv";

    @Override
    protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
            throws AnalysisEngineProcessException
    {
        double adjRatio = 0;
        int superlativeAdj = 0;
        int adjectives = 0;
        for (ADJ tag : JCasUtil.selectCovered(ADJ.class, paragraph)) {
            adjectives++;
            if (tag.getPosValue().contains("JJS")) {
                superlativeAdj++;
            }
        }
        if (adjectives > 0) {
            adjRatio = (double) superlativeAdj / adjectives;
        }

        double advRatio = 0;
        int superlativeAdv = 0;
        int adverbs = 0;
        for (ADV tag : JCasUtil.selectCovered(ADV.class, paragraph)) {
            adverbs++;
            if (tag.getPosValue().contains("RBS")) {
                superlativeAdv++;
            }
        }
        if (adverbs > 0) {
            advRatio = (double) superlativeAdv / adverbs;
        }

        List<Feature> features = new ArrayList<Feature>();
        features.add(new Feature(prefix + FN_SUPERLATIVE_RATIO_ADJ, adjRatio));
        features.add(new Feature(prefix + FN_SUPERLATIVE_RATIO_ADV, advRatio));

        return features;

    }
}
