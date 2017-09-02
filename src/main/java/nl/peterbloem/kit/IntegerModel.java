package nl.peterbloem.kit;

import static nl.peterbloem.kit.Functions.log2;
import static nl.peterbloem.kit.Functions.max;
import static nl.peterbloem.kit.Functions.prefix;
import static org.apache.commons.math3.special.Gamma.digamma;
import static org.apache.commons.math3.special.Gamma.logGamma;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A KT estimator (or Dirichlet-Multinomial model) for observing and encoding 
 * a sequence of nonnegative integers. Integer-specific version of the OnlineModel

 * @author Peter
 *
 * @param <T>
 */
public class IntegerModel extends FrequencyModel<Integer>
{
	private double smoothing = 0.5;
	private int max;
	
	/**
	 * 
	 * @param max Maximum integer we can encounter
	 */
	public IntegerModel(int max)
	{
		this.max = max;
	}
	
	@Override
	public void add(Integer token)
	{
		if(token > max)
			throw new IllegalArgumentException("Input ("+token+") larger than max ("+max+").");
			
		super.add(token);
	}

	public IntegerModel(double smoothing, int max)
	{
		this(max);
		this.smoothing = smoothing;
	}
	
	/**
	 * Combines the act of calculating the probability under the online model 
	 * and observing it
	 *  
	 * @return The probability of the given symbol according to the current model
	 * as it is before the symbol is added. 
	 */
	public double observe(Integer symbol)
	{
		double p = probability(symbol);
		
		add(symbol);
		
		return p;
	}
	
	/**
	 * The same as observe, but returns -log2 of the probability (the number of 
	 * bits required) to encode the symbol under the KT estimator.
	 * 
	 * Safer for small probabilities.
	 * 
	 * @param symbol
	 * @return
	 */
	public double encode(Integer symbol)
	{
		if(distinct() == 0.0)
			return Double.NaN;
			
		double num = log2(frequency(symbol) + smoothing); 
		double den = log2(total() + smoothing * distinct());
		
		double bits = - (num - den);
		
		add(symbol);
		
		return bits;
	}

	/**
	 * Behaves as 'freq' separate calls to encode(symbol)
	 * @param symbol
	 * @param freq
	 * @return
	 */
	public double encode(Integer symbol, int freq)
	{
		if(freq < 1)
			throw new IllegalArgumentException("Frequency must be 1 or larger (was "+freq+")");
		if(freq == 1)
			return encode(symbol);
		
		if(! super.frequencies.keySet().contains(symbol))
		{
			double p = encode(symbol);
			return p + encode(symbol, freq - 1);
		}
		
		double a = frequency(symbol) + smoothing;
		double b = total() + smoothing * distinct();

		double bits = - log2Product(a, b, freq - 1);
		
		add(symbol, freq);
		
		return bits;
	}
	
	@Override
	public double probability(Integer symbol)
	{
		if(symbol < 0 || symbol > max)
			throw new IllegalArgumentException("Input ("+symbol+") outside of legal range (0, "+max+")");
			
		return (frequency(symbol) + smoothing) / (total() + smoothing * distinct());
	}	
	
	@Override
	public double distinct()
	{
		return max + 1;
	}

	/**
	 * This uses an online model to store a sequence of (nonnegative) integers, assuming that 
	 * the maximum value and length of the sequence are known. 
	 * @param sequence
	 * @return
	 */
	public static double store(List<Integer> sequence)
	{
		if(sequence.isEmpty())
			return 0.0;
		
		int max = Functions.max(sequence);
		
		IntegerModel model = new IntegerModel(max);
		
		double bits = 0.0;
		for(int symbol : sequence)
			bits += - log2(model.observe(symbol)); 
		
		return bits;
	}

	
	
	/**
	 * The cost of storing the given sequence under the maximum likelihood model:
	 * ie. the case when the precise relative frequencies of each symbol in the 
	 * sequence are known to both sender and receiver.  
	 * @param sequence
	 * @return
	 */
	public static double storeML(List<Integer> sequence)
	{
		
		FrequencyModel<Integer> model = new FrequencyModel<Integer>(sequence);

		double bits = 0.0;
		for(Integer symbol : sequence)
			bits += -Functions.log2(model.probability(symbol));
		
		return bits;
	}
	
	/**
	 * Computes the product <pre>\prod_{i = 0}^k \frac{a+i}{b+i}</pre> quickly 
	 * 
	 * @param a
	 * @param b
	 * @param k
	 * @return
	 */
	public static double log2Product(double a, double b, int k)
	{
		double logNum =  logGamma(b) + logGamma(a + (double)k + 1.0);
		double logDen = logGamma(a) + logGamma(b + (double)k + 1.0);
		double res = logNum - logDen;
		
		return res / Math.log(2.0);
	}
}
