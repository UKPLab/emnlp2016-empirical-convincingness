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

package de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling;

import de.tudarmstadt.ukp.argumentation.emnlp2016.data.createdebate.Argument;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class AnnotatedArgumentPair
        extends ArgumentPair
{

    protected List<MTurkAssignment> mTurkAssignments = new ArrayList<>();
    private String goldLabel;

    public List<MTurkAssignment> getMTurkAssignments()
    {
        return mTurkAssignments;
    }

    public void setMTurkAssignments(List<MTurkAssignment> mTurkAssignments)
    {
        this.mTurkAssignments = mTurkAssignments;
    }

    /**
     * Creates a shallow copy of the parameter
     *
     * @param argumentPair parameter
     */
    public AnnotatedArgumentPair(ArgumentPair argumentPair)
    {
        super(argumentPair);
    }

    /**
     * Empty constructor - use with caution! :)
     */
    public AnnotatedArgumentPair()
    {
    }

    public void setGoldLabel(String goldLabel)
    {
        this.goldLabel = goldLabel;
    }

    public String getGoldLabel()
    {
        return goldLabel;
    }

    public String toStringSimple()
    {
        return arg1.getId() + ":" + arg2.getId() + " (" + goldLabel + ")";
    }

    @Override public String toString()
    {
        return "AnnotatedArgumentPair{" +
                "mTurkAssignments=" + mTurkAssignments +
                ", goldLabel='" + goldLabel + '\'' +
                '}';
    }

    /**
     * Returns gold-data more convincing argument
     *
     * @return argument
     */
    public Argument getMoreConvincingArgument()
    {
        if ("a1".equals(this.getGoldLabel())) {
            return this.getArg1();
        }

        if ("a2".equals(this.getGoldLabel())) {
            return this.getArg2();
        }

        throw new IllegalStateException("Gold label is neither a1 or a2");
    }

    /**
     * Returns gold-data less convincing argument
     *
     * @return argument
     */
    public Argument getLessConvincingArgument()
    {
        if ("a1".equals(this.getGoldLabel())) {
            return this.getArg2();
        }

        if ("a2".equals(this.getGoldLabel())) {
            return this.getArg1();
        }

        throw new IllegalStateException("Gold label is neither a1 or a2");
    }
}
