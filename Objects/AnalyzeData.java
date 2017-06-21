package cn.hnsoft.sudokuGame.Objects;

import java.util.ArrayList;

public class AnalyzeData {
	private boolean error = false;
	private ArrayList<Integer> usedStrategies = new ArrayList<Integer>();
	private int level = 0;
	private double score = 0;
	private boolean finished = false;
	
	public boolean isFinished() {
		return finished;
	}
	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public boolean isError() {
		return error;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	public ArrayList<Integer> getUsedStrategies() {
		return usedStrategies;
	}
	public void setUsedStrategies(ArrayList<Integer> usedStrategies) {
		this.usedStrategies = usedStrategies;
	}
}
