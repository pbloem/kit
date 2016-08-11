package nl.peterbloem.kit;

import static java.util.Arrays.asList;
import static nl.peterbloem.kit.Functions.log2;
import static nl.peterbloem.kit.Functions.tic;
import static nl.peterbloem.kit.Functions.toc;
import static nl.peterbloem.kit.Series.series;
import static org.apache.commons.math3.special.Gamma.digamma;
import static org.apache.commons.math3.special.Gamma.gamma;
import static org.apache.commons.math3.special.Gamma.logGamma;
import static org.apache.commons.math3.special.Gamma.logGamma1p;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.special.Gamma;
import org.junit.Test;

import nl.peterbloem.kit.Functions;
import nl.peterbloem.kit.OnlineModel;

public class OnlineModelTest
{

	@Test
	public void test()
	{
		int n = 10000;
		
		OnlineModel<Boolean> om = new OnlineModel<Boolean>(Arrays.asList(true, false));
				
		double cl = 0;
		for(int i : series(n))
		{
			double p = om.observe(Global.random().nextBoolean());
			cl += - log2(p);
		}
		
		assertEquals((double)n, cl, 100.0);
	}
	
	@Test
	public void testTwo()
	{
		for(int rep : series(100))
		{
			double prob = Global.random().nextDouble();
			int n = 10000;
			
			OnlineModel<Integer> om = new OnlineModel<Integer>(Arrays.asList(0, 1));
					
			double cl = 0;
			for(int i : series(n))
			{
				double p = om.observe(Functions.choose(Arrays.asList(prob,  1.0 - prob), 1.0));
				cl += - Functions.log2(p);
			}
			
			double ent = - prob * log2(prob) - (1.0 - prob) * log2(1.0 - prob);
	 		
			assertEquals(n*ent, cl, 300.0);
		}
	}
	
	@Test
	public void testInvariance()
	{
		List<Integer> symbols = Arrays.asList(0, 1, 2, 3);
		OnlineModel<Integer> a = new OnlineModel<Integer>(symbols);
		OnlineModel<Integer> b = new OnlineModel<Integer>(symbols);
		
		double bitsA = 0.0;
		double bitsB = 0.0;
		
		bitsA += -log2(a.observe(0));
		bitsA += -log2(a.observe(1));		
		bitsA += -log2(a.observe(0));
		bitsA += -log2(a.observe(0));		
		bitsA += -log2(a.observe(2));		
		bitsA += -log2(a.observe(0));		
		bitsA += -log2(a.observe(0));		
		bitsA += -log2(a.observe(3));		
		bitsA += -log2(a.observe(3));		
		bitsA += -log2(a.observe(0));		
		bitsA += -log2(a.observe(3));		
		bitsA += -log2(a.observe(0));		
		bitsA += -log2(a.observe(0));		
		bitsA += -log2(a.observe(1));		
		bitsA += -log2(a.observe(2));		
		bitsA += -log2(a.observe(1));
				
		bitsB += -log2(b.observe(0));
		bitsB += -log2(b.observe(0));		
		bitsB += -log2(b.observe(0));
		bitsB += -log2(b.observe(0));		
		bitsB += -log2(b.observe(0));		
		bitsB += -log2(b.observe(0));		
		bitsB += -log2(b.observe(0));		
		bitsB += -log2(b.observe(0));		
		bitsB += -log2(b.observe(1));		
		bitsB += -log2(b.observe(1));		
		bitsB += -log2(b.observe(1));		
		bitsB += -log2(b.observe(2));		
		bitsB += -log2(b.observe(2));		
		bitsB += -log2(b.observe(3));		
		bitsB += -log2(b.observe(3));		
		bitsB += -log2(b.observe(3));
				
		assertEquals(bitsA, bitsB, 0.0000000000001);
	}	

	@Test
	public void testThree()
	{
		List<Integer> symbols = Arrays.asList(0, 1, 2, 3);
		OnlineModel<Integer> a = new OnlineModel<Integer>(symbols);
		OnlineModel<Integer> b = new OnlineModel<Integer>(symbols);
		
		double bitsA = 0.0;
		double bitsB = 0.0;
		
		bitsA += a.encode(0);
		bitsA += a.encode(1);		
		bitsA += a.encode(0);
		bitsA += a.encode(0);		
		bitsA += a.encode(2);		
		bitsA += a.encode(0);		
		bitsA += a.encode(0);		
		bitsA += a.encode(3);		
		bitsA += a.encode(3);		
		bitsA += a.encode(0);		
		bitsA += a.encode(3);		
		bitsA += a.encode(0);		
		bitsA += a.encode(0);		
		bitsA += a.encode(1);		
		bitsA += a.encode(2);		
		bitsA += a.encode(1);
				
		bitsB += b.encode(0, 8);
		bitsB += b.encode(1, 3);
		bitsB += b.encode(2, 2);
		bitsB += b.encode(3, 3);
		
		System.out.println(bitsA + " " + bitsB);
				
		assertEquals(bitsA, bitsB, 0.000000001);
	}
	
	@Test
	public void testSpeed()
	{
		int k = 10000000;
		
		OnlineModel<Boolean> om;
		double bits;
		
		om = new OnlineModel<Boolean>(asList(true, false));
		bits = 0.0;
		
		tic();
		for(int i : series(k))
			bits += - log2(om.observe(i%2 == 0));
		System.out.println("Slow method took " + toc() + " seconds: " + bits);
		
		om = new OnlineModel<Boolean>(asList(true, false));
		bits = 0.0;
		
		tic();
		bits += om.encode(true, k/2);
		bits += om.encode(false, k/2);
		System.out.println("Fast method took " + toc() + " seconds: " + bits);
	}
	
	@Test
	public void testGamma()
	{
		double a = 1.5, b = 6.0;
		int k = 2; // number of terms in the product
		
		tic();
		double slow = 1.0;
		for(int i : Series.series(k))
			slow *= (a + i)/(b + i);
		System.out.println("Slow method took " + toc() + " seconds: " + slow);
		
		tic();
		double fast = Math.pow(2.0, OnlineModel.log2Product(a, b, k - 1));
		System.out.println("Fast method took " + toc() + " seconds: " + fast);
		
		assertEquals(slow, fast, slow * 0.00001);
	}
}
