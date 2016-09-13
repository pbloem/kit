package nl.peterbloem.kit.data.classification;

import java.util.List;

import nl.peterbloem.kit.data.Point;

/**
 * A class which assigns points to one of a fixed number of categories.
 * 
 * Classes are represented by non-negative integers. If integer n is a class then all 
 * integers below it are also valid classes.
 * 
 * @author Peter
 *
 */
public interface Classifier
{
	/**
	 * Classify a given point
	 * 
	 * @return
	 */
	public int classify(Point in);
	
	public List<Integer> classify(List<Point> in);
	
	/**
	 * Returns a probability distribution over classes for the given point.
	 * 
	 * The values returned may also be densities, and so do not necessarily sum
	 * to one.
	 * @param point
	 * @return
	 */
	public List<Double> probabilities(Point point);	
	
	public int dimension();
	
	/**
	 * The number of classes.
	 * 
	 * @return
	 */
	public int size();
}
