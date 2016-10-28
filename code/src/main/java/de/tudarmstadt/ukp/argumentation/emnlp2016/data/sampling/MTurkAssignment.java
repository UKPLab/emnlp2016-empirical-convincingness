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

import java.util.Date;

/**
 * @author Ivan Habernal
 */
public class MTurkAssignment
{

    protected String turkID;
    protected String hitID;
    protected Date assignmentAcceptTime;
    protected Date assignmentSubmitTime;
    protected String value;
    protected String reason;
    protected String hitComment;
    protected String assignmentId;
    protected Integer turkRank;
    protected Double turkCompetence;
    protected String workerStance;

    public MTurkAssignment()
    {
    }

    public MTurkAssignment(MTurkAssignment other)
    {
        this.turkID = other.turkID;
        this.hitID = other.hitID;
        this.assignmentAcceptTime = other.assignmentAcceptTime;
        this.assignmentSubmitTime = other.assignmentSubmitTime;
        this.value = other.value;
        this.reason = other.reason;
        this.hitComment = other.hitComment;
        this.assignmentId = other.assignmentId;
        this.turkRank = other.turkRank;
        this.turkCompetence = other.turkCompetence;
        this.workerStance = other.workerStance;
    }

    public String getTurkID()
    {
        return turkID;
    }

    public void setTurkID(String turkID)
    {
        if (turkID == null) {
            throw new IllegalArgumentException("Parameter turkID cannot be null");
        }

        this.turkID = turkID;
    }

    public String getHitID()
    {
        return hitID;
    }

    public void setHitID(String hitID)
    {
        if (hitID == null) {
            throw new IllegalArgumentException("Parameter hitID cannot be null");
        }

        this.hitID = hitID;
    }

    public Date getAssignmentAcceptTime()
    {
        return assignmentAcceptTime;
    }

    public void setAssignmentAcceptTime(Date assignmentAcceptTime)
    {
        if (assignmentAcceptTime == null) {
            throw new IllegalArgumentException("Parameter assignmentAcceptTime cannot be null");
        }

        this.assignmentAcceptTime = assignmentAcceptTime;
    }

    public Date getAssignmentSubmitTime()
    {
        return assignmentSubmitTime;
    }

    public void setAssignmentSubmitTime(Date assignmentSubmitTime)
    {
        if (assignmentSubmitTime == null) {
            throw new IllegalArgumentException("Parameter assignmentSubmitTime cannot be null");
        }

        this.assignmentSubmitTime = assignmentSubmitTime;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        if (value == null) {
            throw new IllegalArgumentException("Parameter value cannot be null");
        }

        this.value = value;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        if (reason == null) {
            throw new IllegalArgumentException("Parameter reason cannot be null");
        }

        this.reason = reason;
    }

    public String getHitComment()
    {
        return hitComment;
    }

    public void setHitComment(String hitComment)
    {
        this.hitComment = hitComment;
    }

    public void setAssignmentId(String assignmentId)
    {
        if (assignmentId == null) {
            throw new IllegalArgumentException("Parameter assignmentId cannot be null");
        }
        this.assignmentId = assignmentId;
    }

    public String getAssignmentId()
    {
        return assignmentId;
    }

    public void setTurkRank(Integer turkRank)
    {
        this.turkRank = turkRank;
    }

    public Integer getTurkRank()
    {
        return turkRank;
    }

    public void setTurkCompetence(Double turkCompetence)
    {
        if (turkCompetence == null) {
            throw new IllegalArgumentException("Parameter turkCompetence cannot be null");
        }
        this.turkCompetence = turkCompetence;
    }

    public Double getTurkCompetence()
    {
        return turkCompetence;
    }

    public void setWorkerStance(String workerStance)
    {
        this.workerStance = workerStance;
    }

    public String getWorkerStance()
    {
        return workerStance;
    }

    @Override
    public String toString()
    {
        return "MTurkAssignment{" +
                "turkID='" + turkID + '\'' +
                ", hitID='" + hitID + '\'' +
                ", assignmentAcceptTime=" + assignmentAcceptTime +
                ", assignmentSubmitTime=" + assignmentSubmitTime +
                ", value='" + value + '\'' +
                ", reason='" + reason + '\'' +
                ", hitComment='" + hitComment + '\'' +
                ", assignmentId='" + assignmentId + '\'' +
                ", turkRank=" + turkRank +
                ", turkCompetence=" + turkCompetence +
                ", workerStance='" + workerStance + '\'' +
                '}';
    }
}
