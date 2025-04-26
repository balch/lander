# Lunar Lander Game Development Guidelines

## Introduction
This document provides guidelines for creating a simple Lunar Lander game using Kotlin Multiplatform and Compose Multiplatform. The game will be playable on Android, iOS, Desktop, and Web platforms.

## Game Overview
Lunar Lander is a classic arcade game where players control a spacecraft and attempt to land it safely on the surface of the moon. The player must manage fuel, thrust, and orientation while dealing with gravity to achieve a gentle landing on designated landing pads.

## Game Mechanics

### Core Mechanics
1. **Gravity**: Constant downward force pulling the lander toward the surface
2. **Thrust**: Player-controlled upward force to counteract gravity
3. **Rotation**: Ability to rotate the lander left or right
4. **Fuel Management**: Limited fuel supply that depletes when using thrust
5. **Landing**: Successfully touching down on a landing pad with:
    - Low vertical velocity
    - Low horizontal velocity
    - Correct orientation (upright)

### Game Screens
1. **Start Screen** 
   1. Title
   2. Instructions 
   3. User Configurable Options
      1. slider for fuel
         1. labeled extents are low and high
      2. 5 options for gravity based on Moon Gravity
      3. 3 options for size of landing pad
      4. 3 options for thrust strength
   4. Start Button
      1. Transition user to the Playing Screen 
      2. Start Game play using the options provided in item 3
   5. Use dark mode theme
      1. background includes stars
      2. text is light enough to read
2. **Playing Screen**: Active gameplay with lander controls
   1. Optimize UI for Landscape orientation
   2. Display lander info to user
      1. Fuel
      2. Rate of Descent 
      3. Side Drift direction and speed
      4. Distance to Ground
   3. Display lander control pad 
      1. Rotate Left
      2. Rotate Right
      3. Thrust
      4. Small button on the bottom right of the screen
   4. Game Play Behavior
      1. start at top center of screen
      2. lander descent is based on selected gravity
      3. camera should zoom in as lander approaches ground
      4. terrain should scroll on screen as lander gets close to the left or right screen edge
      5. provide alarm if lander is about to crash
         1. based on fuel, descent velocity, distance to ground
         2. audio
         3. flash the lander as red
      6. If the Lander Lands successfully
         1. Show success animation
         2. play success audio
         3. show a random message suggesting the user did a good job
         4. Show Nav button for Restart to Back to Start Screen
      7. If the Lander Crashes
         1. Show crash animation
         2. play crash audio
         3. show a random message suggesting the user needs to learn to navigate better
         4. Show Nav button for Restart to Back to Start Screen

## Implementation Steps

### 1. Project Setup
- Use the existing Kotlin Multiplatform project structure
- Set up the game loop in the `commonMain` source set
- Configure platform-specific input handling in respective source sets

### 2. Game Physics
- Based on Moon Gravity
- implement collision detection between lander and terrain
- lander lateral drift should be based on the physics model also
- make physics engine configurable based on user input and code config
- utilize thread paralellism if needed

### 3. App Architecture
Base on latest JetBrains and Googles latest arch patterns for KMP
- use koin for dependency injection if needed
- use coroutines for state management
  - do game physics in background thread
  - render ui in main thread
- use Compose Multiplatform
  - make sure all @Composable have previews using @Preview
- use MaterialTheme
  - prefer dark background with light text
  - give everything a retro look and feel

### 4. Input Handling
Implement platform-specific input handling:
- show on screen controls to handle left, right, and up thrust
- implement platform specific keyboard handling for thrust
  - space key maps to up thrust
  - left key maps to rotate left
  - right key maps to rotate right

### 5. Terrain Generation
Create random moon terrain with landing pads:
- landing pad size is based on user config option
- terrain scrolls on to screen as user gets close to the edge

## Asset Requirements

### Graphics
1. **Lander Spacecraft**: Main player character with different states (normal, thrusting, crashed)
2. **Moon Surface**: Terrain with varying elevations
3. **Landing Pad**: Flat area with markings
   1. highlight with different colors 
4. **Background**: Space backdrop with stars
   1. not too many stars
   2. optimize for performance 
5. **UI Elements**: Buttons, gauges, indicators

### Audio
1. **Thrust Sound**: Engine firing
2. **Landing Sound**: Successful touchdown
3. **Explosion Sound**: Failed landing
4. **Background Music**: Ambient space theme
5. **UI Sounds**: Button clicks, alerts

## Testing and Debugging

### Physics Testing
- Test lander movement under different conditions
- Verify collision detection with terrain
- Ensure landing detection works correctly

### Performance Optimization
- Keep physics calculations simple
- Use efficient rendering techniques
- Implement frame rate limiting

### Cross-Platform Testing
- Test on all target platforms
- Adjust controls for different input methods
- Ensure consistent performance

## Resources and References

### Learning Resources
- [Kotlin Multiplatform Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/)
- [Game Physics Tutorials](https://www.toptal.com/game/video-game-physics-part-i-an-introduction-to-rigid-body-dynamics)

### Inspiration
- [Original Lunar Lander Game](https://en.wikipedia.org/wiki/Lunar_Lander_(video_game_genre))
- [Modern Implementations](https://github.com/topics/lunar-lander)
