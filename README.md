
# Notes

I have created two main interfaces: **GameManagement** and **GameQueries** which will cover the exercise. I have done it this way to follow the SOLID Interface Segregation principle, so it could be different clients having different roles. 

A new instance of **InMemoryTwoTeamsGameStore** is all is needed as the entrypoint to use the code. 

There is a **GameCreator** which is a factory to create different types of two team games, at the moment just FootBall. An implementation as a Singleton has been created with **TwoTeamGameSportsFactory**.

I have abstracted the fact that we will have "entities" being them: **TwoTeamsGame**. Then an implementation with a **FootBallGame** has been created which contains the rules applicable for valid scoring.

A store has been implemented: **InMemoryTwoTeamsGameStore**, it will check that no games can be created unless non of the teams of the game being created is already playing, then for each game it will delegate the update score to the corresponding **TwoTeamsGame**. Note how the store could handle different sports with no change (another SOLID principle Open-Close). Also the Dependency-Injection is used to pass the factory to create new games.


# Doubts

I'm not sure exactly that I understand the getSummaries ordering from the exercise, so I have implemented an ordering based on lastUpdated of the game.