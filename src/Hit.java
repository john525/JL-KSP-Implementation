import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Hit implements Comparable<Hit> {
	
	String proteinName;
	String foldGuess;
	
	double importance;
	/**
	 * Used to calculate an importance value using probability theory.
	 * @param p
	 * @param g
	 * @param imps
	 */
	public Hit(String p, String g, List<Double> importances) {
		proteinName = p;
		foldGuess = g;
		
		double probAllWrong = 1.0;
		for(Double probCorrect : importances) {
			probAllWrong *= 1.0-probCorrect;
		}
		importance = 1.0-probAllWrong;
	}
	
	/**
	 * Used if you already have an importance value.
	 * @param p
	 * @param g
	 * @param i
	 */
	public Hit(String p, String g, double i) {
		proteinName = p;
		foldGuess = g;
		importance = i;
	}
	
	@Override
	public int compareTo(Hit o) {
		if(importance > o.importance) return 1;
		else if(importance < o.importance) return -1;
		else return 0;
	}
	
}