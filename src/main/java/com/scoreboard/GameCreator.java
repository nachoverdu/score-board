package com.scoreboard;

import static com.scoreboard.Sports.FOOTBALL;
import static java.lang.String.format;

import com.scoreboard.store.FootballGame;
import com.scoreboard.store.Team;

public interface GameCreator {
	public default TwoTeamsGame createGame(String homeTeamId, String awayTeamId, Sports sport) {
		if (sport.equals(FOOTBALL)) {
			return new FootballGame(new Team(homeTeamId), new Team(awayTeamId));
		}
		
		throw new RuntimeException(format("Not supported Sport: %s", sport.name()));
	}
}
