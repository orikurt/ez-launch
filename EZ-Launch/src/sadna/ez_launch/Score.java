package sadna.ez_launch;

public class Score implements Comparable<Score>{
	
	public String name;
	public float score;
	
	public Score(String name, float score){
		this.name = name;
		this.score = score;
	}
	
	public Score(String name){
		this.name = name;
		this.score = 0;
	}
	
	public String getName(){
		return this.name;
	}
	
	public float getScore(){
		return this.score;
	}
	
	public void setScore(float score){
		this.score = score;
	}
	
	public void adjustScore(float score){
		this.score += score;
	}
	
	@Override
	public int compareTo(Score other) {
		if (this.score > other.score){
			return 1;
		}
		else{
			if (this.score < other.score)
				return -1;
		}
		return 0;
	}
	
	
}
