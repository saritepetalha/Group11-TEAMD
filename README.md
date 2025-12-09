<div align="center">

# 🏰 KU Tower Defense 🎮

<img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/java/java-original.svg" alt="Java" width="100" height="100"/>

### ⚔️ Defend Your Kingdom with Strategy and Skill! ⚔️

*A feature-rich tower defense game built with Java Swing*

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Java%20Swing-orange?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Completed-success?style=for-the-badge)

</div>

---

## 📖 Overview

🎯 **KU Tower Defense** is a comprehensive tower defense game that challenges players to strategically place and upgrade towers to defend against waves of enemies. The game features a custom-built level editor, save/load system, statistics tracking, and various gameplay enhancements including weather effects, skill trees, and exciting power-ups!

🖥️ Built entirely using **Java Swing** for the GUI, providing smooth graphics rendering with **Java AWT Graphics2D** for custom game visuals.

## 📸 Screenshots

<div align="center">

### 🎮 Main Menu
<!-- Add your main menu screenshot here -->
![Main Menu](path/to/screenshot1.png)

### ⚔️ Gameplay
<!-- Add your gameplay screenshot here -->
![Gameplay](path/to/screenshot2.png)

### 🗺️ Level Editor
<!-- Add your level editor screenshot here -->
![Level Editor](path/to/screenshot3.png)

### 🏆 Victory Screen
<!-- Add your victory screenshot here -->
![Victory](path/to/screenshot4.png)

</div>

---

## ✨ Features

### 🎯 Core Gameplay
- 🗼 **Multiple Tower Types**: Archer, Mage, Artillery, and Poison towers, each with unique abilities and attack patterns
- ⬆️ **Tower Upgrades**: Enhance your towers using the Decorator pattern for added functionality and power
- 👹 **Diverse Enemies**: Face different enemy types including Goblins, Knights, Trolls, Barrels, and TNT units
- 🌊 **Wave System**: Progressive difficulty with configurable wave patterns that keep you on your toes
- 🛡️ **Warrior Units**: Deploy special warrior units (Archer Warriors, Wizard Warriors, TNT Warriors) for additional defense

### 🎮 Game Modes
- 🗺️ **Level Selection**: Play through pre-designed challenging levels
- 🎨 **Custom Levels**: Create and play custom maps using the built-in level editor
- ✏️ **Map Editor**: Full-featured level editor with save/load functionality
- 💾 **Saved Games**: Load previously saved game states and continue your defense

### 💎 Power-Ups & Resources
- ⚡ **Ultimate Abilities**: Unleash devastating ultimate powers to turn the tide of battle
- 💰 **Gold Collection**: Earn gold by defeating enemies and collect gold bags that spawn during gameplay
- ⛏️ **Mining System**: Mine resources from stone deposits for additional income
- 🌲 **Tree Interactions**: Interact with environmental elements for strategic advantages
- 🎄 **Seasonal Effects**: Experience dynamic weather and snow transitions

### 🎨 Advanced Features
- 💾 **Save/Load System**: Save your progress and resume later with full game state preservation
- 📊 **Statistics Tracking**: Track your performance across games with detailed stats
- 🌳 **Skill Tree**: Unlock and upgrade abilities through a comprehensive skill system
- 🌦️ **Weather Effects**: Dynamic weather system affecting gameplay and visuals
- 🔊 **Audio System**: Immersive background music and sound effects
- 🖥️ **Fullscreen Support**: Play in windowed or fullscreen mode for optimal experience

### 🔧 Technical Highlights
- 🏗️ **MVC Architecture**: Clean separation of concerns with Models, Views, and Controllers
- 🎨 **Design Patterns**: Implements Observer, Decorator, Strategy, Memento, and Factory patterns
- 🧭 **Pathfinding System**: Custom A* pathfinding algorithm for intelligent enemy movement
- 💰 **Resource Management**: Complete mining and economy system with gold generation
- 🎬 **Animation System**: Smooth sprite animations and visual effects

## 📁 Project Structure

```
KUTowerDefense/
├── 📂 src/
│   ├── ⚙️ config/          # Game configuration (enemy stats, tower stats, waves)
│   ├── 📌 constants/       # Game constants and dimensions
│   ├── 🎮 controllers/     # Game logic controllers (MVC)
│   ├── 👹 enemies/         # Enemy types and behaviors
│   ├── 🛠️ helpMethods/     # Utility classes for I/O and rendering
│   ├── 🖱️ inputs/          # Keyboard and mouse input handlers
│   ├── 📊 managers/        # Game systems management
│   ├── 📦 models/          # Data models (MVC)
│   ├── 🎯 objects/         # Game objects (towers, projectiles, tiles)
│   ├── 🧭 pathfinding/     # Enemy pathfinding algorithms
│   ├── 🎬 scenes/          # Game screens (menu, playing, editing)
│   ├── 🌳 skills/          # Skill tree implementation
│   ├── 🎯 strategies/      # Strategy pattern implementations
│   ├── 🖼️ ui_p/            # UI components
│   └── 👁️ views/           # View layer (MVC)
├── 📂 resources/
│   ├── 🔊 Audio/           # Sound effects and music
│   ├── 👾 EnemyAssets/     # Enemy sprites and animations
│   ├── 🗼 TowerAssets/     # Tower sprites and animations
│   ├── 🗺️ Levels/          # Level data files
│   └── 🎨 UI/              # UI graphics and elements
└── 📚 lib/
    └── gson-2.9.0.jar      # JSON library for data serialization
```

## 🛠️ Technologies & Tools Used

<div align="center">

| Technology | Purpose | Badge |
|------------|---------|-------|
| ☕ **Java** | Primary programming language | ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white) |
| 🖼️ **Java Swing** | GUI Framework for windows and components | ![Swing](https://img.shields.io/badge/Swing-GUI-orange?style=flat-square) |
| 🎨 **Java AWT** | Graphics2D for custom rendering and animations | ![AWT](https://img.shields.io/badge/AWT-Graphics-blue?style=flat-square) |
| 📝 **Gson** | JSON serialization for save/load functionality | ![Gson](https://img.shields.io/badge/Gson-2.9.0-green?style=flat-square) |
| 💡 **IntelliJ IDEA** | Integrated Development Environment | ![IntelliJ](https://img.shields.io/badge/IntelliJ-IDE-purple?style=flat-square) |

</div>

## 🚀 How to Run

### 🎮 Option 1: Download Pre-built Executable (Easiest!)

**Perfect for players who just want to enjoy the game!**

#### 📋 Prerequisites
- 🪟 Windows OS
- ☕ Java Runtime Environment (JRE) 17 or higher
- 🖱️ Mouse for gameplay controls

#### ▶️ Quick Start

1️⃣ **Download the executable:**
- Go to the [Releases](../../releases) page
- Download the latest `KUTowerDefense.exe` from the release assets
- Extract to a folder on your computer

2️⃣ **Run the game:**
- Double-click `KUTowerDefense.exe` to launch
- Or run from command line: `KUTowerDefense.exe`

3️⃣ **Play and enjoy!** 🎮

> 💡 **Note**: The executable is packaged with Launch4j and includes all necessary resources. Make sure you have Java 17+ installed on your system!

---

### 💻 Option 2: Run from Source Code (For Developers)

**For developers who want to explore or modify the code**

#### 📋 Prerequisites
- ☕ Java Development Kit (JDK) 17 or higher
- 💻 IntelliJ IDEA (recommended) or any Java IDE
- 🖱️ Mouse for gameplay controls

#### ▶️ Running from Source

1️⃣ **Clone the repository:**
```bash
git clone <repository-url>
cd Group11-TEAMD/KUTowerDefense
```

2️⃣ **Open the project in your IDE:**
- For IntelliJ IDEA: Open the `KUTowerDefense` folder as a project
- Wait for the project to index

3️⃣ **Ensure dependencies are loaded:**
- Verify that `lib/gson-2.9.0.jar` is added to the classpath
- IntelliJ should automatically detect the library

4️⃣ **Run the main class:**
- Navigate to `src/main/Game.java`
- Right-click and select "Run 'Game.main()'"
- Or use the IDE's run button

5️⃣ **Start playing and developing!** 🎮💻

#### 🔨 Building Your Own JAR (Optional)

If you want to create your own executable JAR file:

**Using IntelliJ IDEA:**
1. Go to `File` → `Project Structure` → `Artifacts`
2. Click `+` → `JAR` → `From modules with dependencies`
3. Select `Game` as the main class
4. Click OK and Apply
5. Go to `Build` → `Build Artifacts` → `Build`
6. Find your JAR in `out/artifacts/`

**Using Command Line:**
```bash
# Compile all Java files
javac -cp lib/gson-2.9.0.jar -d out src/**/*.java

# Create JAR file
jar cfm KUTowerDefense.jar manifest.txt -C out . -C resources .
```

> 📦 **Note**: The JAR and EXE files are not included in this repository due to their large size. Download them from Releases or build from source.

## 🎮 Gameplay Instructions

### 🕹️ Basic Controls
- 🖱️ **Mouse**: Select and place towers, interact with UI elements
- 👆 **Left Click**: Place towers on valid tiles
- 🎯 **Right Click**: Cancel selection or deselect
- 🗺️ **Menu Navigation**: Use mouse to navigate through menus and buttons

### 🗼 Tower Placement Strategy
1️⃣ Select a tower type from the tower menu at the bottom
2️⃣ Click on a valid grass tile to place the tower
3️⃣ Ensure the path remains clear for enemies to maintain valid routes
4️⃣ Position towers strategically to maximize coverage

### ⬆️ Tower Upgrades
- 🔍 Click on an existing tower to view upgrade options
- 💰 Spend gold to upgrade towers with enhanced abilities
- ⚡ Upgraded towers have increased damage, range, or special effects
- 🎨 Tower decorators can add multiple enhancements

### 💰 Resources & Economy
- 💵 **Gold**: Earned by defeating enemies, collecting gold bags, and mining
- ❤️ **Lives**: Lost when enemies reach the end of the path - don't let them through!
- 🌊 **Waves**: Survive all waves to complete the level
- ⛏️ **Mining**: Click on stone deposits to mine for additional gold
- 🌲 **Trees**: Interact with trees for special bonuses

### ⚡ Power-Ups
- 🎯 **Ultimate Abilities**: Charge up and unleash powerful area-of-effect attacks
- 💼 **Gold Bags**: Collect golden treasure bags that appear during gameplay
- 🎄 **Seasonal Bonuses**: Take advantage of weather effects and seasonal power-ups

## 🎨 Design Patterns Implemented

Our project showcases professional software architecture using industry-standard design patterns:

<div align="center">

| Pattern | Implementation | Purpose |
|---------|---------------|---------|
| 👁️ **Observer** | Map change notifications | Real-time event handling and updates |
| 🎁 **Decorator** | Tower upgrades | Dynamic ability enhancements |
| 🎯 **Strategy** | Level selection & combat | Flexible behavior algorithms |
| 🏭 **Factory** | Object creation | Centralized instantiation logic |
| 💾 **Memento** | Save/Load system | Game state preservation |
| 🏗️ **MVC** | Architecture | Clean code separation |

</div>

---

## 👥 Development Team

<div align="center">

### 🎓 Team D - Group 11

*A passionate team of software engineering students dedicated to creating an engaging gaming experience*

</div>

---

## 📚 Documentation

Additional documentation and design artifacts can be found in:

- 📊 **`reports/D1/`**: Comprehensive design diagrams and class structures
  - Class diagrams
  - Sequence diagrams
  - Communication diagrams
  - Package diagrams

- 🎨 **`reports/R2M2D2/`**: Decorator pattern implementation details and new features

- 📖 **`UP Artifacts/`**: Use cases and operation contracts

---

## 🎯 Game Features Showcase

### 🗼 Tower Types
| Tower | Specialty | Best Against |
|-------|-----------|--------------|
| 🏹 Archer | Fast firing rate | Light enemies |
| 🔮 Mage | Area damage | Grouped enemies |
| 💣 Artillery | Heavy damage | Tanky enemies |
| ☠️ Poison | Damage over time | All enemy types |

### 👹 Enemy Types
- 👺 **Goblin**: Fast and numerous
- ⚔️ **Knight**: Armored and tough
- 🧌 **Troll**: Slow but powerful
- 🛢️ **Barrel**: Explosive surprise
- 💣 **TNT**: High risk, high reward

---

## 📜 License

This project was developed as part of a software engineering course at Koç University.

## 🙏 Acknowledgments

- 🎮 Game assets and sprites used for educational purposes
- 👨‍🏫 Course instructors and teaching assistants for their invaluable guidance
- 💡 Open source community for inspiration and resources

---

<div align="center">

### 🌟 Star this repository if you enjoyed the game! 🌟

**Made with ☕ and ❤️ by Team D**

</div>
