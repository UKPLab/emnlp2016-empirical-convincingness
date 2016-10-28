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

import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkReasonUnitAssignment;

import java.util.List;

/**
 * Gold label provider for ReasonUnit annotations
 * @author Ivan Habernal
 */
public interface GoldLabelProvider
{

    /**
     * Given a reason unit ID, returns gold label (if known) of null
     *
     * @param reasonUnitId ID of reason unit
     * @return gold label or null
     */
    String provideGoldLabel(int reasonUnitId);

    /**
     * Returns all MTurk assignments for this reason unit
     *
     * @param reasonUnitId reason unit ID
     * @return list of assignments (may be empty but never null)
     */
    List<MTurkReasonUnitAssignment> getMTurkReasonUnitAssignments(int reasonUnitId);
}
