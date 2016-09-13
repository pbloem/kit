package nl.peterbloem.kit.distance;

public class NaturalDistance<T extends Metrizable<T>> implements Distance<T> {

	public double distance(T a, T b) {
		return a.distance(b);
	}
	
}