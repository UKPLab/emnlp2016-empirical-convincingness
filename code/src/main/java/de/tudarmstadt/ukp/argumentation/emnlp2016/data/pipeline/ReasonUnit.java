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

import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.AnnotatedArgumentPair;
import org.apache.commons.math3.analysis.function.Sigmoid;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkReasonUnitAssignment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.tudarmstadt.ukp.argumentation.emnlp2016.data.pipeline.ReasonUnit.Target.a1;
import static de.tudarmstadt.ukp.argumentation.emnlp2016.data.pipeline.ReasonUnit.Target.a2;

/**
 * Container for a single Reason Unit (a property describing an argument)
 *
 * @author Ivan Habernal
 */
public class ReasonUnit
        implements Serializable
{
    private Target target;

    private String reasonUnitText;

    private String id;

    /**
     * Average score of all workers who wrote this reason unit (the same text)
     */
    protected Double averageCompetenceOfOriginalWorkers;

    /**
     * Text that is displayed to Turkers; A1/2 replaced by Argument X etc.
     */
    protected String textForAnnotation;

    /**
     * Turker assignments
     */
    protected List<MTurkReasonUnitAssignment> assignments = new ArrayList<>();

    /**
     * Gold label
     */
    protected String estimatedGoldLabel;

    /**
     * True if this should be ignored for annotations (too short etc)
     */
    protected boolean ignored = false;

    /**
     * True if this text has been seen already somewhere else (one occurrence of this unit
     * has to have duplicate = false)
     */
    protected boolean duplicate = false;
    private boolean filtered;

    public void setAverageCompetenceOfOriginalWorkers(Double averageCompetenceOfOriginalWorkers)
    {
        this.averageCompetenceOfOriginalWorkers = averageCompetenceOfOriginalWorkers;
    }

    public String getTextForAnnotation()
    {
        return textForAnnotation;
    }

    public void setTextForAnnotation(String textForAnnotation)
    {
        this.textForAnnotation = textForAnnotation;
    }

    public List<MTurkReasonUnitAssignment> getAssignments()
    {
        // might not have been initialized by XStream?
        if (assignments == null) {
            assignments = new ArrayList<>();
        }

        return assignments;
    }

    public void setAssignments(List<MTurkReasonUnitAssignment> assignments)
    {
        this.assignments = assignments;
    }

    public String getEstimatedGoldLabel()
    {
        return estimatedGoldLabel;
    }

    public void setEstimatedGoldLabel(String estimatedGoldLabel)
    {
        this.estimatedGoldLabel = estimatedGoldLabel;
    }

    public boolean isIgnored()
    {
        return ignored;
    }

    public void setIgnored(boolean ignored)
    {
        this.ignored = ignored;
    }

    public boolean isDuplicate()
    {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate)
    {
        this.duplicate = duplicate;
    }

    public Target getTarget()
    {
        return target;
    }

    /**
     * "Anchoring" the target from "a1"/"a2" to argument id
     *
     * @param parentArgumentPair parent argument pair
     * @return id
     */
    public String getTargetArgumentId(AnnotatedArgumentPair parentArgumentPair)
    {
        if (a1.equals(target)) {
            return parentArgumentPair.getArg1().getId();
        }

        if (a2.equals(target)) {
            return parentArgumentPair.getArg2().getId();
        }

        throw new IllegalStateException("Unknown target: " + target);
    }

    public void setTarget(Target target)
    {
        if (target == null) {
            throw new IllegalArgumentException("target cannot be null");
        }
        this.target = target;
    }

    public String getReasonUnitText()
    {
        return reasonUnitText;
    }

    public void setReasonUnitText(String reasonUnitText)
    {
        if (reasonUnitText.isEmpty()) {
            throw new IllegalArgumentException("reasonUnitText cannot be empty");
        }

        this.reasonUnitText = reasonUnitText;
    }

    /**
     * Appends text to the right
     *
     * @param reasonUnitText text to be appended
     */
    public void appendText(String reasonUnitText)
    {
        if (this.reasonUnitText == null) {
            this.reasonUnitText = reasonUnitText;
        }
        else {
            this.reasonUnitText = this.reasonUnitText + " " + reasonUnitText;
        }
    }

    @Override public String toString()
    {
        return "ReasonUnit{" +
                "textForAnnotation='" + textForAnnotation + '\'' +
                ", target=" + target +
                ", estimatedGoldLabel='" + estimatedGoldLabel + '\'' +
                ", averageCompetenceOfOriginalWorkers=" + averageCompetenceOfOriginalWorkers +
                '}';
    }

    private final static List<String> CONNECTIVES = Arrays
            .asList("and", "but", "while", "because", "whereas", "whilst", "although");

    public void removeOpeningConnective()
    {
        for (String connective : CONNECTIVES) {
            if (this.reasonUnitText.startsWith(connective)) {
                this.reasonUnitText = this.reasonUnitText.replaceAll("^" + connective, "").trim();
                return;
            }
        }
    }

    public void normalizeWhiteSpaces()
    {
        this.reasonUnitText = this.reasonUnitText.replaceAll("\\s+", " ");
    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setFiltered(boolean filtered)
    {
        this.filtered = filtered;
    }

    public boolean isFiltered()
    {
        return filtered;
    }

    public double getAverageWorkerCompetence()
    {
        double result = 0.0;

        for (MTurkReasonUnitAssignment assignment : assignments) {
            if (assignment.getTurkCompetence() == null) {
                throw new IllegalStateException("No worker competence found");
            }

            result += assignment.getTurkCompetence();
        }

        return result / (double) assignments.size();
    }

    public Double getAverageCompetenceOfOriginalWorkers()
    {
        return averageCompetenceOfOriginalWorkers;
    }

    public enum Target
    {
        a1, a2
    }

    private static final Sigmoid SIGMOID = new Sigmoid();

    /**
     * Computes the weight of the reason unit given the workers' scores from MACE. Labels
     * different from the gold predicted label are penalized by the {@code lambda} parameter.
     * Output is squeezed by sigmoid function to fit into (0-1)
     *
     * @param lambda     lambda for negative
     * @return weight (0.0 - 1.0)
     */
    public double computeAnnotatedWeight(double lambda)
    {
        String goldLabel = getEstimatedGoldLabel();

        if (goldLabel == null) {
            throw new IllegalArgumentException(
                    "Cannot compute weight on argument pair with null gold label");
        }

        if (this.assignments.isEmpty()) {
            throw new IllegalStateException("No assignments to this reason unit");
        }

        double sumCompetenceGold = 0.0;
        double sumCompetenceOpposite = 0.0;

        for (MTurkReasonUnitAssignment assignment : getAssignments()) {
            // label
            String label = assignment.getValue();
            double competence = assignment.getTurkCompetence();

            if (label.equals(goldLabel)) {
                sumCompetenceGold += competence;
            }
            else {
                sumCompetenceOpposite += competence;
            }
        }

        double sum = sumCompetenceGold - (lambda * sumCompetenceOpposite);

        // and squeeze using sigmoid
        return SIGMOID.value(sum);
    }

}
