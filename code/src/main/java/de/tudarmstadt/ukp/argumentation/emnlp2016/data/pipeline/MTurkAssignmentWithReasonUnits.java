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

import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkAssignment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class MTurkAssignmentWithReasonUnits
        extends MTurkAssignment
{
    public MTurkAssignmentWithReasonUnits()
    {
    }

    public MTurkAssignmentWithReasonUnits(MTurkAssignment other)
    {
        super(other);
    }

    protected List<ReasonUnit> reasonUnits = new ArrayList<>();

    public List<ReasonUnit> getReasonUnits()
    {
        return reasonUnits;
    }

    public void setReasonUnits(List<ReasonUnit> reasonUnits)
    {
        this.reasonUnits = reasonUnits;
    }
}
