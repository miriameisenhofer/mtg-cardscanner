# MTG Card Scanner
A personal scanner for organizing one's MTG collection.
## Demo
Demo 1: Creating and loading .csv's

Demo 2: Scanning cards

![](https://github.com/miriameisenhofer/mtg-cardscanner/blob/main/select-collection.gif)
![](https://github.com/miriameisenhofer/mtg-cardscanner/blob/main/card-scan.gif)

TODO:
- New Demos/Screenshots (of created .csv's)
- Reproduce and fix null-pointer bug on scanning card
- Return to MainActivity/PreviewFragment upon adding card to collection
- Refactoring: Develop internal data structure to represent collection (with contains and insertion functions), create .csv's based on this

## Installation & Usage
1. Clone repository:<br/>
   **`git clone https://github.com/miriameisenhofer/mtg-cardscanner.git`**
3. Open project in Android Studio:<br/>
   **File > Open...** and navigate to the project folder (`mtg-cardscanner`), select the project and click **Open**
5. Sync Gradle: Android Studio will automatically sync the Gradle files. (If not, select **File > Sync Project with Gradle Files**)
7. Connect phone or set up Android Virtual Device
8. Run the project: Choose target device (phone or emulator) and click **Run**

## About
Uses the Scryfall API. (**Not** affiliated with Scryfall or MTG/WotC)
