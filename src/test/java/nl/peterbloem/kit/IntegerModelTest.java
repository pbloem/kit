package nl.peterbloem.kit;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class IntegerModelTest
{

	@Test
	public void test1()
	{
		int n = 1000000;
		List<Integer> seq = new ArrayList<Integer>(n);
		
		for(int i : Series.series(n / 2))
		{
			seq.add(0);
			seq.add(4567);
		}
		
		System.out.println(seq.size());
		
		System.out.println(IntegerModel.storeML(seq));
		System.out.println(IntegerModel.store(seq));
		System.out.println(PitmanYorModel.storeIntegers(seq));
	}

}
