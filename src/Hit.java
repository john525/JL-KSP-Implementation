public class Hit implements Comparable<Hit> {
	
	String proteinName;
	String foldGuess;
	
	double importance;
	
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