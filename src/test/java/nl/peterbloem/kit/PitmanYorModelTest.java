package nl.peterbloem.kit;

import static java.util.Arrays.asList;
import static nl.peterbloem.kit.Functions.exp2;
import static nl.peterbloem.kit.Functions.log2;
import static nl.peterbloem.kit.Functions.log2Sum;
import static nl.peterbloem.kit.Functions.tic;
import static nl.peterbloem.kit.Functions.toc;
import static nl.peterbloem.kit.Series.series;
import static org.apache.commons.math3.special.Gamma.digamma;
import static org.apache.commons.math3.special.Gamma.gamma;
import static org.apache.commons.math3.special.Gamma.logGamma;
import static org.apache.commons.math3.special.Gamma.logGamma1p;
import static org.junit.Assert.*;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.math3.special.Gamma;
import org.junit.Test;

import nl.peterbloem.kit.Functions;
import nl.peterbloem.kit.OnlineModel;

public class PitmanYorModelTest
{

	@Test
	public void test()
	{
		int n = 10000;
		
		PitmanYorModel<Boolean> om = new PitmanYorModel<Boolean>();
				
		double cl = 0;
		for(int i : series(n))
		{
			boolean bit = Global.random().nextBoolean();
			double p = om.observe(bit);
			// System.out.println(bit + " " + p + " " + cl);
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
			
			PitmanYorModel<Integer> om = new PitmanYorModel<Integer>();
					
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
	public void testSum()
	{
		Double prob = null;
		
		for(List<Integer> string : asList(asList(0, 0), asList(0, 1)))
		{
			double p = 0.0;
			PitmanYorModel<Integer> om = new PitmanYorModel<Integer>(0.5, 0.1);
			for(int elem : string)
			{
				p += om.encode(elem);
			}
			
			prob = prob == null ? -p : log2Sum(prob, -p);
		}
	
		assertEquals(1.0, exp2(prob), 0.00001);
	}
	
	@Test
	public void testSum2()
	{
		
		for(int size : series(1, 15))
		{
    		Double prob = null;
    		
    		for(List<Integer> string : new Partitions(size))
    		{
    			double p = 0.0;
    			PitmanYorModel<Integer> om = new PitmanYorModel<Integer>(0.5, 0.1);
    			for(int elem : string)
    			{
    				p += om.encode(elem);
    			}
    			
    			prob = prob == null ? -p : log2Sum(prob, -p);
    		}
    	
    		assertEquals(1.0, exp2(prob), 0.00001);
		}
	}
	
	@Test
	public void testSum3()
	{
		int size = 20, max = 1;

		Double prob = null;
		
		for(List<Integer> string : new Sequences(size, max))
		{
			double bits = PitmanYorModel.storeIntegers(string);
			
			prob = prob == null ? -bits : log2Sum(prob, -bits);

		}    			
		
		
		System.out.println((max+1)+ "^" + size + " sequences: " + exp2(prob));

		
		assert(prob < 1.0 + 0.01);

	}
	
	
	@Test
	public void testGenerator()
	{
		
		for(int i : series(5))
		{
			int n = 0;
			for(List<Integer> seq : new Partitions(i))
				n++;
			System.out.println(i + "\t: " + n );
		}
		
		for(int max : series(5))
			for(int size : series(5))
    		{
    			int n = 0;
    			for(List<Integer> seq : new Sequences(size, max))
    				n++;
    			System.out.println((max+1)+ "^" + size + " =  " + n );
    		}
	}
	/**
	 * Iterates over all possible partitions of a given size
	 * @author Peter
	 *
	 */
	public class Partitions implements Iterable<List<Integer>> 
	{
		int size;
	
		public Partitions(int size)
		{
			this.size = size;
		}

		@Override
		public Iterator<List<Integer>> iterator()
		{
			return new Iterator<List<Integer>>()
			{
				private Deque<List<Integer>> stack = new LinkedList<>();
				private Queue<List<Integer>> buffer = new LinkedList<>();
				
				{
					stack.push(new ArrayList<>());
				}
				
				@Override
				public boolean hasNext()
				{
					if(size == 0)
						return false;
					check();
					return ! buffer.isEmpty();
				}

				@Override
				public List<Integer> next()
				{
					if(size == 0)
						throw new NoSuchElementException();
					check();
					return buffer.poll();
				}

				private void check()
				{
					while(buffer.isEmpty() && ! stack.isEmpty())
					{
						List<Integer> head = stack.pop();
						
						List<Integer> copy = new ArrayList<Integer>(head);
						
						Integer max = Functions.max(head);
						copy.add(max == null ? 0 : max + 1);
						
						push(copy);

						for(Integer elem : new HashSet<>(head))
						{
							copy = new ArrayList<Integer>(head);
							copy.add(elem);
							
							push(copy);
						}
					}
				}
				
				private void push(List<Integer> elem)
				{
					if(elem.size() == size)
						buffer.add(elem);
					else
						stack.push(elem);
				}
			};
		}	
	}
	
	/**
	 * Iterates over all possible partitions of a given size
	 * @author Peter
	 *
	 */
	public class Sequences implements Iterable<List<Integer>> 
	{
		int size;
		int max;
	
		public Sequences(int size, int max)
		{
			this.size = size;
			this.max = max;
		}

		@Override
		public Iterator<List<Integer>> iterator()
		{
			return new Iterator<List<Integer>>()
			{
				private Deque<List<Integer>> stack = new LinkedList<>();
				private Queue<List<Integer>> buffer = new LinkedList<>();
				
				{
					stack.push(new ArrayList<>());
				}
				
				@Override
				public boolean hasNext()
				{

					check();
					return ! buffer.isEmpty();
				}

				@Override
				public List<Integer> next()
				{
				
					check();
					return buffer.poll();
				}

				private void check()
				{
					while(buffer.isEmpty() && ! stack.isEmpty())
					{
						List<Integer> head = stack.pop();
						if(head.size() == size)
							buffer.add(head);
						else
						{
    						List<Integer> copy = new ArrayList<Integer>(head);
    						
    						for(Integer elem : series(max+1))
    						{
    							copy = new ArrayList<Integer>(head);
    							copy.add(elem);
    							
    							stack.push(copy);
    						}
						}
					}
				}
			};
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
