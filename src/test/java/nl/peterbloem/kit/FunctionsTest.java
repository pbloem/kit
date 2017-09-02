package nl.peterbloem.kit;

import static nl.peterbloem.kit.Functions.exp2;
import static nl.peterbloem.kit.Functions.log2;
import static nl.peterbloem.kit.Functions.log2Min;
import static nl.peterbloem.kit.Functions.log2Sum;
import static nl.peterbloem.kit.Functions.logMin;
import static nl.peterbloem.kit.Functions.prefixNeg;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import nl.peterbloem.kit.Functions;
import nl.peterbloem.kit.Series;

public class FunctionsTest
{

	@Test
	public void testLog2Sum()
	{
		testLog2Sum(2.0, 1.0, 1.0);
		testLog2Sum(1000.0, 1.0, 1000.0);
		
		assertEquals(log2(24), log2Min(5, 3), 0.0);
		
		double a = 5, b = 3, c = 1;
		assertEquals(log2(26.0), log2Min(log2Sum(a, c), b), 0.0);
	}
	
	public void testLog2Sum(double expected, double... values)
	{
		assertEquals(expected, log2Sum(values), 0.000000000000000000001);
		
		List<Double> v = new ArrayList<Double>(values.length);
		for(double val : values)
			v.add(val);
		assertEquals(expected, log2Sum(v), 0.000000000000000000001);
	}
	
	@Test
	public void testMulti()
	{
		for(int i : Series.series(100))
		{
			List<Double> w = Functions.randomMultinomial(5); 
			double sum = 0.0;
			for(double v : w)
				sum += v;
			assertEquals(1.0, sum, 0.0);
		}
		
	}
	
	@Test
	public void testTic() throws InterruptedException
	{
		double t0 = System.currentTimeMillis();
		Functions.tic();
		
		Thread.sleep(2000);
		
		double t = System.currentTimeMillis();
		assertEquals(2.0, (t - t0)/1000.0, 0.1); 
		assertEquals(2.0, Functions.toc(), 0.1); 
	}
	
	@Test
	public void testPow() 
	{
		assertEquals(25, Functions.pow(5, 2));
		assertEquals(32768, Functions.pow(2, 15));

	}
	
	@Test
	public void testPrefix() 
	{
		double prob;
		
		prob = 0.0;
		for(int n : Series.series(1000000))
			prob += exp2(-Functions.prefix(n));
		
		System.out.println(prob);
		assertEquals(1.0, prob, 0.0001);
		
		prob = 0.0;
		for(int n : Series.series(-1000000, 1000000))
			prob += exp2(-prefixNeg(n));
		
		System.out.println(prob);
		assertEquals(1.0, prob, 0.0001);
	}
}
