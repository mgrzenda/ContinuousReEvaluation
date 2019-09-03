# Continuous Re-evaluation

Repository of a custom MOA version including support for continuous re-evaluation, implemented in 2018-2019. The code of continuous re-evaluation is used to assess performance of prediction models under delayed labeling scenarios. It makes it possible to request multiple predictions from evolving classification or regression model for every instance waiting for true label under delayed labelling cases. The accuracy of predictions made in individual subperiods between unlabelled instance arrival and its true label arrival can be evaluated. 

This code is planned to be available in MOA in the future.

For further details on MOA, see project website: 
http://moa.cms.waikato.ac.nz 

## Citating this work
If you use the continuos re-evaluation in a publication, please cite the following paper: 
> Maciej Grzenda, Heitor Murilo Gomes, Albert Bifet. Delayed Labelling Evaluation for Data Streams. 
> Data Mining and Knowledge Discovery. *In press*, Springer.

## Key source files
Some of the key changes made to MOA include changes in the following files:
* EvaluatePrequential.java: this version of the task enables prediction when unlabelled instance arrives and in the period preceding true label arrival for classification tasks
* EvaluatePrequentialRegression.java: this version of the task enables prediction when unlabelled instance arrives and in the period preceding true label arrival for regression tasks

Some of important new classes include:
* InstancesInProgress.java: The buffer of instances awaiting true labels, including possibly many predictions made for each of these instances in the period preceding arrival of individual true labels
* InstanceInProgress.java: Single instance waiting for true label, including predictions made for this instance.
