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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ivan Habernal
 */
public class NGramPresenceFeature
        extends AbstractArgumentPairFeature
{
    @Override
    protected List<Feature> extractFeaturesFromArgument(JCas jCas, Paragraph paragraph,
            String prefix)
    {
        List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, paragraph);

        Set<String> nGramsBinary = new HashSet<String>();

        for (int i = 0; i < tokens.size() - 1; i++) {
            Token token = tokens.get(i);
            Token tokenPlus1 = tokens.get(i + 1);

            String uniGram = getLemma(token);
            String biGram = getLemma(token) + "_" + getLemma(tokenPlus1);

            nGramsBinary.add(uniGram);
            nGramsBinary.add(biGram);
        }

        List<Feature> result = new ArrayList<Feature>();

        for (String nGram : nGramsBinary) {
            result.add(new Feature(prefix + nGram, 1));
        }

        return result;
    }

    /**
     * Extracts lemma of falls back to token text
     * @param token token
     * @return lemma or token
     */
    public static String getLemma(Token token)
    {
        Lemma lemma = token.getLemma();

        if (lemma != null) {
            return lemma.getValue();
        }

        System.err.println("No lemma for " + token.getCoveredText());

        return token.getCoveredText();
    }
}
