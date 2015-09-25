/**
 * A class to define the structure of an instance .
 *
 */
public class instance
{
double[] features;
double label_1;
double label_2;
int cluster_id;

public instance(double features[], double label_1, double label_2)
{
	this.features = features;
	this.label_1 = label_1;
	this.label_2 = label_2;
	this.cluster_id = -1;
}

/**
 * To evaluate which cluster the instance should be assigned to.
 *
 */
public void assign_cluster(cluster[] cluster_list) 
{
	double min_dist = 2099999999.9999999;//Initialize to infinity
	int clus_id = -1;
	for(cluster clus : cluster_list)
	{
		double dist = calculate_distance(clus);
		if(dist<min_dist)
		{
			min_dist = dist;
			clus_id = clus.cluster_id;
		}
	}
	
	if(this.cluster_id != clus_id)
	{
		// Check if it is not the first iteration.
		if(this.cluster_id != -1)
			cluster_list[this.cluster_id].remove_instance(this);
		
		this.cluster_id = clus_id - 1;
		cluster_list[this.cluster_id].add_instance(this);
	}
}

/**
 * Calculate the distance of the instance from a cluster.
 *
 */
public double calculate_distance(cluster clus)
{
	double dist = 0;
	for(int i = 0; i < kmeanscluster.NUMBER_OF_FEATURES; i++)
	{
		dist += Math.pow(clus.centroid.features[i] - this.features[i], 2);
	}
	return Math.sqrt(dist);
}
}
