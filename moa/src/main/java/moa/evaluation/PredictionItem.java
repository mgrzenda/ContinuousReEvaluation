/*
 *    PredictionItem.java
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

/**
 * Class that contains one prediction made for an instance, including whether it is
 * - first time prediction i.e. prediction made when unlabelled instance arrived
 * - test-then-train prediction i.e. prediction made immediately before true label for the instance was used to update a model
 * - reprediction i.e. any prediction made between the two predictions described above
 * i.e. while waiting for true label
 * @author Maciej Grzenda (M.Grzenda@mini.pw.edu.pl)
 */
public class PredictionItem {

	public enum PredictionType {
	    FIRST_PREDICTION, REPREDICTION,FINAL_PREDICTION
	};
	
	PredictionType predictionType;
	private double[] classVotes;
	
	private int binIndex=-1;
	public int getBinIndex() {
		return binIndex;
	}
	public void setBinIndex(int binIndex) {
		this.binIndex = binIndex;
	}
	public double[] getClassVotes() {
		return classVotes;
	}
	public long getPredictionTimeStamp() {
		return predictionTimeStamp;
	}
	public PredictionItem(double[] classVotes, long predictionTimeStamp, PredictionType predictionType) {
		super();
		this.classVotes = classVotes;
		this.predictionTimeStamp = predictionTimeStamp;
		this.predictionType=predictionType;
	}
	private long predictionTimeStamp;
	public PredictionType getPredictionType() {
		return predictionType;
	}
	

}
