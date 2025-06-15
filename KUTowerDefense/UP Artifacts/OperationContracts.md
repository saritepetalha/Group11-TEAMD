# Updated Operation Contracts for KU Tower Defense Game

## Updated Existing Contracts

### Contract CO1-Updated: saveGameState
**Operation:** `saveGameState(filename: String)`  
**Cross References:** Use Cases: Save Game State  
**Preconditions:**
- Game is in a valid state for saving
- Player has initiated save operation
- Game is not in transition state (between waves, during animations)

**Postconditions:**
- Complete game state saved including: player stats (health, shield, gold), wave progress, tower states with conditions and targeting strategies
- Weather conditions and seasonal progression preserved
- Skill tree progress and selected skills saved
- Environmental objects state (DeadTrees, LiveTrees, MineableStones) preserved
- Wave start states preserved for restart capability
- Save file created with timestamp and metadata

**Constraints:**
- Save file must be valid and not corrupted
- All manager states must be serializable
- File system must be accessible for writing

---

### Contract CO2: clickNewGame
**Operation:** `clickNewGame()`  
**Cross References:** Use Cases: Start New Game  
**Preconditions:**
- Player is in the main menu and wants to start a new game
- Game system is initialized

**Postconditions:**
- System displayed the list of available maps for the player to select
- Game marked as new game (not loaded from save)
- Default game options loaded

**Constraints:**
- System must have valid map files stored and accessible
- Default game configuration must be available

---

### Contract CO3: selectMap
**Operation:** `selectMap(mapFileName: String, difficulty: String)`  
**Cross References:** Use Cases: Start New Game  
**Preconditions:**
- Player has selected a map from the list
- Difficulty level has been chosen
- System has received the map file for validation

**Postconditions:**
- System validated the selected map file structure
- If valid: map data loaded, game options applied based on difficulty, game screen initialized with weather system
- If invalid: error shown, player prompted to select another map
- Game statistics initialized (enemies spawned, defeated, etc.)

**Constraints:**
- Map file must contain valid path data and tile information
- Difficulty settings must exist in game options
- Map must be compatible with current game version

---

### Contract CO4: loadMapData
**Operation:** `loadMapData(mapFileName: String, gameOptions: GameOptions)`  
**Cross References:** Use Cases: Start New Game, Load Game  
**Preconditions:**
- Valid map file has been selected
- Game options are available for the selected difficulty

**Postconditions:**
- Map data (layout, tiles, paths, environmental objects) loaded successfully
- Environmental objects (DeadTrees, LiveTrees, MineableStones) positioned
- Weather system initialized with seasonal progression
- Enemy and tower stats configured based on difficulty

**Constraints:**
- Map file must be correctly formatted with all necessary data
- Environmental objects must have valid positions
- Path waypoints must form valid enemy routes

---

### Contract CO5-Updated: placeTower
**Operation:** `placeTower(towerType: TowerType, position: Point, targetingStrategy: TargetingStrategy)`  
**Cross References:** Use Cases: Place Tower  
**Preconditions:**
- Player has selected a tower type
- Valid placement location (DeadTree or valid tile)
- Sufficient gold for tower cost (based on game options)
- Position is not occupied by existing tower

**Postconditions:**
- Tower created with initial condition (100%), usage count (0)
- Targeting strategy assigned to tower
- Environmental object (DeadTree) removed if applicable
- Tile data updated to reflect tower placement
- Player gold reduced by tower cost
- Tower added to warrior spawning capability if applicable

**Constraints:**
- Tower must be placeable at the specified location
- Tower cost must be correctly calculated from game options
- Placement must not block enemy paths

---

### Contract CO6-Updated: updateGameOptions
**Operation:** `updateGameOptions(gameOptions: GameOptions)`  
**Cross References:** Use Cases: Set Game Parameters  
**Preconditions:**
- Player has modified game parameters
- New options are valid and within acceptable ranges

**Postconditions:**
- Enemy stats updated for all existing and future enemies
- Tower stats and costs refreshed
- Weather system enabled/disabled based on settings
- Starting resources (gold, health, shield) modified
- Difficulty level effects applied

**Constraints:**
- All parameter values must be within valid ranges
- Changes must not break existing game state
- Options must be persistable to file system

---

### Contract CO7: clickNewGame
*(Same as CO2 - appears to be duplicate)*

---

### Contract CO8: triggerWaveStart
**Operation:** `triggerWaveStart(waveNumber: int)`  
**Cross References:** Use Cases: Start Enemy Wave  
**Preconditions:**
- Game has reached the point where a new wave should start
- Previous wave is completed or this is the first wave
- Game is not paused

**Postconditions:**
- Wave start states captured for potential restart
- Enemy groups prepared for spawning
- Wave timer and counters initialized
- Player notified of wave start

**Constraints:**
- Wave data must be available and valid
- Game must not be in paused state
- All previous wave enemies must be cleared

---

### Contract CO9: wait
**Operation:** `wait(spawnInterval: float, gameSpeedMultiplier: float)`  
**Cross References:** Use Cases: Start Enemy Wave  
**Preconditions:**
- Enemy group is in process of spawning
- Spawn interval is defined for the enemy group

**Postconditions:**
- System waited for the defined interval adjusted by game speed
- Next enemy in group ready for spawning

**Constraints:**
- Spawn interval must account for game speed multiplier
- Wait must be interruptible if game is paused

---

### Contract CO10-Updated: spawnEnemyGroup
**Operation:** `spawnEnemyGroup(enemyGroup: EnemyGroup, gameOptions: GameOptions)`  
**Cross References:** Use Cases: Start Enemy Wave  
**Preconditions:**
- Wave is active and group spawn time reached
- Enemy group data is valid
- Spawn location is clear

**Postconditions:**
- Enemies spawned with stats from game options
- Resistances and vulnerabilities applied based on enemy type
- Combat synergy effects initialized for grouped enemies
- Enemy size and movement characteristics set
- Enemies added to path following system

**Constraints:**
- Enemy spawn location must be valid
- Enemy stats must be loaded from current game options
- Path must be available for enemy movement

---

### Contract CO11: closeMapSelection
**Operation:** `closeMapSelection()`  
**Cross References:** Use Cases: Start New Game  
**Preconditions:**
- Map selection menu is open
- Player wants to close without selecting

**Postconditions:**
- Map selection menu closed
- Player returned to main menu
- No game initialization performed

**Constraints:**
- System must cleanly close menu without side effects
- Return to main menu must be successful

---

### Contract CO12-Updated: checkPlayerStatus
**Operation:** `checkPlayerStatus()`  
**Cross References:** Use Cases: Display Game Over, Check Victory  
**Preconditions:**
- Game state needs evaluation
- Player status requires checking

**Postconditions:**
- Player health and shield status evaluated
- Castle health checked against maximum
- Game over triggered if health ≤ 0 and shield ≤ 0
- Victory triggered if all waves completed and no enemies remaining
- Game statistics updated

**Constraints:**
- Health and shield values must be accurately tracked
- Victory conditions must be properly evaluated
- Game over/victory states must be mutually exclusive

---

### Contract CO13: clickGameOverScreen
**Operation:** `clickGameOverScreen()`  
**Cross References:** Use Cases: Display Game Over  
**Preconditions:**
- Game over screen is displayed
- Player has clicked on the screen

**Postconditions:**
- Game statistics recorded
- Player choice processed (restart, main menu, or exit)

**Constraints:**
- Game over screen must be active
- Click must be valid user interaction

---

### Contract CO14: resetGameState
**Operation:** `resetGameState()`  
**Cross References:** Use Cases: Display Game Over, Restart Game  
**Preconditions:**
- Player wants to restart after game over
- Current game session exists

**Postconditions:**
- All game variables reset to starting values
- Managers reinitialized
- Map data reloaded
- Environmental objects restored to initial state
- Weather system reset
- Main menu displayed

**Constraints:**
- Complete state reset must be performed
- No previous game data should persist
- Reset must not cause memory leaks

---

### Contract CO15: closeGame
**Operation:** `closeGame()`  
**Cross References:** Use Cases: Display Game Over, Exit Game  
**Preconditions:**
- Player decides to exit the game
- Game can be safely closed

**Postconditions:**
- All resources cleaned up properly
- Any unsaved progress handled appropriately
- Game closed without data corruption
- Audio/visual resources released

**Constraints:**
- All threads and timers must be properly terminated
- File handles must be closed
- Memory must be properly released

---

### Contract CO16: clickPauseButton
**Operation:** `clickPauseButton()`  
**Cross References:** Use Cases: Pause / Resume Game  
**Preconditions:**
- Game is running and pauseable
- Player wants to pause

**Postconditions:**
- Game paused (all updates stopped)
- Pause menu prepared for display
- Game speed multiplier set to 0

**Constraints:**
- Game must be in playable state
- Pause functionality must be available

---

### Contract CO17: returnToMainMenu
**Operation:** `returnToMainMenu()`  
**Cross References:** Use Cases: Pause / Resume Game  
**Preconditions:**
- Player chooses to exit current game
- Game is paused or can be interrupted

**Postconditions:**
- Current game session ended
- Unsaved progress discarded or saved based on player choice
- Main menu displayed
- Resources cleaned up

**Constraints:**
- Player must confirm action if progress will be lost
- Clean transition to main menu required

---

### Contract CO18: gameContinues
**Operation:** `gameContinues()`  
**Cross References:** Use Cases: Pause / Resume Game  
**Preconditions:**
- Game is paused
- Player chooses to resume

**Postconditions:**
- Game resumed from exact previous state
- All timers and animations continue
- Game speed multiplier restored
- Pause menu closed

**Constraints:**
- All game elements must resume correctly
- No state should be lost during pause/resume cycle

---

## New Operation Contracts for Missing Systems

### Contract CO_Weather1: updateWeatherState
**Operation:** `updateWeatherState(gameTime: long, speedMultiplier: float)`  
**Cross References:** Use Cases: Weather System Management  
**Preconditions:**
- Weather system is enabled
- Game is running

**Postconditions:**
- Weather type updated based on season and time progression
- Day/night cycle progressed
- Weather effects applied (enemy speed, tower range modifications)
- Weather particles updated for visual effects

**Constraints:**
- Weather changes must follow seasonal rules
- Weather effects must be consistently applied

---

### Contract CO_Weather2: applyWeatherEffects
**Operation:** `applyWeatherEffects(weatherType: WeatherType)`  
**Cross References:** Use Cases: Weather Effects on Gameplay  
**Preconditions:**
- Weather type is active
- Game entities exist to be affected

**Postconditions:**
- Enemy speed modified (Snow: -25%)
- Tower range modified (Rain: -20%)
- Archer accuracy affected (Wind: +30% miss chance)
- Visual effects applied to match weather

**Constraints:**
- Effects must be reversible when weather changes
- Multiple weather effects must not stack incorrectly

---

### Contract CO_Warrior1: spawnWarrior
**Operation:** `spawnWarrior(tower: Tower, targetX: int, targetY: int, warriorType: WarriorType)`  
**Cross References:** Use Cases: Warrior Spawning System  
**Preconditions:**
- Tower can spawn warriors (currentWarriors < maxWarriors)
- Valid spawn and target positions
- Tower is not destroyed

**Postconditions:**
- Warrior created with lifetime tracking (30 seconds)
- Warrior begins movement from tower to target position
- Tower's current warrior count incremented

**Constraints:**
- Maximum warriors per tower must be enforced
- Warrior lifetime must be properly tracked

---

### Contract CO_Warrior2: updateWarriorState
**Operation:** `updateWarriorState(warrior: Warrior, speedMultiplier: float)`  
**Cross References:** Use Cases: Warrior Lifecycle Management  
**Preconditions:**
- Warrior exists and is active
- Game is not paused

**Postconditions:**
- Warrior position updated based on current state
- State transitions handled (RUNNING → IDLE → ATTACKING → RETURNING)
- Lifetime countdown progressed
- Warrior removed if lifetime expired or returned to tower
- Tower warrior count decremented if warrior removed

**Constraints:**
- State transitions must be logical and consistent
- Lifetime tracking must account for game speed
- Warrior removal must properly clean up resources

---

### Contract CO_Skill1: selectSkill
**Operation:** `selectSkill(skillType: SkillType, player: Player)`  
**Cross References:** Use Cases: Skill Tree Development  
**Preconditions:**
- Skill is available for selection
- Player has sufficient skill points

**Postconditions:**
- Skill marked as selected in skill tree
- Skill effects immediately applied to relevant systems
- Skill points deducted from player

**Constraints:**
- Skill selection must be permanent
- Effects must be applied consistently

---

### Contract CO_Skill2: applySkillEffects
**Operation:** `applySkillEffects(selectedSkills: List[SkillType])`  
**Cross References:** Use Cases: Skill System Integration  
**Preconditions:**
- Skills are selected in skill tree
- Game systems exist to receive effects

**Postconditions:**
- Economy skills applied (bonus gold, interest, starting resources)
- Tower enhancement skills applied (damage, range, special abilities)
- Ultimate skill bonuses applied (cooldown reduction, effect enhancement)

**Constraints:**
- Skill effects must stack appropriately
- Performance impact must be minimized
- Effects must be recalculated when relevant

---

### Contract CO_Ultimate1: useUltimateAbility
**Operation:** `useUltimateAbility(abilityType: UltimateType, targetArea: Area)`  
**Cross References:** Use Cases: Ultimate Abilities  
**Preconditions:**
- Ability cooldown has expired
- Valid target area selected

**Postconditions:**
- Ability effect applied to target area
- Cooldown timer reset with skill bonuses applied
- Visual and audio effects triggered

**Constraints:**
- Cooldown must be properly enforced
- Area effects must be calculated accurately

---

### Contract CO_Ultimate2: processEarthquake
**Operation:** `processEarthquake(epicenter: Point, magnitude: float, duration: long)`  
**Cross References:** Use Cases: Earthquake Ultimate Ability  
**Preconditions:**
- Earthquake ability has been activated
- Valid epicenter location provided

**Postconditions:**
- Damage applied to all enemies in affected radius
- Towers in radius may be destroyed based on condition
- Debris effects created for destroyed towers
- Ground shaking visual effects applied

**Constraints:**
- Damage calculation must account for distance from epicenter
- Tower destruction must follow condition-based rules
- Visual effects must not impact performance

---

### Contract CO_Ultimate3: processLightning
**Operation:** `processLightning(strikePositions: List[Point], chainRadius: float)`  
**Cross References:** Use Cases: Lightning Ultimate Ability  
**Preconditions:**
- Lightning ability has been activated
- Valid strike positions calculated

**Postconditions:**
- Lightning strikes created at specified positions
- Chain lightning effects applied to nearby enemies
- Stun effects applied to struck enemies
- Visual lightning effects displayed

**Constraints:**
- Chain lightning must respect maximum chain distance
- Stun duration must be properly tracked
- Multiple strikes must be coordinated properly

---

### Contract CO_Environment1: interactWithEnvironmentalObject
**Operation:** `interactWithEnvironmentalObject(objectType: EnvironmentalObjectType, position: Point)`  
**Cross References:** Use Cases: Environmental Interaction  
**Preconditions:**
- Valid environmental object exists at position
- Object is in interactable state

**Postconditions:**
- Object interaction processed (mining stone, chopping tree)
- Resources awarded to player
- Object state updated or removed

**Constraints:**
- Object state changes must be permanent
- Resource rewards must be balanced

---

### Contract CO_Environment2: updateEnvironmentalObjects
**Operation:** `updateEnvironmentalObjects(speedMultiplier: float)`  
**Cross References:** Use Cases: Environmental Object Management  
**Preconditions:**
- Environmental objects exist on the map
- Game is running

**Postconditions:**
- Object states updated (growth, decay, availability)
- Temporary objects (GoldBags) checked for expiration
- Collection effects processed
- Object interactions validated

**Constraints:**
- Object lifecycle must be properly managed
- Expired objects must be cleaned up
- State changes must be visually represented

---

### Contract CO_Resistance1: calculateDamage
**Operation:** `calculateDamage(rawDamage: int, damageType: DamageType, enemy: Enemy)`  
**Cross References:** Use Cases: Combat System  
**Preconditions:**
- Damage is being applied to enemy
- Enemy has resistance/vulnerability values
- Damage type is specified

**Postconditions:**
- Actual damage calculated using resistance/vulnerability formulas
- Damage applied to enemy health
- Combat effects triggered if applicable

**Constraints:**
- Damage calculation must be consistent and balanced
- Resistances and vulnerabilities must be properly applied
- Minimum damage thresholds must be respected

---

### Contract CO_Status1: applyStatusEffect
**Operation:** `applyStatusEffect(enemy: Enemy, effectType: StatusEffectType, duration: int)`  
**Cross References:** Use Cases: Status Effect System  
**Preconditions:**
- Enemy is alive and active
- Status effect parameters are valid

**Postconditions:**
- Status effect applied to enemy
- Effect duration set
- Visual indicators activated

**Constraints:**
- Multiple status effects must stack appropriately
- Effect duration must account for game speed

---

### Contract CO_Status2: updateStatusEffects
**Operation:** `updateStatusEffects(enemy: Enemy, speedMultiplier: float)`  
**Cross References:** Use Cases: Status Effect Processing  
**Preconditions:**
- Enemy has active status effects
- Game is not paused

**Postconditions:**
- Effect durations decremented
- Periodic effects processed (poison damage)
- Expired effects removed
- Enemy stats updated based on active effects

**Constraints:**
- Effect timers must be accurate
- Stat modifications must be reversible
- Performance must remain acceptable with many effects

---

### Contract CO_Save1: createGameStateMemento
**Operation:** `createGameStateMemento()`  
**Cross References:** Use Cases: Advanced Save System  
**Preconditions:**
- Game is in stable state
- All managers are initialized

**Postconditions:**
- Complete game state captured including:
  - Player stats and progress
  - All tower states with conditions and targeting
  - Enemy positions and status effects
  - Weather and environmental state
  - Skill tree progress
  - Wave and timing information

**Constraints:**
- Memento must be complete and consistent
- State capture must not affect game performance
- All manager states must be serializable

---

### Contract CO_Load1: restoreGameStateMemento
**Operation:** `restoreGameStateMemento(memento: GameStateMemento)`  
**Cross References:** Use Cases: Advanced Load System  
**Preconditions:**
- Valid game state memento provided
- Game systems are ready to receive state

**Postconditions:**
- Complete game state restored exactly as saved
- All managers properly initialized
- Game ready to continue from saved point
- Visual state matches saved state

**Constraints:**
- State restoration must be atomic (all or nothing)
- Compatibility with save format must be maintained
- No data corruption during restoration process 