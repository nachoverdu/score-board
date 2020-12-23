package com.scoreboard;

import java.time.LocalDateTime;

public interface TwoTeamsGame {
	void updateScores(int newHomeTeamScore, int newAwayTeamScore);
	String getGameInfo();
	LocalDateTime getLastUpdated();
}
