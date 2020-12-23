package com.scoreboard;

public interface GameManagement {
	void createGame(String homeTeamId, String awayTeamId, Sports sport);
	void updateGameScore(String homeTeamId, Integer newHomeScore, String awayTeamId, Integer newAwayScore);
	void finishGame(String homeTeamId, String awayTeamId);
}
