package com.scoreboard;

import static com.scoreboard.Sports.FOOTBALL;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.scoreboard.store.FootballGame;
import com.scoreboard.store.InMemoryTwoTeamsGameStore;
import com.scoreboard.store.Team;

@RunWith(MockitoJUnitRunner.class)  
public class GameManagementTest {
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	
	@Mock
	private Map<String, TwoTeamsGame> currentGames;
	
	@Mock
	private GameCreator gameFactory;
	
	@Mock
	private Semaphore lock;
	
	@InjectMocks
	private GameManagement gameManagement = new InMemoryTwoTeamsGameStore(gameFactory);
	
	@Test
	public void checkCreateGameWhenNoneOfTheTeamsArePlayingCreatesANewGame() throws InterruptedException {
		when(gameFactory.createGame("a", "b", FOOTBALL)).thenReturn(aFootBallGame("a", "b"));
		
		gameManagement.createGame("a", "b", FOOTBALL);
		
		verify(gameFactory).createGame("a", "b", FOOTBALL);
		verify(currentGames, times(2)).keySet();
		verify(currentGames).put("a-b", aFootBallGame("a", "b"));
		verify(lock).acquire();
		verify(lock).release();
		verifyNoMoreInteractions(lock, gameFactory, currentGames);
	}

	@Test
	public void checkCreateGameWhenOneOfTheTeamsAreAlreadyPlayingDoesNotCreateANewGame() throws InterruptedException {
		when(currentGames.keySet()).thenReturn(new HashSet<>(asList("m-a"))); //This would be from a: m-a -> new FootballGame(new Team("m"), new Team("a"));
		
		gameManagement.createGame("a", "b", FOOTBALL);
		
		verify(currentGames).keySet();
		verify(lock).acquire();
		verify(lock).release();
		verifyNoMoreInteractions(lock, gameFactory, currentGames);
	}
	
	@Test
	public void checkUpdateExistingGameScoreCallsExistingGameUpdateScore() throws InterruptedException {
		FootballGame game = mock(FootballGame.class);
		
		when(currentGames.containsKey("a-b")).thenReturn(true);
		when(currentGames.get("a-b")).thenReturn(game);
		
		gameManagement.updateGameScore("a", 1, "b", 0);
		
		verify(currentGames).containsKey("a-b");
		verify(currentGames).get("a-b");
		verify(game).updateScores(1, 0);
		verify(lock).acquire();
		verify(lock).release();
		verifyNoMoreInteractions(lock, gameFactory, currentGames, game);
	}
	
	@Test
	public void checkUpdateNonExistingGameScoreReturnsAnException() throws InterruptedException {
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("The game with id: a-b does not exist");
		
		when(currentGames.containsKey("a-b")).thenReturn(false);
		
		gameManagement.updateGameScore("a", 1, "b", 0);
		
		verify(currentGames).containsKey("a-b");
		verify(lock).acquire();
		verify(lock).release();
		verifyNoMoreInteractions(lock, gameFactory, currentGames);
	}
	
	@Test
	public void checkFinishExistingGameRemovesTheGame() throws InterruptedException {
		when(currentGames.remove("a-b")).thenReturn(aFootBallGame("a", "b"));
		
		gameManagement.finishGame("a", "b");
		
		verify(currentGames).remove("a-b");
		verify(lock).acquire();
		verify(lock).release();
		verifyNoMoreInteractions(lock, gameFactory, currentGames);
	}
	
	@Test
	public void checkFinishNonExistingGameReturnsAnException() throws InterruptedException {
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("The game with id: a-b does not exist");
		
		when(currentGames.remove("a-b")).thenReturn(null);
		
		gameManagement.finishGame("a", "b");
		
		verify(currentGames).remove("a-b");
		verify(lock).acquire();
		verify(lock).release();
		verifyNoMoreInteractions(lock, gameFactory, currentGames);
	}
	
	private FootballGame aFootBallGame(String homeTeamId, String awayTeamId) {
		return new FootballGame(new Team(homeTeamId), new Team(awayTeamId));
	}
}
