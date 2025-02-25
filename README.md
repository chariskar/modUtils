Below is a sample README in Markdown that you can use as a full-fledged readme for your mod:

---

# modUtils

**modUtils** is a Minecraft utility mod that provides a suite of features for server administration and moderation. The current version focuses on **Alt Detection**, scanning the player list via the EarthMC API to detect alternate accounts and flag players based on specific criteria.

---

## Overview

The alt detection system is designed to:
- Scan the online player list (or tab list) for all connected players.
- Process players in manageable chunks with built-in rate limit handling.
- Flag players who have two or more alternate accounts.
- Display flagged players with a color-coded status:
    - **Dark Red**: Indicates at least one banned alt.
    - **Yellow**: Indicates at least one IP banned alt.
    - **Gray**: Indicates no special status (default for offline or other states).

If only one (or zero) player is flagged, the mod will output a summary message instead of detailed flagged player information.

---

## Features

- **Alt Detection**: Automatically scans and identifies players with multiple alt accounts.
- **Color-Coded Status**:
    - **Dark Red** for banned accounts.
    - **Yellow** for IP banned accounts.
    - **Gray** for all other cases (green is omitted since everyone is online anyway).
- **Rate Limit Handling**: Introduces appropriate delays between requests to avoid API rate limiting.
- **In-Game Command**: Simply type `/searchAlts` in chat to begin scanning.
- **Sorting**: Flagged players are sorted from highest to lowest by the number of alt accounts.

---

## Installation

### Requirements
- **Minecraft Version**: Compatible with Fabric loader.
- **Java**: JDK 17 (or the version required by your environment).
- **Fabric API**: Ensure that you have the Fabric API installed.

### Steps
1. **Download**: Obtain the latest `modUtils.jar` from the [Releases](#) page.
2. **Place in Mods Folder**: Copy the jar file to your Minecraft `mods` folder.
3. **Launch**: Start Minecraft using the Fabric loader.

---

## Usage

1. **Starting the Scan**:
    - In-game, run the command:
      ```
      /searchAlts
      ```
    - The mod will begin scanning all players, sending requests in chunks to the EarthMC API.

2. **Interpreting the Output**:
    - **Flagged Players**: Those with 2 or more alt accounts will be listed.
    - **Color Codes**:
        - **Dark Red**: Player has at least one banned alt.
        - **Yellow**: Player has at least one IP banned alt.
        - **Gray**: Default status if no banned conditions are met.
    - **Summary**: If only one or no players are flagged, a summary message like "X out of Y people scanned have been flagged." will be displayed instead.

3. **Rate Limit Handling**:  
   The mod automatically handles rate limits by introducing delays between requests.

---

## Configuration

Currently, configuration is done in-code. For example:
- **SCAN_DELAY**: Adjust the delay (in milliseconds) between player scans if needed.

Future versions may include a configuration file or GUI.

---

## License

This mod is licensed under the GNU General Public License v3 or later. You should have received a copy of the license with this mod; if not, see [GNU's website](https://www.gnu.org/licenses/).

---

## Credits

- **Author**: chariskar (Charilaos Karametos)
- **Special Thanks**: Veyronity for the suggestions and guidance.


---

Enjoy using **modUtils** for robust alt detection and server management! If you have any questions or feedback, please feel free to contact the author or open an issue on the repository.

---

This README provides a complete overview and instructions for your mod. Adjust sections as needed to match your project's details and style preferences.