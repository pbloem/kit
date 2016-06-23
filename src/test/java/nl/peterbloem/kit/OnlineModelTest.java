package nl.peterbloem.kit;

import static nl.peterbloem.kit.Functions.log2;
import static nl.peterbloem.kit.Series.series;
import static org.junit.Assert.*;

import java.util.Arrays;

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

}
