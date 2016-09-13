package nl.peterbloem.kit.distance;

import java.util.ArrayList;
import java.util.List;

import nl.peterbloem.kit.FrequencyModel;


/**
 * Determines the distance between two lists of tokens based on the cosine 
 * similarity between their term frequency vectors.
 * 
 * @author peter
 *
 * @param <T>
 */
public class FrequencyCosineDistance<T> implements Distance<List<T>>
{
	private static final long serialVersionUID = 1L;	
	
	private static CosineDistance cd = new CosineDistance();
	
	private List<T> featureVector;
	
	public FrequencyCosineDistance(List<T> featureVector)
	{
		this.featureVector = new ArrayList<T>(featureVector);
	}
	@Override
	public double distance(List<T> a, List<T> b)
	{
		FrequencyModel<T> aModel = new FrequencyModel<T>(),
		                       bModel = new FrequencyModel<T>();
		
		aModel.add(a);
		bModel.add(b);
		
		List<Double> aVector = new ArrayList<Double>(featureVector.size()),		
		             bVector = new ArrayList<Double>(featureVector.size());
		
		for(T feature: featureVector)
		{
			aVector.add(aModel.frequency(feature));
			bVector.add(bModel.frequency(feature));			
		}
		
		return cd.distance(aVector, bVector);
	}

}

