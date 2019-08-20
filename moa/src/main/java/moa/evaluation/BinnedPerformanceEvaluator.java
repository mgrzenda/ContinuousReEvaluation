/*
 *    BinnedPerformanceEvaluator.java
 *    Copyright (C) 2019 Warsaw University of Technology, Warszawa, Poland
 *    @author Maciej Grzenda (M.Grzenda@mini.pw.edu.pl)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *    
 */
package moa.evaluation;

import java.util.ArrayList;

import com.yahoo.labs.samoa.instances.Instance;
import moa.core.Example;


/**
 * Interface implemented by learner evaluators capable of supporting verification latency
 * including continuous re-evaluation 
 *
 * @author Maciej Grzenda (M.Grzenda@mini.pw.edu.pl)
 */
public interface BinnedPerformanceEvaluator<E extends Example> extends LearningPerformanceEvaluator<E>  {

    public void addMultipleResultsForInstance(Example<Instance> instance, ArrayList <PredictionItem> predictionsToMerge);
	public void incrementTotalNumberOfRepredictionsForFinishedInstances(int valueToAdd);
	public void setPredictionsInBufferCount(double instancesInBufferCount);
	public void setInstancesInBufferCount(double instancesInBufferCount);
    public void addClonedResult(Example<Instance> example, double[] classVotes);

}
