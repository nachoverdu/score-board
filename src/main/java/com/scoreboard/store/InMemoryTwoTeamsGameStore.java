package com.scoreboard.store;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import com.scoreboard.GameCreator;
import com.scoreboard.GameManagement;
import com.scoreboard.GameQueries;
import com.scoreboard.Sports;
import com.scoreboard.TwoTeamsGame;

public final class InMemoryTwoTeamsGameStore implements GameManagement, GameQueries {
	private static final String NON_EXISTENT_GAME_EXCEPTION = "The game with id: %s does not exist";
	private static final String GAME_ID_SEP = "-";
	
	private Map<String, TwoTeamsGame> currentGames;
	
	private GameCreator gameFactory;
	
	private Semaphore lock;
	
	public InMemoryTwoTeamsGameStore(GameCreator gameFactory) {
		this.gameFactory = gameFactory;
		
		currentGames = new TreeMap<>();
		lock = new Semaphore(1, true);
	}

	@Override
	public void createGame(String homeTeamId, String awayTeamId, Sports sport) {
		try {
			lock.acquire();
			
			if (noneOfTheTeamsIsAlreadyPlaying(homeTeamId, awayTeamId)) {
				currentGames.put(gameIdFrom(homeTeamId, awayTeamId), gameFactory.createGame(homeTeamId, awayTeamId, sport));
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			lock.release();
		}
	}

	@Override
	public void updateGameScore(String homeTeamId, Integer newHomeScore, String awayTeamId, Integer newAwayScore) {
		try {
			lock.acquire();
			
			String gameId = gameIdFrom(homeTeamId, awayTeamId);
			if (currentGames.containsKey(gameId)) {
				currentGames.get(gameId).updateScores(newHomeScore, newAwayScore);
			} else {
				throw new RuntimeException(format(NON_EXISTENT_GAME_EXCEPTION, gameId));
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			lock.release();
		}	 
	}

	@Override
	public void finishGame(String homeTeamId, String awayTeamId) {
		try {
			lock.acquire();
			
			String gameId = gameIdFrom(homeTeamId, awayTeamId);
			if (currentGames.remove(gameId) == null) {
				throw new RuntimeException(format(NON_EXISTENT_GAME_EXCEPTION, gameId));
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			lock.release();
		}		
	}
	
	@Override
	public List<String> getGameSummaries() {
		try {
			lock.acquire();
			
			return currentGames.values().stream()
					.sorted((game1, game2) -> game1.getLastUpdated().isAfter(game2.getLastUpdated()) ? -1 : 1)
					.map(TwoTeamsGame::getGameInfo)
					.collect(toList());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			lock.release();
		}		
	}
	
	private boolean noneOfTheTeamsIsAlreadyPlaying(String homeTeam, String awayTeam) {
		return teamIsNotPlayingAlready(homeTeam) && teamIsNotPlayingAlready(awayTeam);
	}

	private boolean teamIsNotPlayingAlready(String teamId) {
		return !teamIsPlayingAlready(teamId);
	}
	
	private boolean teamIsPlayingAlready(String teamId) {
		return currentGames.keySet().stream().anyMatch(gameId -> gameId.contains(teamId));
	}
	
	private String gameIdFrom(String homeTeamId, String awayTeamId) {
		return join(GAME_ID_SEP, homeTeamId, awayTeamId);
	}
}