package com.scoreboard.store;

import static java.lang.Integer.compare;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;
import java.util.Optional;

import com.scoreboard.TwoTeamsGame;

public class FootballGame implements TwoTeamsGame {
	private static final String UPDATE_AT_THE_SAME_TIME_EXCEPTION = "Only one team score can be updated at the same time";
	private static final String CANCEL_GOAL_AT_THE_SAME_TIME_EXCEPTION = "Invalid score, both teams can't have a cancelled goal at the same time";
	private static final String SCORE_AT_THE_SAME_TIME_EXCEPTION = "Invalid score, both teams can't score at the same time";
	private static final String AWAY_TEAM_SCORE_EXCEPTION = "Invalid new awayTeamScore, from oldScore: %s to newScore: %s";
	private static final String HOME_TEAM_SCORE_EXCEPTION = "Invalid new homeTeamScore, from oldScore: %s to newScore: %s";
	private static final String NEGATIVE_SCORE_EXCEPTION = "Error scores can't be negative, newHomeTeamScore: %s newAwayTeamScore: %s";
	private static final String CONSTRUCTOR_EXCEPTION = "Can't create a football game with both same contenders";
	
	private static final int INITIAL_SCORE = 0;
	private static final int CANCEL_GOAL = -1;
	private static final int NEW_GOAL = 1;
	private static final int NO_DIFFERENCE_GOAL = 0;
	
	private final Team homeTeam;
	private int homeTeamScore;
	
	private final Team awayTeam;
	private int awayTeamScore;
	
	private LocalDateTime lastUpdated;
	
	public FootballGame(Team homeTeam, Team awayTeam) {
		if (homeTeam.getTeamName().equals(awayTeam.getTeamName())) {
			throw new RuntimeException(CONSTRUCTOR_EXCEPTION);
		}
		
		this.homeTeam = homeTeam;
		this.awayTeam = awayTeam;
		
		setInitalScore();
	}

	@Override
	public void updateScores(int newHomeTeamScore, int newAwayTeamScore) {
		if (newHomeTeamScore < 0 || newAwayTeamScore < 0) {
			throw new RuntimeException(format(NEGATIVE_SCORE_EXCEPTION, newHomeTeamScore, newAwayTeamScore));
		}
		
		getNewScoresError(newHomeTeamScore, newAwayTeamScore).ifPresent(newScoresError -> {
			throw new RuntimeException(newScoresError.toString());
		});
		
		homeTeamScore = newHomeTeamScore;	
		awayTeamScore = newAwayTeamScore;
		lastUpdated = now();
	}

	@Override
	public String getGameInfo() {
		return toString();
	}
	
	@Override
	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}

	@Override
	public String toString() {
		StringBuilder game = new StringBuilder();
		
		game.append("Game [homeTeam=");
		game.append(homeTeam.toString());
		game.append(", homeTeamScore=");
		game.append(homeTeamScore);
		game.append(", awayTeam=");
		game.append(awayTeam.toString());
		game.append(", awayTeamScore=");
		game.append(awayTeamScore);
		game.append("]");
		
		return game.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((awayTeam == null) ? 0 : awayTeam.hashCode());
		result = prime * result + ((homeTeam == null) ? 0 : homeTeam.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FootballGame other = (FootballGame) obj;
		if (awayTeam == null) {
			if (other.awayTeam != null)
				return false;
		} else if (!awayTeam.equals(other.awayTeam))
			return false;
		if (homeTeam == null) {
			if (other.homeTeam != null)
				return false;
		} else if (!homeTeam.equals(other.homeTeam))
			return false;
		return true;
	}
	
	private Optional<String> getNewScoresError(int newHomeTeamScore, int newAwayTeamScore) {
		StringBuilder errorMessage = new StringBuilder();
		
		if (isNotAValidNewScore(homeTeamScore, newHomeTeamScore)) {
			errorMessage.append(format(HOME_TEAM_SCORE_EXCEPTION, homeTeamScore, newHomeTeamScore));
		}
		
		if (isNotAValidNewScore(awayTeamScore, newAwayTeamScore)) {
			errorMessage.append(format(AWAY_TEAM_SCORE_EXCEPTION, awayTeamScore, newAwayTeamScore));
		}
		
		if (isANewGoal(homeTeamScore, newHomeTeamScore) && isANewGoal(awayTeamScore, newAwayTeamScore)) {
			errorMessage.append(format(SCORE_AT_THE_SAME_TIME_EXCEPTION));
		}
		
		if (isACancelGoal(homeTeamScore, newHomeTeamScore) && isACancelGoal(awayTeamScore, newAwayTeamScore)) {
			errorMessage.append(format(CANCEL_GOAL_AT_THE_SAME_TIME_EXCEPTION));
		}
		
		if ( (isANewGoal(homeTeamScore, newHomeTeamScore) && isACancelGoal(awayTeamScore, newAwayTeamScore)) ||
			 (isACancelGoal(homeTeamScore, newHomeTeamScore) && isANewGoal(awayTeamScore, newAwayTeamScore)) ){
			errorMessage.append(format(UPDATE_AT_THE_SAME_TIME_EXCEPTION));
		}
		
		return ofNullable(errorMessage.length() > 0 ? errorMessage.toString() : null);
	}
	
	private boolean isNotAValidNewScore(int oldScore, int newScore) {
		return !isAValidNewScore(oldScore, newScore);
	}
	
	private boolean isAValidNewScore(int oldScore, int newScore) {
		return isACancelGoal(oldScore, newScore) ? 
				isNotInitalScore(oldScore) :
					isANewGoal(oldScore, newScore) || doesNotChange(oldScore, newScore) ? true : false;
	}
	
	private boolean isACancelGoal(int oldScore, int newScore) {
		return compare(newScore - oldScore, CANCEL_GOAL) == 0;
	}
	
	private boolean isANewGoal(int oldScore, int newScore) {
		return compare(newScore - oldScore, NEW_GOAL) == 0;
	}

	private boolean doesNotChange(int oldScore, int newScore) {
		return compare(oldScore - newScore, NO_DIFFERENCE_GOAL) == 0;
	}
	
	private boolean isNotInitalScore(int score) {
		return !isInitalScore(score);
	}
	
	private boolean isInitalScore(int score) {
		return compare(score, INITIAL_SCORE) == 0;
	}
	
	private void setInitalScore() {
		homeTeamScore = INITIAL_SCORE;
		awayTeamScore = INITIAL_SCORE;
		lastUpdated = now();
	}
}
