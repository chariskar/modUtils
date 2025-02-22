/*
 * This file is part of modUtils.
 *
 * modUtils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * modUtils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with modUtils. If not, see <https://www.gnu.org/licenses/>.
 */

package modUtils.commands;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import modUtils.commands.Command;
import java.util.ArrayList;
import java.util.List;

public class checkAlts extends Command {

    public void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("searchAlts")
                            .executes(context -> {

                                performAltCheck();
                                return 1;
                            })
            );
        });
    }
    /**
     * Iterates over online players, simulates the /alts command for each,
     * and displays a chat message listing players with multiple alternate accounts.
     */
    private void performAltCheck() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            client.player.sendMessage(Text.literal("No world loaded."), false);
            return;
        }

        List<String> flaggedPlayers = new ArrayList<>();

        // Iterate through all players currently in the world.
        client.world.getPlayers().forEach(player -> {
            String playerName = player.getGameProfile().getName();
            String altsOutput = simulateAltsCommand(playerName);
            if (hasMultipleAlts(altsOutput)) {
                flaggedPlayers.add(playerName);
            }
        });

        // Build the output with a header and flagged player names.
        StringBuilder output = new StringBuilder();
        output.append("[Online] [Offline] [Banned] [IPBanned]\n");
        if (flaggedPlayers.isEmpty()) {
            output.append("No players flagged.");
        } else {
            flaggedPlayers.forEach(name -> output.append(name).append("\n"));
        }

        // Display the results in the chat.
        client.player.sendMessage(Text.literal(output.toString()), false);
    }

    /**
     *
     * Simulates the output of the "/alts {player}" command.
     *
     * <p>For demonstration, this dummy implementation assumes that if the player's name
     * contains "alt" (case insensitive), then the output lists multiple alternate accounts.
     * Otherwise, it returns a single name.
     *
     * @param playerName the name of the player to simulate the command for.
     * @return a simulated multi-line output string.
     */
    private String simulateAltsCommand(String playerName) {
        if (playerName.toLowerCase().contains("alt")) {
            // Simulate multiple alts: header line and a list with commas.
            return "[Online] [Offline] [Banned] [IPBanned]\n"
                    + playerName + ", AltAccount1, AltAccount2";
        } else {
            // Simulate a single alt.
            return "[Online] [Offline] [Banned] [IPBanned]\n" + playerName;
        }
    }

    /**
     * Determines whether the simulated output indicates multiple alts.
     *
     * <p>This method splits the output by newlines, ignores the first line (header),
     * and checks if the second line (the alts list) contains a comma.
     *
     * @param output the simulated command output.
     * @return true if multiple alts are detected; false otherwise.
     */
    private boolean hasMultipleAlts(String output) {
        if (output == null || output.isEmpty()) {
            return false;
        }
        String[] lines = output.split("\\R"); // Split on any line break.
        if (lines.length < 2) {
            return false;
        }
        String altList = lines[1].trim();
        return altList.contains(",");
    }
}
