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

import java.util.Comparator;
import java.util.Date;

/**
 * @author Ivan Habernal
 */
public class MTurkReasonUnitAssignment
{
    protected final String turkID;
    protected final String hitID;
    protected final String assignmentId;
    protected final Date assignmentAcceptTime;
    protected final Date assignmentSubmitTime;
    protected final String value;
    protected String hitComment;
    protected Double turkCompetence;

    public MTurkReasonUnitAssignment(String turkID, String hitID, String assignmentId,
            Date assignmentAcceptTime, Date assignmentSubmitTime, String value)
    {
        if (turkID == null) {
            throw new IllegalArgumentException("turkID is null");
        }

        if (hitID == null) {
            throw new IllegalArgumentException("hitID is null");
        }

        if (assignmentId == null) {
            throw new IllegalArgumentException("assignmentID is null");
        }

        if (assignmentAcceptTime == null) {
            throw new IllegalArgumentException("assignmentAcceptTime is null");
        }

        if (assignmentSubmitTime == null) {
            throw new IllegalArgumentException("assignmentSubmitTime is null");
        }

        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        this.turkID = turkID;
        this.hitID = hitID;
        this.assignmentId = assignmentId;
        this.assignmentAcceptTime = assignmentAcceptTime;
        this.assignmentSubmitTime = assignmentSubmitTime;
        this.value = value;
    }

    public String getTurkID()
    {
        return turkID;
    }

    public String getHitID()
    {
        return hitID;
    }

    public String getAssignmentId()
    {
        return assignmentId;
    }

    public Date getAssignmentAcceptTime()
    {
        return assignmentAcceptTime;
    }

    public Date getAssignmentSubmitTime()
    {
        return assignmentSubmitTime;
    }

    public String getValue()
    {
        return value;
    }

    public String getHitComment()
    {
        return hitComment;
    }

    public void setHitComment(String hitComment)
    {
        this.hitComment = hitComment;
    }

    public Double getTurkCompetence()
    {
        return turkCompetence;
    }

    public void setTurkCompetence(Double turkCompetence)
    {
        this.turkCompetence = turkCompetence;
    }

    public static class SubmissionTimeComparator
            implements Comparator<MTurkReasonUnitAssignment>
    {
        @Override
        public int compare(MTurkReasonUnitAssignment o1, MTurkReasonUnitAssignment o2)
        {
            return o1.getAssignmentSubmitTime().compareTo(o2.getAssignmentSubmitTime());
        }
    }

    @Override
    public String toString()
    {
        return "MTurkReasonUnitAssignment{" +
                "turkID='" + turkID + '\'' +
                ", hitID='" + hitID + '\'' +
                ", assignmentId='" + assignmentId + '\'' +
                ", assignmentAcceptTime=" + assignmentAcceptTime +
                ", assignmentSubmitTime=" + assignmentSubmitTime +
                ", value='" + value + '\'' +
                ", hitComment='" + hitComment + '\'' +
                ", turkCompetence=" + turkCompetence +
                '}';
    }

    /**
     * Seconds between accepting HIT and submitting HIT
     *
     * @return seconds
     */
    public int getSubmitTimeInSeconds()
    {
        return (int) (this.getAssignmentSubmitTime().getTime() - this.getAssignmentAcceptTime()
                .getTime()) / 1000;
    }
}
