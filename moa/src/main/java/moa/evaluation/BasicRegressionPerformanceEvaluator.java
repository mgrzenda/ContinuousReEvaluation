/*
 *    BasicRegressionPerformanceEvaluator.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *    @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
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

import moa.AbstractMOAObject;
import moa.core.Example;
import moa.core.Measurement;
import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.tasks.TaskMonitor;

import java.util.ArrayList;

import com.github.javacliparser.FlagOption;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstanceData;
import com.yahoo.labs.samoa.instances.Prediction;

/**
 * Regression evaluator that performs basic incremental evaluation.
 *
 * @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
 * @version $Revision: 7 $
 */
public class BasicRegressionPerformanceEvaluator extends AbstractOptionHandler
        implements RegressionPerformanceEvaluator,
         BinnedPerformanceEvaluator<Example<Instance>> {

    private static final long serialVersionUID = 1L;

    protected double weightObserved;

    protected double squareError;

    protected double averageError;

    protected double sumTarget;
    
    protected double squareTargetError;
    
    protected double averageTargetError;
    
    private String evaluatorInstance="";
    
    // total number of true predictions made within this bin i.e. excluding possible cloned predictions
 	// the number is incremented since the beginning of evaluation
 	public double totalPredictionCount;
 	
 	private double predictionsInBufferCount;
     
 	private double instancesInBufferCount;
 	
 	private double totalRepredictionCountForFinishedInstances;
 	
 	public FlagOption reportExtendedEvaluationOption = new FlagOption("reportExtendedEvaluationOverhead", 'e',
            "Report the overhead of extended evaluation.");
    
    public FlagOption aggregateMultiplePredictionsPerBinOption = new FlagOption("aggregateMultiplePredictionsPerBin", 'a',
            "Aggregate multiple predictions in one bin into a single prediction.");

    
    @Override
    protected void prepareForUseImpl(TaskMonitor monitor, ObjectRepository repository) {

    }
    
    public String getEvaluatorInstance() {
		return evaluatorInstance;
	}

	public void setEvaluatorInstance(String evaluatorInstance) {
		this.evaluatorInstance = evaluatorInstance;
	}

    @Override
    public void reset() {
        this.weightObserved = 0.0;
        this.squareError = 0.0;
        this.averageError = 0.0;
        this.sumTarget = 0.0;
        this.averageTargetError = 0.0;
        this.squareTargetError = 0.0;
        
    }

    @Override
    public void addResult(Example<Instance> example, double[] prediction) {
	Instance inst = example.getData();
	 	if (inst.classIsMissing())
	 	{
	        if (inst.weight() > 0.0) {
	           this.totalPredictionCount=this.totalPredictionCount+1;
	        }
	        return;
	 	}
        if (inst.weight() > 0.0) {
            if (prediction.length > 0) {
            	this.totalPredictionCount=this.totalPredictionCount+1;
                double meanTarget = this.weightObserved != 0 ? 
                            this.sumTarget / this.weightObserved : 0.0;
                this.squareError += (inst.classValue() - prediction[0]) * (inst.classValue() - prediction[0]);
                this.averageError += Math.abs(inst.classValue() - prediction[0]);
                this.squareTargetError += (inst.classValue() - meanTarget) * (inst.classValue() - meanTarget);
                this.averageTargetError += Math.abs(inst.classValue() - meanTarget);
                this.sumTarget += inst.classValue();
                this.weightObserved += inst.weight();
            }
        }
    }

    @Override
    public Measurement[] getPerformanceMeasurements() {
    	ArrayList<Measurement> measurements = new ArrayList<Measurement>();
    	measurements.add(new Measurement(evaluatorInstance+"classified instances",getTotalWeightObserved()));
    	measurements.add(new Measurement(evaluatorInstance+"mean absolute error",getMeanError()));
    	measurements.add(new Measurement(evaluatorInstance+"root mean squared error",getSquareError()));
    	measurements.add(new Measurement(evaluatorInstance+"relative mean absolute error",getRelativeMeanError()));
    	measurements.add(new Measurement(evaluatorInstance+"relative root mean squared error",getRelativeSquareError()));
    	if (reportExtendedEvaluationOption.isSet())
           {
           	measurements.add(new Measurement(evaluatorInstance+"Calculated prediction count",
           			this.getTotalPredictionCount()));
           	measurements.add(new Measurement(evaluatorInstance+"Instances in buffer count",
           			this.getInstancesInBufferCount()));
           	measurements.add(new Measurement(evaluatorInstance+"Predictions in buffer count",
           			this.getPredictionsInBufferCount()));
           	measurements.add(new Measurement(evaluatorInstance+"Average reprediction count",
           			this.getAverageRepredictionPerInstanceCount()));
           	
           }
    	Measurement[] result = new Measurement[measurements.size()];

        return measurements.toArray(result);
    }

    public double getTotalWeightObserved() {
        return this.weightObserved;
    }

    public double getMeanError() {
        return this.weightObserved > 0.0 ? this.averageError
                / this.weightObserved : 0.0;
    }

    public double getSquareError() {
        return Math.sqrt(this.weightObserved > 0.0 ? this.squareError
                / this.weightObserved : 0.0);
    }

    public double getTargetMeanError() {
        return this.weightObserved > 0.0 ? this.averageTargetError
                / this.weightObserved : 0.0;
    }

    public double getTargetSquareError() {
        return Math.sqrt(this.weightObserved > 0.0 ? this.squareTargetError
                / this.weightObserved : 0.0);
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        Measurement.getMeasurementsDescription(getPerformanceMeasurements(),
                sb, indent);
    }

    private double getRelativeMeanError() {
        //double targetMeanError = getTargetMeanError();
        //return targetMeanError > 0 ? getMeanError()/targetMeanError : 0.0;
        return this.averageTargetError> 0 ?
                this.averageError/this.averageTargetError : 0.0;
}

    private double getRelativeSquareError() {
        //double targetSquareError = getTargetSquareError();
        //return targetSquareError > 0 ? getSquareError()/targetSquareError : 0.0;
    return Math.sqrt(this.squareTargetError> 0 ?
                this.squareError/this.squareTargetError : 0.0);
    }
    
    public double getTotalPredictionCount() {
		return totalPredictionCount;
	}
    
    public double getPredictionsInBufferCount() {
		return predictionsInBufferCount;
	}

	public void setPredictionsInBufferCount(double instancesInBufferCount) {
		this.predictionsInBufferCount = instancesInBufferCount;
	}
	
	public double getInstancesInBufferCount() {
		return instancesInBufferCount;
	}

	public void setInstancesInBufferCount(double instancesInBufferCount) {
		this.instancesInBufferCount = instancesInBufferCount;
	}
    
    @Override
    public void addResult(Example<Instance> example, Prediction prediction) {
    	if(prediction!=null)
    		addResult(example,prediction.getVotes(0));
    }

	@Override
	public void addMultipleResultsForInstance(Example<Instance> instance,
			ArrayList<PredictionItem> predictionsToMerge) {
		 double weightedVotes[] = this.getAggregateDecision(predictionsToMerge);
		
		 // for this bin, produce just one aggregated prediction
		 this.addResult(instance, weightedVotes);
		
	}
	
	private double[] getAggregateDecision(ArrayList<PredictionItem> predictionsForPeriod)
	{
		double[] aggregatedVotes=new double[1];
		double weightedAnswer=0;

		for (int currentPredictionIndex=1;currentPredictionIndex<predictionsForPeriod.size();currentPredictionIndex++)
		{
			// previous prediction (possibly from previous bin) remains valid until the next one 
			double vote=predictionsForPeriod.get(currentPredictionIndex - 1).getClassVotes()[0];
			
			weightedAnswer+=vote*
					(predictionsForPeriod.get(currentPredictionIndex).getPredictionTimeStamp()-
							predictionsForPeriod.get(currentPredictionIndex-1).getPredictionTimeStamp());	
			}
		

		aggregatedVotes[0]=weightedAnswer/(predictionsForPeriod.get(predictionsForPeriod.size()-1).getPredictionTimeStamp()-
				predictionsForPeriod.get(0).getPredictionTimeStamp());
		return aggregatedVotes;
	}

	public void incrementTotalNumberOfRepredictionsForFinishedInstances(int valueToAdd)
	{
		this.totalRepredictionCountForFinishedInstances+=valueToAdd;
	}
	
	public double getAverageRepredictionPerInstanceCount()
	{
		// this functionality is available for classification only
		return 0;
	}
	
	@Override
	public void addClonedResult(Example<Instance> example, double[] classVotes) {
		this.addResult(example,classVotes);
    	// do not count this prediction as it did not require extra call to predictive model
    	this.totalPredictionCount=this.totalPredictionCount-1;  	
	}
	
	
}
