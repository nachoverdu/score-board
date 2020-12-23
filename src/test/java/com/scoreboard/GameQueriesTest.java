package com.scoreboard;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.Semaphore;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.scoreboard.store.FootballGame;
import com.scoreboard.store.InMemoryTwoTeamsGameStore;
import com.scoreboard.store.Team;

@RunWith(MockitoJUnitRunner.class)
public class GameQueriesTest {
	@Mock
	private Map<String, TwoTeamsGame> currentGames;
	
	@Mock
	private GameCreator gameFactory;
	
	@Mock
	private Semaphore lock;
	
	@InjectMocks
	private GameQueries gameQueries = new InMemoryTwoTeamsGameStore(gameFactory);
	
	@Test
	public void checkGetGameSummariesWhenMoreThanOneExistsReturnsThemInlastUpdatedOrder() throws InterruptedException {
		when(currentGames.values()).thenReturn(asList(aDelayedCreatedFootBallGame("a", "b"), aDelayedCreatedFootBallGame("c", "d")));
		
		assertThat(gameQueries.getGameSummaries(), contains(toString("c", 0, "d", 0), toString("a", 0, "b", 0)));
		
		verify(currentGames).values();
		verify(lock).acquire();
		verify(lock).release();
		verifyNoMoreInteractions(lock, gameFactory, currentGames);
	}
	
	@Test
	public void checkGetGameSummariesWhenThereAreNoExistingGamesReturnsEmptySummaryList() throws InterruptedException {
		when(currentGames.values()).thenReturn(emptyList());
		
		assertThat(gameQueries.getGameSummaries(), empty());
		
		verify(currentGames).values();
		verify(lock).acquire();
		verify(lock).release();
		verifyNoMoreInteractions(lock, gameFactory, currentGames);
	}
	
	private FootballGame aDelayedCreatedFootBallGame(String homeTeamId, String awayTeamId) throws InterruptedException {
		sleep(1000);
		
		return new FootballGame(new Team(homeTeamId), new Team(awayTeamId));
	}
	
	private String toString(String homeTeam, int homeTeamScore, String awayTeam, int awayTeamScore) {
		StringBuilder game = new StringBuilder();
		
		game.append("Game [homeTeam=");
		game.append("Team [teamName=" + homeTeam + "]");
		game.append(", homeTeamScore=");
		game.append(homeTeamScore);
		game.append(", awayTeam=");
		game.append("Team [teamName=" + awayTeam + "]");
		game.append(", awayTeamScore=");
		game.append(awayTeamScore);
		game.append("]");
		
		return game.toString();
	}
}
