package nl.peterbloem.kit;

import static nl.peterbloem.kit.Functions.log2;
import static nl.peterbloem.kit.Functions.max;
import static nl.peterbloem.kit.Functions.prefix;
import static nl.peterbloem.kit.Functions.prefixNeg;
import static org.apache.commons.math3.special.Gamma.digamma;
import static org.apache.commons.math3.special.Gamma.logGamma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A Pitman-Yor model for observing and encoding 
 * a sequence of tokens.
 * 
 * This is an extension of the DM model used for the {OnlineModel}, making it 
 * better equipped to handle sequences with a (zipf-like) uneven distribution over the 
 * alphabet.
 * 
 * The PitMan Yor model does not require all symbols to be known beforehand.
 * 
 * Note that it is assumed by the model that when a new symbol is seen, it is 
 * known beforehand which symbol it is. If this is not the case (e.g. the 
 * sequence is a non-contiguous sequence of integers) a codebook should be 
 * established before the code can be used. 
 * 
 * @author Peter
 *
 * @param <T>
 */
public class PitmanYorModel<T> extends FrequencyModel<T>
{
	private double alpha = 0.5;
	private double d = 0.1;
	
	public PitmanYorModel()
	{
		super(new HashSet<T>());
	}
	
	/**
	 * 
	 * @param smoothing
	 * @param d Controls the probability mass reserved for new symbols.
	 */
	public PitmanYorModel(double alpha, double d)
	{
		this();
		this.alpha = alpha;
		this.d = d;
	}
	
	/**
	 * Adds a symbol to the dictionary without incrementing any frequencies
	 * 
	 * @param symbol
	 */
	public void addToken(T symbol)
	{
		throw new UnsupportedOperationException("Tokens can only be added when observed.");
	}
	
	@Override
	public void add(T token)
	{
		super.add(token);
	}
	
	/**
	 * Combines the act of calculating the probability under the online model 
	 * and observing it
	 *  
	 * @return The probability of the given symbol according to the current model
	 * as it is before the symbol is added. 
	 */
	public double observe(T symbol)
	{
		double p = probability(symbol);
		
		add(symbol);
		
		return p;
	}
	
	/**
	 * The same as observe, but returns -log2 of the probability (the number of 
	 * bits required) to encode the symbol under the KT estimator.
	 * 
	 * Saferr for small probabilities.
	 * 
	 * @param symbol
	 * @return
	 */
	public double encode(T symbol)
	{		
		double num;  
		double den; 
		
		double bits = codelength(symbol);
		
		add(symbol);
		
		return bits;
	}

	/**
	 * Behaves as 'freq' separate calls to encode(symbol)
	 * @param symbol
	 * @param freq
	 * @return
	 */
	public double encode(T symbol, int freq)
	{
		throw new UnsupportedOperationException("TODO.");
	}
	
	@Override
	public double probability(T symbol)
	{
		if(frequencies.containsKey(symbol))
			return (frequency(symbol) - d) / (total() + alpha);

		return (alpha + d * distinct()) / (total() + alpha);
	}
	
	/**
	 * -log of the probability (does not observe the symbol).
	 * @param symbol
	 * @return
	 */
	public double codelength(T symbol)
	{
		double num;
		
		if(frequencies.containsKey(symbol))
			num = log2(frequency(symbol) - d);
		 else 
			num = log2(alpha + d * distinct());
		
		double den = log2(total() + alpha);

		return - (num - den);
	}
	
	/**
	 * This uses an online model to store a sequence of (nonnegative) integers, assuming that 
	 * the length of the sequence is known. 
	 * @param sequence
	 * @return
	 */
	public static double storeIntegers(List<Integer> sequence)
	{
		return storeIntegers(sequence, 0.5, 0.1);
	}	
	
	public static double storeIntegers(List<Integer> sequence, double alpha, double d)
	{
		if(sequence.isEmpty())
			return 0.0;

		double bits = 0.0;

		ArrayList<Integer> members = new ArrayList<>(new LinkedHashSet<>(sequence));
		Collections.sort(members);
		
		// * Store the dimensions
		// bits += prefix(sequence.size());
		bits += prefix(members.size());
		// * store the members
		bits += prefixNeg(members.get(0));
		for(int i : Series.series(1, members.size()))
			bits += prefix(members.get(i) - members.get(i - 1)); 
				
		PitmanYorModel<Integer> model = new PitmanYorModel<Integer>(alpha, d);
		
		for(int symbol : sequence)
			bits += - log2(model.observe(symbol)); 
		
		return bits;
	}

	/**
	 * Stores a sequence of integers, optimizing for the hyperparameters alpha
	 * and d. 
	 * @param sequence
	 * @return
	 */
	public static double storeIntegersOpt(List<Integer> sequence)
	{
		if(sequence.isEmpty())
			return 0.0;

		double preamble = 0.0;

		ArrayList<Integer> members = new ArrayList<>(new LinkedHashSet<>(sequence));
		Collections.sort(members);
		
		// * Store the dimensions
		// bits += prefix(sequence.size());
		preamble += prefix(members.size());
		// * store the members
		preamble += prefixNeg(members.get(0));
		for(int i : Series.series(1, members.size()))
			preamble += prefix(members.get(i) - members.get(i - 1)); 
				
		double shortest = Double.POSITIVE_INFINITY;
		for(double alpha : Series.series(1/16.0, 1/16.0, 1.0))
			for(double d : Series.series(0.0, 1/16.0, 1.0))
			{
				PitmanYorModel<Integer> model = new PitmanYorModel<Integer>(alpha, d);
				
				double cl = 0.0;
				for(int symbol : sequence)
					cl += - log2(model.observe(symbol)); 
				
				shortest = Math.min(shortest, cl + preamble);
				
			}
				
		return shortest + 8; // return the shortest codelength, plus 8 bits to 
							 // store the two parameter values
	}
	
	/**
	 * Returns the number of bits required to store the given sequence.
	 * 
	 * Does not include the cost of storing the symbols themselves or the length
	 * of the sequence.
	 * 
	 * @param sequence
	 * @return
	 */
	public static <L> double storeSequence(List<L> sequence)
	{
		Set<L> symbols = new LinkedHashSet<L>(sequence);
		
		PitmanYorModel<L> model = new PitmanYorModel<L>();
		
		double bits = 0.0;
		for(L symbol : sequence)
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
	public static <L> double storeSequenceML(List<L> sequence)
	{
		
		FrequencyModel<L> model = new FrequencyModel<L>(sequence);

		double bits = 0.0;
		for(L symbol : sequence)
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
