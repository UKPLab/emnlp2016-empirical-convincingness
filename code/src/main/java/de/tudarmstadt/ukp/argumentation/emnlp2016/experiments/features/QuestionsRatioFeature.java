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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ivan Habernal
 */
public class QuestionsRatioFeature
        extends AbstractArgumentPairFeature
{
    public static final String FN_QUESTION_RATIO = "QuestionRatio";

    @Override protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
            throws AnalysisEngineProcessException
    {
        int nrOfSentences = JCasUtil.selectCovered(Sentence.class, paragraph).size();
        String text = paragraph.getCoveredText();

        Pattern p = Pattern.compile("\\?[^\\?]"); // don't count multiple question marks as multiple
        // questions

        int matches = 0;
        Matcher m = p.matcher(text);
        while (m.find()) {
            matches++;
        }

        double questionRatio = 0.0;
        if (nrOfSentences > 0) {
            questionRatio = (double) matches / nrOfSentences;
        }

        return new Feature(prefix + FN_QUESTION_RATIO, questionRatio).asList();

    }
}
