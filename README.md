# Safari Olds

Safari Olds is a **Java Swing safari management game** where the player builds and manages a wildlife park on a tile-based map. Players can buy and place roads, ponds, trees, herbivores, and carnivores, deploy jeeps and rangers, and manage the park economy while dealing with threats like poachers.

## Features

- Full-screen Java Swing interface
- Main menu, start screen, settings screen, and difficulty screen
- Tile-based safari map system
- Buy/sell shop for roads, trees, ponds, herbivores, carnivores, jeeps, and animal chips
- Wildlife simulation with moving animals and basic interactions
- Ranger and poacher gameplay mechanics
- Day/night visibility mode
- Adjustable game speed modes
- In-game counters for money, roads, animals, plants, and ponds
- Asset-based visuals for map tiles and game objects

## Project Structure

```text
safariolds/
├── controller/
├── model/
└── view/
    └── assets/
```

The code follows a simple MVC-style structure:

- `controller/` – game flow, map logic, state, and sound handling
- `model/` – animals, roads, rangers, poachers, settings, and gameplay entities
- `view/` – menu, start screen, shop, and UI assets

## Tech Stack

- Java
- Java Swing / AWT

## How to Run

Keep the `safariolds/` folder as a package directory at the root of the project.

Compile from the folder **above** `safariolds/`:

```bash
javac safariolds/controller/Safariolds.java
```

Run the game with:

```bash
java safariolds.controller.Safariolds
```

## Notes

- This project includes both source files (`.java`) and compiled class files (`.class`).
- Before pushing to GitHub, it is a good idea to remove compiled `.class` files and other system files like `desktop.ini`.
- The project references background music at `resources/safari_olds_sound.wav`. If that file is missing, the game may still start, but audio will not play.
- Some menu items such as `LOAD` and parts of the difficulty flow appear to be incomplete or still under development.

## Why this project is interesting

Safari Olds combines UI programming, game state management, object movement, and simulation-style mechanics in a desktop Java project. It is a good showcase of Swing-based game development and event-driven programming.

## Future Improvements

- Add a proper save/load system
- Fully connect difficulty settings to gameplay
- Add scoring, win/lose conditions, and progression
- Improve balancing of animals, rangers, and poachers
- Package the game with a runnable JAR
- Add screenshots and gameplay preview GIFs to this README

## Author

Created as a Java desktop game project focused on safari park management and simulation.
