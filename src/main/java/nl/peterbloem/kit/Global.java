package nl.peterbloem.kit;

import java.io.File;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Logger;

/**
 * This class contains methods and objects that are used globally by all other 
 * classes. 
 * 
 * It contains, for example, a single random number generator that all
 * classes are supposed to use for random numbers. This rng can then be set to 
 * an instance with a specific seed, to make all experiments deterministic and 
 * reproducible.
 *
 * For commonly useful methods available for all classes that do not have this 
 * mandatory character, see org.lilian.util.Functions  
 * 
 */

public class Global
{
	/**
	 * The default random seed. May be changed during runtime.
	 */
	public static final int RANDOM_SEED = 42;
	
	/**
	 * Switches to a secure random generator (with random seed)
	 */
	public static void secureRandom()
	{
		random = new SecureRandom();
	}
	
	public static void secureRandom(long seed)
	{
		byte[] bytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(seed).array();
		random = new SecureRandom(bytes);
	}
	
	private static Random random = new Random(RANDOM_SEED);
	public static Random random()
	{
		return random;
	}
	
	/**
	 * Gives the RNG a random seed (based on a reasonable source of randomness,
	 * such as time). This makes the code non-deterministic and non-repeatable.
	 * 
	 * The chosen seed is returned so that it can be stored/printed, and a run 
	 * of some program can be repeated deterministically, if it proves to be 
	 * interesting. The seed is also written to the info logger.
	 */
	public static long randomSeed()
	{
		long seed = new Random().nextLong(); 
		
		info("New seed: " + seed);
		setSeed(seed);
		
		return seed;
	}
	
	public static void setSeed(long seed)
	{
		random = new Random(seed);
	}
	
	/**
	 * Shorthand for the global logger
	 * @return
	 */
	public static Logger log() { return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); }
	
	/**
	 * Logs the given message to INFO.
	 * @param msg
	 * @return
	 */
	public static void info(String msg) { 
		log().info(msg);
	}

	
	private static int numThreads = Runtime.getRuntime().availableProcessors();
	
	/**
	 * Suggestion for the number of threads to use. Set to 
	 * Runtime.getRuntime().availableProcessors() by default.
	 * 
	 * @return
	 */
	public static int numThreads()
	{
		return numThreads;
	}
	
	/**
	 * Set the suggested number of threads.
	 * @return
	 */
	public static void setNumThreads(int numThreads)
	{
		Global.numThreads = numThreads;
	}
}
