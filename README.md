# Safari Olds

Safari Olds is a Java Swing safari management game where the player builds a wildlife park, manages resources, places roads and habitat items, and protects animals from threats while growing the park's capital.

The project combines a desktop game interface with simulation-style mechanics such as animal placement, road building, ranger protection, jeep tours, poacher encounters, and day/night gameplay changes.

## Project Highlights

- Full-screen Java Swing game interface
- Start menu, settings screen, and difficulty screen
- 20 x 40 tile-based safari map
- In-game shop for buying and selling park items
- Placeable objects including roads, trees, ponds, herbivores, and carnivores
- Jeep tour system that generates park income
- Rangers that help protect the reserve
- Poacher spawning and wildlife protection mechanics
- Night mode and animal chip/tag interaction
- Game timer, minimap, and money tracking
- JUnit test suite with JaCoCo coverage support through Ant

## Gameplay Overview

The player starts the game by entering a name and launching the main map. From there, the goal is to grow the safari park and increase available money through smarter park management.

### Main gameplay elements

- **Starting capital:** The game logic currently starts the player with **€100,000**.
- **Win condition:** Reach **€150,000**.
- **Lose condition:** Drop to **€0 or below**.
- **Road building:** Roads begin from the starting point and must connect correctly across the map.
- **Jeep tours:** Jeeps travel along valid road paths and increase capital when tours complete.
- **Wildlife and habitat:** Players can buy and place herbivores, carnivores, trees, and ponds.
- **Protection system:** Rangers can be placed to protect animals and respond to poachers.
- **Night mode:** Includes special visibility behavior and chip-based animal tagging.

## Shop Items

The in-game shop currently supports buying and selling the following items:

- Jeep
- Road
- Tree
- Herbivore
- Carnivore
- Pond
- Chipanimal (buy only)

## Tech Stack

- **Language:** Java
- **UI:** Java Swing / AWT
- **Build tool:** Apache Ant
- **Testing:** JUnit 4
- **Coverage:** JaCoCo
- **IDE support:** NetBeans project files included

## Project Structure

```text
safari-olds-master/
├── src/
│   └── safariolds/
│       ├── controller/
│       ├── model/
│       └── view/
├── test/
│   └── safariolds/
├── resources/
├── lib/
├── build.xml
└── nbproject/
```

### Package overview

- `safariolds.controller` - game startup, map control, timer/state logic, sound handling, minimap logic
- `safariolds.model` - gameplay entities such as animals, roads, jeep, ranger, poacher, settings, and difficulty
- `safariolds.view` - menu screens, start screen, and shop UI

## Requirements

- **Java 21**
- **Apache Ant** installed and available in your terminal

The NetBeans project settings in this repository are configured with:

- `javac.source=21`
- `javac.target=21`

## How to Build

From the project root, run:

```bash
ant build-and-test
```

This will:

- clean old build output
- compile the source code
- compile the test code
- run the JUnit test suite
- generate JaCoCo coverage output

Coverage reports are written to:

```text
build/coverage/html/
```

## How to Run

### Option 1: Run from NetBeans

Open the project in NetBeans and run the main class:

```text
safariolds.controller.Safariolds
```

### Option 2: Run from the terminal

First compile the project, then run:

```bash
java -cp build/classes safariolds.controller.Safariolds
```

Run this command from the project root so the game can access the `resources/` folder correctly.

## Main Entry Point

```text
safariolds.controller.Safariolds
```

This class starts the background music and opens the main menu.

## Features by Screen

### Main Menu

- Start Game
- Difficulty
- Settings
- Load
- Exit

### Start Game Screen

- player name input
- play button
- back navigation

### Settings

- game speed options: Hour, Day, Week
- volume control
- saved volume settings through `settings.properties`

### Main Map

- money display
- roads remaining
- counters for carnivores, herbivores, plants, and ponds
- timer display
- minimap
- shop button
- ranger placement button
- night mode toggle

## Testing

The repository includes unit tests for major parts of the project, including:

- controller classes
- model classes
- view classes

Example test areas:

- `GameMapTest`
- `AnimalTest`
- `RoadTest`
- `PoacherTest`
- `SettingsTest`
- `ShopTest`

## Assets

The project includes built-in visual and audio assets such as:

- terrain backgrounds
- animals
- jeep and tourist sprites
- ranger and poacher images
- pond and tree assets
- background sound file

## Notes

- This repository currently includes generated `.class` files inside source-related folders and build output folders.
- For a cleaner GitHub repository, you may want to keep only source files, assets, tests, and build configuration.
- The existing `.gitignore` is minimal and can be expanded if you want a cleaner Java project structure in GitHub.

## Future Improvements

Possible improvements for the project:

- add save/load functionality
- improve difficulty mode behavior
- package the game as a runnable JAR
- add screenshots and gameplay preview to the repository
- improve input validation and test stability for GUI components
- clean compiled files from source directories

## Author

Developed as a Java safari simulation / management game project.
