/**
 * This project is intended to solve the problem of 
 * predicting the geographical origin of music.
 * The dataset used is provided by 
 * Fang Zhou, The University of Nottinghan, Ningbo, China.
 * References:
 * [1] Fang Zhou, Claire Q and Ross. D. King 
 * Predicting the Geographical Origin of Music, ICDM, 2014
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Map.Entry;

/**
 * This class implements the Epsilon K-Nearest Neighbor Algorithm.
 * 80% of the Instances are used for training and the remaining
 * 20% are used for testing.
 */
public class KNN {
	static final int NUMBER_OF_INSTANCES = 847;
	static final int NUMBER_OF_TEST_INSTANCES = 212;
	static final int NUMBER_OF_FEATURES = 68;
	static final int NUMBER_OF_CLUSTERS = 33;
	
	static final int K = 19;
	static double epsilon = 6.0;
	
	//A list to hold all the instances read from the file.
	static final instance instance_list[] = new instance[NUMBER_OF_INSTANCES];
	static final instance test_instance_list[] = new instance[NUMBER_OF_TEST_INSTANCES];
	static boolean iterate = true;
	static int number_of_different_labels = 0;
	
	/**
	 * This function loads the training instances from the CSV file.
	 */
	void loadInstances()
	        throws IOException 
	{
	    BufferedReader br_instance = null;
	    try {
	        br_instance = new BufferedReader(new FileReader("C:\\Data\\Training_Data.csv"));
	        String instance_string;
	        int count = 0;
	        instance_string = br_instance.readLine();
	        while (count < NUMBER_OF_INSTANCES && (instance_string = br_instance.readLine()) != null)
	        {
	            String[] features_array = instance_string.split(",");
	            int i;
	            double features[] = new double[NUMBER_OF_FEATURES];
	            for (i = 0; i < features_array.length-2; i++) {
	                features[i] = Double.parseDouble(features_array[i]);
	            }
	            instance_list[count++] = new instance(features, Double.parseDouble(features_array[i]), Double.parseDouble(features_array[i+1]));
	        }
	    } finally {
	        if (br_instance != null)
	            br_instance.close();
	    }
	}
	
	/**
	 * This function loads the test instances from the CSV file 
	 * and implements the Epsilon KNN Algorithm.
	 */
	void testInstances() throws IOException
	{

		BufferedReader br_instance = new BufferedReader(new FileReader("C:\\Data\\Test_Data.csv"));
		String instance_string = br_instance.readLine();
		String[] features_array = instance_string.split(",");
		int count = 0;
	    while (count < NUMBER_OF_TEST_INSTANCES && (instance_string = br_instance.readLine())!=null)
	    {
	        features_array = instance_string.split(",");
	        int i;
	        double features[] = new double[NUMBER_OF_FEATURES];
	        for (i = 0; i < features_array.length-2; i++) {
	            features[i] = Double.parseDouble(features_array[i]);
	        }
	        test_instance_list[count++] = new instance(features, Double.parseDouble(features_array[i]), Double.parseDouble(features_array[i+1]));
	    }
	    
	    if (br_instance != null)
            br_instance.close();
	    
	    int misclassified_instances = 0;
	    double misclassified_latitude = 0.0;
	    double misclassified_longitude = 0.0;
	    double distance_error = 0.0;
	    int qwe = 0;
	    System.out.println("Test Phase");
	    for(instance test_inst : test_instance_list)
	    {		
	    	PriorityQueue<neighbour> pq = new PriorityQueue<neighbour>(new NComparator());
	    	HashMap<String, Integer> hm = new HashMap<String, Integer>();
	    	neighbour n;
	    	for(instance training_inst : instance_list)
	    	{
	    		double dist = 0;
	    		for(int i = 0; i < NUMBER_OF_FEATURES; i++)
	    		{
	    			dist += Math.pow(test_inst.features[i] - training_inst.features[i], 2);
	    		}
	    		pq.add(new neighbour(training_inst, Math.sqrt(dist)));
	    	}
	    	
	    	
	    	//Epsilon KNN implementation.
	    	while(!pq.isEmpty())
	    	{
	    		n=pq.remove();
	    		if(n.dist <= epsilon)
	    		{
	    			
	    			String key = Double.toString(n.inst.label_1) + " " + Double.toString(n.inst.label_2);
	    			if(hm.containsKey(key))
		    			hm.put(key, ((hm.get(key))+1));
		    		else
		    			hm.put(key,1);
	    		}
	    	}
	    	
	    	if(!hm.isEmpty())
	    	{
	    	int max = -1;
	    	String key,label = null;
	    	int value;
	    	Set<Entry<String, Integer>> hash_set = hm.entrySet();
	    	
	    	for(Entry<String, Integer> me : hash_set)
	    	{
	    		key = me.getKey();
	    		value = me.getValue();
	    		if(value > max)
	    		{
	    			label = key;
	    			max = value;
	    		}
	    	}
	    	
	    	String common_label[] = label.split(" ");
	    	Double common_label_1 = Double.parseDouble(common_label[0]);
	    	Double common_label_2 = Double.parseDouble(common_label[1]);
	    	
	    	//The formula for calculating the distance given the longitude and latitude is taken from [1].
	    	double predicted_lat = common_label_1, predicted_long = common_label_2;
	    	double actual_lat = test_inst.label_1, actual_long = test_inst.label_2;
			double R = 6373;
			
			double a = Math.pow(Math.sin((predicted_lat-actual_lat)/2),2.0) + ((Math.cos(predicted_long)) * (Math.cos(actual_long)) * (Math.pow(Math.sin((predicted_long-actual_long)/2),2.0)));
			double dist_km = 2*R*(Math.atan2(Math.sqrt(Math.abs(a)), Math.sqrt(Math.abs(1-a))));

	    	misclassified_latitude += Math.abs(test_inst.label_1 - common_label_1);
	    	misclassified_longitude += Math.abs(test_inst.label_2 - common_label_2);
	    	distance_error += dist_km;
	  
	    	if(test_inst.label_1 != common_label_1 && test_inst.label_2 != common_label_2)
	    		misclassified_instances++;
	    }
	    	else
	    	{	
	    		double predicted_lat = 0, predicted_long = 0, actual_lat = test_inst.label_1, actual_long = test_inst.label_2;
				double R = 6373;
				
				double a = Math.pow(Math.sin((predicted_lat-actual_lat)/2),2.0) + ((Math.cos(predicted_long)) * (Math.cos(actual_long)) * (Math.pow(Math.sin((predicted_long-actual_long)/2),2.0)));
				double dist_km = 2*R*(Math.atan2(Math.sqrt(Math.abs(a)), Math.sqrt(Math.abs(1-a))));
	    		misclassified_latitude += test_inst.label_1;
	    		misclassified_longitude += test_inst.label_2;
	    		distance_error += dist_km;
	    		qwe++;
	    	}
	    }
	    misclassified_latitude = misclassified_latitude/NUMBER_OF_TEST_INSTANCES;
	    misclassified_longitude = misclassified_longitude/NUMBER_OF_TEST_INSTANCES;
	    distance_error = distance_error/NUMBER_OF_TEST_INSTANCES;
	    System.out.println("Misclassified instances = " + misclassified_instances);
	    System.out.println("Misclassified latitude = " + misclassified_latitude);
	    System.out.println("Misclassified longitude = " + misclassified_longitude);
	    System.out.println(qwe);
	    System.out.println("Error in Distance = " + distance_error);

	}
	
	// The main function to implement the algorithm.
	public static void main(String[] args) {
		KNN obj = new KNN();
	// The model is run over the dataset for values of epsilon ranging form 
	// 6.0 to 9.0 increasing it by 0.5 for each epoch.
	while(epsilon<=9.0)
	{
		System.out.println("Epsilon = " + epsilon);
		try {
			obj.loadInstances();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			obj.testInstances();
		} catch (IOException e) {
			e.printStackTrace();
		}
		epsilon = epsilon + 0.5;
	}
	}
}

class neighbour{
	instance inst;
	double dist;
	
	public neighbour(instance inst, double dist) {
		this.inst = inst;
		this.dist = dist;
	}
}

class NComparator implements Comparator<neighbour>{

	public int compare(neighbour n1, neighbour n2) {
		if (n1.dist < n2.dist)
			return -1;
		else if (n1.dist > n2.dist)
			return 1;
		return 0;
			
	}
	
}
