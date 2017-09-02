package nl.peterbloem.kit.data;

import java.util.Collection;

public abstract class AbstractDensity implements Density
{

	@Override
	public abstract double density(Point p);

	public double logDensity(Point p)
	{
		return Math.log(density(p));
	}
	
	public double logDensity(Collection<Point> points)
	{
		double ld = 0.0;
		for(Point p : points)
			ld += logDensity(p);
		
		return ld;
	}
	
	@Override
	public abstract int dimension();

}
