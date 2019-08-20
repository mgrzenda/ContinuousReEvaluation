/*
 *    InstanceInProgress.java
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
 * Class that contains one instance awaiting its true label in delayed labelling setting
 * The instance can contain possibly many predictions produced by an evolving model during the period
 * preceding true label arrival
 * @author Maciej Grzenda (M.Grzenda@mini.pw.edu.pl)
 */
public class InstanceInProgress {

	private ArrayList<PredictionItem> predictions = new ArrayList<PredictionItem>();

	public ArrayList<PredictionItem> getPredictions() {
		return predictions;
	}

	public Instance getInstance() {
		return example.getData();
	}

	public long getInstancesPassed() {
		return instancesPassed;
	}

	private Example<Instance> example;

	public Example<Instance> getExample() {
		return example;
	}

	// the number of labelled instances that passed (and were used to update the model) since making first prediction for this instance
	// this is used to trigger re-predictions
	private long instancesPassed;

	public InstanceInProgress(Example<Instance> example) {
		this.example = example;
		this.instancesPassed = 0;
	}

	public void incrementInstancesPassed() {
		instancesPassed++;
	}

	// get the index of one of intermediate bins corresponding to the time of the prediction
	public int getPeriodicBinIndexForTime(long firstTimeStamp,long lastTimeStamp,PredictionItem predictionItem,int binCount)
	{
		if (predictionItem.getPredictionType()==PredictionItem.PredictionType.FIRST_PREDICTION)
			return 0;
		if (predictionItem.getPredictionType()==PredictionItem.PredictionType.FINAL_PREDICTION)
			return binCount+1;
		long predictionTimeStamp=predictionItem.getPredictionTimeStamp();
		return getPeriodicBinIndexForTime(firstTimeStamp,lastTimeStamp,predictionTimeStamp,binCount);
	}

	public int getPeriodicBinIndexForTime(long firstTimeStamp,long lastTimeStamp,long predictionTimeStamp,int binCount)
	{
		int bin=1 + (int) Math.floor(((1.0 * predictionTimeStamp - firstTimeStamp)
				/ (lastTimeStamp - firstTimeStamp)) * binCount);

		// even if the timestamp for reprediction is the same as for the final prediction (i.e. true label arrival) put this
		// reprediction into the last but one bin i.e. not the one reserved for final predictions
		if (bin == binCount + 1) {
			bin=binCount;
		}
		return bin;
	}

	public void mapPredictionsToBins(BinnedPerformanceEvaluator<Example<Instance>>[] binEvaluators) {
		
		int predictionCount = predictions.size();
		int binCount = binEvaluators.length - 2;
		int lastBinProcessed = 0;

		long firstTimeStamp = predictions.get(0).getPredictionTimeStamp();
		long lastTimeStamp = predictions.get(predictionCount - 1).getPredictionTimeStamp();

		int previousBinIndex=-1,currentBinIndex=-1;

		for (int i = 0; i < predictionCount; i++) {
			switch (predictions.get(i).predictionType) {
			// first time prediction
			case FIRST_PREDICTION:
				predictions.get(i).setBinIndex(0);
				binEvaluators[0].addResult(this.getExample(), predictions.get(i).getClassVotes());
				break;

				// prediction made in test-then-train mode i.e. right before
				// receivng true label
			case FINAL_PREDICTION:
				predictions.get(i).setBinIndex(binCount + 1);
				binEvaluators[binCount + 1].addResult(this.getExample(), predictions.get(i).getClassVotes());

				// add potentially missing predictions for preceding bins
				for (int bin = lastBinProcessed + 1; bin <= binCount; bin++) {
					binEvaluators[bin].addClonedResult(this.getExample(), predictions.get(i - 1).getClassVotes());
				}

				break;

				// additional predictions made to consider evolving model
			case REPREDICTION:

				boolean aggregatePredictionsMerged=false;
				currentBinIndex=getPeriodicBinIndexForTime(firstTimeStamp,lastTimeStamp,predictions.get(i),binCount);
				predictions.get(i).setBinIndex(currentBinIndex);

				for (int bin = lastBinProcessed + 1; bin < predictions.get(i).getBinIndex(); bin++) {
					binEvaluators[bin].addClonedResult(this.getExample(), predictions.get(i - 1).getClassVotes());
				}

				lastBinProcessed = predictions.get(i).getBinIndex();

				
				if ((i<predictionCount-1) && (predictions.get(i+1).predictionType==PredictionItem.PredictionType.REPREDICTION))
				{
					// next prediction comes from the same bin, so we need to aggregate two or more of them
					if (currentBinIndex==getPeriodicBinIndexForTime(firstTimeStamp,lastTimeStamp,predictions.get(i+1),binCount))
					{
						long binStartTimeStamp=(long) Math.floor(((currentBinIndex-1)/((double) binCount))*(lastTimeStamp-firstTimeStamp)+firstTimeStamp);
						long binEndTimeStamp=(long)  Math.floor(((currentBinIndex)/((double) binCount))*(lastTimeStamp-firstTimeStamp)+firstTimeStamp);
						int currentPredictionIndex=i;
						int nextBinIndex;

						ArrayList <PredictionItem> predictionsToMerge = new ArrayList<PredictionItem>();

						// insert prediction from previous bin as the first one
						// to be used at the beginning of the period
						predictionsToMerge.add(new PredictionItem(
								predictions.get(i-1).getClassVotes(),
								binStartTimeStamp,
								PredictionItem.PredictionType.REPREDICTION));

						currentPredictionIndex=i;
						do
						{
							predictionsToMerge.add(new PredictionItem(
									predictions.get(currentPredictionIndex).getClassVotes(),
									predictions.get(currentPredictionIndex).getPredictionTimeStamp(),
									PredictionItem.PredictionType.REPREDICTION));
							currentPredictionIndex++;
							nextBinIndex=getPeriodicBinIndexForTime(firstTimeStamp,lastTimeStamp,predictions.get(currentPredictionIndex),binCount);

						}while(currentBinIndex==nextBinIndex);
						predictionsToMerge.add(new PredictionItem(
								predictions.get(currentPredictionIndex-1).getClassVotes(),
								binEndTimeStamp,
								PredictionItem.PredictionType.REPREDICTION));
						// now, let us use evaluator to (if requested to do so) aggregate possibly many predictions made for one instance
						binEvaluators[lastBinProcessed].addMultipleResultsForInstance(this.getExample(), predictionsToMerge);
						i=currentPredictionIndex-1;
						aggregatePredictionsMerged=true;
					}
				}
				// this happens when we found just one prediction for this bean 
				if (!aggregatePredictionsMerged)
				{			
					binEvaluators[lastBinProcessed].addResult(this.getExample(), predictions.get(i).getClassVotes());
				}
	
			}
		}

	}

}
