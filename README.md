# Aegis Grid
<p align="center">
  <img src="assets/gamedemo.gif" alt="Tower Defense Gameplay" width="600"/>
</p>

A custom-built, wave-based Tower Defense engine developed from scratch using **Java** and the **LibGDX** framework.

This project was built to explore Object-Oriented Programming (OOP) principles, game state management, and complex pathfinding algorithms in a real-time, grid-based environment.


## Key Technical Features

* **Dynamic A(*) Pathfinding:** Enemies utilize a custom implementation of the A* search algorithm to dynamically calculate the shortest path to the player's base. The pathing recalculates in real-time whenever a player places or sells a wall.
* **Anti-Maze Trapping (Breach Logic):** To prevent players from simply boxing in the base for an instant win, the pathfinding system includes a secondary fallback heuristic. If a valid open path is blocked, enemies calculate a "breach route" and will actively attack and destroy player-placed barricades to forge a new path.
* **Vector Homing Projectiles:** Towers scan for the nearest target using Euclidean distance calculations. Once fired, projectiles use vector math to continually update their trajectory, homing in on moving targets.
* **Trigonometric Rotations:** Defensive towers use `Math.atan2` to calculate the exact degree of rotation required to face their current target smoothly.
* **Game State Machine:** Clean separation of logic using Enums (`PREP`, `DEFEND`, `GAMEOVER`), completely pausing entity updates during the building/economy phase.

## Technologies & Concepts
* **Language:** Java
* **Framework:** LibGDX (Desktop)
* **Architecture:** Object-Oriented Programming (OOP)
* **Algorithms:** A* Pathfinding, Euclidean Distance Tracking, 2D Vector Math
* **Rendering:** Orthographic Camera scaling, SpriteBatching, and ShapeRenderer for dynamic UI (health bars, selection boundaries).

## Gameplay Mechanics
1. **Prep Phase:** Use your starting bank to build a maze using **Walls ($10)** and place offensive **Towers ($50)**.
2. **Defend Phase:** Survive waves of enemies that spawn at the perimeter.
3. **Economy:** Earn money for every enemy destroyed to expand your defenses in the next Prep Phase.
4. **Destructible Environment:** Enemies will bash through your walls if you don't leave them a clear path to the core!

## How to Run Locally
1. Ensure you have the [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) installed.
2. Clone this repository to your local machine.
3. Import the project into your preferred IDE (IntelliJ IDEA or Eclipse) as a Gradle project.
4. Run the `Lwjgl3auncher.java` file located in the `lwjgl3/src/` directory.

## Assets
All pixel art assets (seamless ground terrain, mechanical walls, animated enemy sprite sheets, and structures) were custom-generated and scaled for this project's 64x64 grid architecture.

---
**Author:** [Zaki Amin Ahmad](https://www.linkedin.com/in/zakiamin/)
