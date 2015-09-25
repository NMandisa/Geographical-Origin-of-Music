/**
 * A class to define the structure of a cluster.
 *
 */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class cluster
{
int cluster_id;
List<instance> members;
instance centroid;

/**
 * Constructor.
 */
public cluster(instance ins, int id)
{
	cluster_id = id + 1;
	centroid = new instance(ins.features, ins.label_1, ins.label_2);
	members = new ArrayList<instance>();
}

/**
 * Assign an instance to the cluster.
 *
 * @param instance to be added.
 */
public void add_instance(instance inst)
{
	this.members.add(inst);
}

/**
 * Print the detail of the cluster.
 * @throws IOException 
 *
 */
public void print_cluster() throws IOException 
{
	//int label_count[][] = new int[2][kmeanscluster.NUMBER_OF_CLUSTERS];
	HashMap<Double,Integer> hm_label_1 = new HashMap<Double, Integer>();
	HashMap<Double,Integer> hm_label_2 = new HashMap<Double, Integer>();
	int max_label_1_count = -1, max_label_2_count = -1;
	double max_label_1 = -1, max_label_2 = -1;
	int number_of_diff_labels = 0;
	double misclassified_latitude = 0;
	double misclassified_longitude = 0;
	double distance_error = 0;
	
	for(instance inst : this.members)
	{
		if(hm_label_1.containsKey(inst.label_1))
			hm_label_1.put(inst.label_1, ((hm_label_1.get(inst.label_1))+1));
		else
			hm_label_1.put(inst.label_1, 1);
		
		if(hm_label_2.containsKey(inst.label_2))
			hm_label_2.put(inst.label_2, ((hm_label_2.get(inst.label_2))+1));
		else
			hm_label_2.put(inst.label_2, 1);
	}
	
	/*for(int i : hm_label_1.get(key))
	{	
		if(label_count[0][i] > max_label_1_count)
		{
			max_label = i;
			max_label_1_count = label_count[0][i];
		}
		if(label_count[1][i] > max_label_2_count)
		{
		max_label = i;
		max_label_2_count = label_count[1][i];
		}
	}*/
	
	
	double key;
	int value;
	Set<Entry<Double, Integer>> hash_set = hm_label_1.entrySet();
	
	for(Entry<Double, Integer> me : hash_set)
	{
		key = me.getKey();
		value = me.getValue();
		if(value > max_label_1_count)
		{
			max_label_1 = key;
			max_label_1_count = value;
		}
	}
	
	hash_set = hm_label_2.entrySet();
	
	for(Entry<Double, Integer> me : hash_set)
	{
		key = me.getKey();
		value = me.getValue();
		if(value > max_label_2_count)
		{
			max_label_2 = key;
			max_label_2_count = value;
		}
	}
	
	/*System.out.println();
	System.out.println("Most Common Label  in cluster " + this.cluster_id + " is " + max_label_1 + " " + max_label_2 + ".");
	
	System.out.println("Total instances in this cluster are " + this.members.size() + ".");
	
	for(instance inst : this.members)
		if(inst.label_1 != max_label_1 && inst.label_2 != max_label_2)
			number_of_diff_labels++;
	
	kmeanscluster.number_of_different_labels += number_of_diff_labels;
	
	System.out.println(" Number of members with different labels than most common label is " + number_of_diff_labels + ".");*/
	
//	PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Data\\Output.txt", true)));
//	pw.println();
//	pw.println("Most Common Label  in cluster " + this.cluster_id + " is " + max_label_1 + " " + max_label_2 + ".");
//	
//	pw.println("Total instances in this cluster are " + this.members.size() + ".");
	
	for(instance inst : this.members){
	//pw.println(inst.label_1 + " " + inst.label_2);
		if(inst.label_1 != max_label_1 && inst.label_2 != max_label_2)
		{
			double predicted_lat = max_label_1, predicted_long = max_label_2, actual_lat = inst.label_1, actual_long = inst.label_2;
			double R = 6373;
			
			double a = Math.pow(Math.sin((predicted_lat-actual_lat)/2),2.0) + ((Math.cos(predicted_long)) * (Math.cos(actual_long)) * (Math.pow(Math.sin((predicted_long-actual_long)/2),2.0)));
			double dist_km = 2*R*(Math.atan2(Math.sqrt(Math.abs(a)), Math.sqrt(Math.abs(1-a))));
			distance_error += dist_km;
			number_of_diff_labels++;
			misclassified_latitude += Math.abs(inst.label_1-max_label_1);
			misclassified_longitude += Math.abs(inst.label_2-max_label_2);
		}
	}
	
	kmeanscluster.number_of_different_labels += number_of_diff_labels;
	kmeanscluster.misclassified_latitude += misclassified_latitude;
	kmeanscluster.misclassified_longitude += misclassified_longitude;
	kmeanscluster.distance_error += distance_error;
	
	//pw.println(" Number of members with different labels than most common label is " + number_of_diff_labels + ".");
	//pw.close();
}

/**
 * Re-evaluate the centroid values for the cluster.
 *
 */
public void calculate_centroid() 
{
	if(!members.isEmpty())
	{
		kmeanscluster.iterate = false;
		for(int i = 0; i < kmeanscluster.NUMBER_OF_FEATURES; i++)
		{
			double sum = 0;
			for(instance inst : members)
				sum += inst.features[i];
			double average = sum/members.size();
			if(average != centroid.features[i])
			{
				centroid.features[i] = average;
				kmeanscluster.iterate = true;
			}
		}
	}
}

/**
 * Remove an instance from the cluster.
 *
 */
public void remove_instance(instance inst) 
{
	this.members.remove(inst);
}

}
