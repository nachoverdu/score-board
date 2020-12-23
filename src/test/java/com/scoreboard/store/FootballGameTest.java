package com.scoreboard.store;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.isNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class) 
public class FootballGameTest {
	@Rule
	public ExpectedException expectedEx = none();
	
	@Test
	public void checkCreateANewGameWithTwoValidTeamsCreatesANewGameWithInitialScoresSetToZero() {
		assertThat(new FootballGame(new Team("a"), new Team("b")), 
				  hasProperty("gameInfo", is(toString("a", 0, "b", 0))));
	}
	
	@Test
	public void checkCreateANewGameWithTwoEqualTeamsDoesNotCreateAGameAndReturnsAnException() {
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("Can't create a football game with both same contenders");
		
		assertThat(new FootballGame(new Team("a"), new Team("a")), isNull());
	}
	
	@Test
	public void checkUpdateScoresWhenHomeTeamScoresANewGoalUpdatesTheScoreOk() {
		FootballGame game = new FootballGame(new Team("a"), new Team("b"));
		
		game.updateScores(1, 0);
		
		assertThat(game, hasProperty("gameInfo", is(toString("a", 1, "b", 0))));
	}
	
	@Test
	public void checkUpdateScoresWithSameScoreDoesNotChangeTheScore() {
		FootballGame game = new FootballGame(new Team("a"), new Team("b"));
		
		game.updateScores(0, 0);
		
		assertThat(game, hasProperty("gameInfo", is(toString("a", 0, "b", 0))));
	}
	
	@Test
	public void checkUpdateScoresWhenHomeTeamScoresIsNegativeReturnsAnExceptionAndDoesNotUpdateTheScore() {
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("Error scores can't be negative, newHomeTeamScore: -1 newAwayTeamScore: 0");
		
		FootballGame game = new FootballGame(new Team("a"), new Team("b"));
		
		game.updateScores(-1, 0);
		
		assertThat(game, hasProperty("gameInfo", is(toString("a", 0, "b", 0))));
	}
	
	@Test
	public void checkUpdateScoresWhenHomeTeamHasACancelledGoalFromAPreviousValidGoalUpdatesTheScoreOk() {
		FootballGame game = new FootballGame(new Team("a"), new Team("b"));
		
		game.updateScores(1, 0); //goal is valid
		game.updateScores(0, 0); //cancel the goal
		
		assertThat(game, hasProperty("gameInfo", is(toString("a", 0, "b", 0))));
	}
	
	@Test
	public void checkUpdateScoresPassingAnInvalidPreviousHomeTeamScoreReturnsAnExceptionAndDoesNotUpdateTheScore() {
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("Only one team score can be updated at the same time");
		
		FootballGame game = new FootballGame(new Team("a"), new Team("b"));
		
		game.updateScores(1, 0); //home team goal is valid
		game.updateScores(0, 1); //home team score is invalid though away team goal is valid
		
		assertThat(game, hasProperty("gameInfo", is(toString("a", 1, "b", 0))));
	}
	
	@Test
	public void checkUpdateScoresWhenAwayTeamScoresANewGoalUpdatesTheScoreOk() {
		FootballGame game = new FootballGame(new Team("a"), new Team("b"));
		
		game.updateScores(0, 1);
		
		assertThat(game, hasProperty("gameInfo", is(toString("a", 0, "b", 1))));
	}
	
	@Test
	public void checkUpdateScoresWhenAwayTeamScoresIsNegativeReturnsAnExceptionAndDoesNotUpdateTheScore() {
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("Error scores can't be negative, newHomeTeamScore: 0 newAwayTeamScore: -1");
		
		FootballGame game = new FootballGame(new Team("a"), new Team("b"));
		
		game.updateScores(0, -1);
		
		assertThat(game, hasProperty("gameInfo", is(toString("a", 0, "b", 0))));
	}
	
	@Test
	public void checkUpdateScoresWhenAwayTeamHasACancelledGoalFromAPreviousValidGoalUpdatesTheScoreOk() {
		FootballGame game = new FootballGame(new Team("a"), new Team("b"));
		
		game.updateScores(0, 1); //goal is valid
		game.updateScores(0, 0); //cancel the goal
		
		assertThat(game, hasProperty("gameInfo", is(toString("a", 0, "b", 0))));
	}
	
	@Test
	public void checkUpdateScoresPassingAnInvalidPreviousAwayTeamScoreReturnsAnExceptionAndDoesNotUpdateTheScore() {
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("Only one team score can be updated at the same time");
		
		FootballGame game = new FootballGame(new Team("a"), new Team("b"));
		
		game.updateScores(0, 1); //away team goal is valid
		game.updateScores(1, 0); //away team score is invalid though home team goal is valid
		
		assertThat(game, hasProperty("gameInfo", is(toString("a", 0, "b", 1))));
	}
	
	@Test
	public void checkUpdateScoresWithBothTeamsScoringAtTheSameTimeReturnsAnExceptionAndDoesNotUpdateTheScore() {
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("Invalid score, both teams can't score at the same time");
		
		FootballGame game = new FootballGame(new Team("a"), new Team("b"));
		
		game.updateScores(1, 1);
		
		assertThat(game, hasProperty("gameInfo", is(toString("a", 0, "b", 0))));
	}
	
	@Test
	public void checkUpdateScoresWithBothTeamsHavingACancelledGoalAtTheSameTimeReturnsAnExceptionAndDoesNotUpdateTheScore() {
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("Invalid score, both teams can't have a cancelled goal at the same time");
		
		FootballGame game = new FootballGame(new Team("a"), new Team("b"));
		
		game.updateScores(1, 0); //goal is valid
		game.updateScores(1, 1); //goal is valid
		game.updateScores(0, 0); //cancel both goals at the same time
		
		assertThat(game, hasProperty("gameInfo", is(toString("a", 1, "b", 1))));
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
