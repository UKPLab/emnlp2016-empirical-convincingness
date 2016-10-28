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

package de.tudarmstadt.ukp.argumentation.emnlp2016.data.pipeline;

import java.util.*;

/**
 *
 */
public class GoldLabelPairContainer
{
    public String id;
    public String moreConvincingArgumentText;
    public String lessConvincingArgumentText;
    public SortedSet<String> moreConvincingLabels = new TreeSet<>();
    public SortedSet<String> lessConvincingLabels = new TreeSet<>();
    public String debateTopic;
    public String debateStance;
    public String moreConvincingArgumentId;
    public String lessConvincingArgumentId;
    public Integer isFaked = null;
    public String moreConvincingLabelsString;
    public String lessConvincingLabelsString;

    public GoldLabelPairContainer()
    {
        // empty
    }

    public GoldLabelPairContainer(GoldLabelPairContainer other)
    {
        this.id = other.id;
        this.moreConvincingLabels = other.moreConvincingLabels;
        this.lessConvincingLabels = other.lessConvincingLabels;
        this.moreConvincingArgumentText = other.moreConvincingArgumentText;
        this.lessConvincingArgumentText = other.lessConvincingArgumentText;
        this.debateStance = other.debateStance;
        this.debateTopic = other.debateTopic;
        this.moreConvincingArgumentId = other.moreConvincingArgumentId;
        this.lessConvincingArgumentId = other.lessConvincingArgumentId;
        this.isFaked = other.isFaked;
    }

    @Override public String toString()
    {
        return "GoldLabelPairContainer{" +
                "id='" + id + '\'' +
                ", moreConvincingArgumentText='" + moreConvincingArgumentText + '\'' +
                ", lessConvincingArgumentText='" + lessConvincingArgumentText + '\'' +
                ", moreConvincingLabels=" + moreConvincingLabels +
                ", lessConvincingLabels=" + lessConvincingLabels +
                ", debateTopic='" + debateTopic + '\'' +
                ", debateStance='" + debateStance + '\'' +
                ", moreConvincingArgumentId='" + moreConvincingArgumentId + '\'' +
                ", lessConvincingArgumentId='" + lessConvincingArgumentId + '\'' +
                '}';
    }
}
