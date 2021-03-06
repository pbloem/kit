package nl.peterbloem.kit.search;

import java.util.ArrayList;
import java.util.List;

import nl.peterbloem.kit.Global;

/**
 * Static helper functions for parametrizable objects
 * @author Peter
 *
 */
public class Parameters
{

	public static <M extends Parametrizable> M random(Builder<M> builder, double variance)
	{
		int n = builder.numParameters();
		List<Double> parameters = random(n, variance);
		
		return builder.build(parameters);
	}
	
	public static List<Double> random(int size, double var)
	{
		List<Double> r = new ArrayList<Double>(size);
		
		for(int i = 0; i < size; i++)
			r.add(Global.random().nextGaussian() * var);
		
		return r;		
	}
	
	public static <M extends Parametrizable> M perturb(
			M in, Builder<M> builder, double variance)
	{
		List<Double> params = perturb(in.parameters(), variance);
		
		return builder.build(params);
	}
	
	public static List<Double> perturb(List<Double> in, double variance)
	{
		List<Double> params = new ArrayList<Double>(in.size());
		
		for(double d : in)
			params.add(d + Global.random().nextGaussian() * variance);
		
		return params;
		
	}
}
