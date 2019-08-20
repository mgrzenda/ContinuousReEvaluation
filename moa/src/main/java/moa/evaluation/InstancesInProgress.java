/*
 *    InstancesInProgress.java
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

/**
 * Class that represents a memory buffer i.e. a list of instances waiting for their true labels
 * in delayed labelling setting
 * @author Maciej Grzenda (M.Grzenda@mini.pw.edu.pl)
 */
public class InstancesInProgress extends ArrayList<InstanceInProgress>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2305798291051514080L;


	public int getRepredictionCount() {
		int predictionCount=0;
		// for every instance in progress
		for (int i=0;i<this.size();i++)
		{
			// for every calculated (i.e. not cloned) prediction made for this instance
			ArrayList<PredictionItem> predictions=this.get(i).getPredictions();
			for (int j=0;j<predictions.size();j++)
			{
				if (predictions.get(j).getPredictionType()==PredictionItem.PredictionType.REPREDICTION)
					predictionCount=predictionCount+1;
			}
		}
		return predictionCount;
	}
	
	
}
