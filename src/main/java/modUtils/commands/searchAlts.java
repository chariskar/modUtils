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
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.concurrent.*;

public class searchAlts extends Command {
    private static searchAlts instance;

    // Scanning state.
    private boolean scanningInProgress = false;
    private String currentScanning = null;
    // Buffer for alt lines received for the current player.
    private final List<String> altNamesBuffer = new ArrayList<>();
    // Map of player name -> list of alt entries.
    private final Map<String, List<String>> altData = new HashMap<>();
    // Queue of players to scan.
    private final Queue<String> playerQueue = new LinkedList<>();

    // Scheduler for processing players sequentially.
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    // Fixed delay per player (milliseconds) – adjust as needed for speed vs reliability.
    private final int SCAN_DELAY = 1250;

    private searchAlts() {}

    public static searchAlts getInstance() {
        if (instance == null) {
            instance = new searchAlts();
        }
        return instance;
    }

    /**
     * Registers the /searchAlts command and chat listeners.
     */
    public void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("searchAlts")
                            .executes(ctx -> {
                                startAltScan();
                                return 1;
                            })
            );
        });

        // Listen on both game and chat channels.
        ClientReceiveMessageEvents.ALLOW_GAME.register((text, overlay) -> handleIncomingChat(text));
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, sender, typeKey, params, receptionTime) -> handleIncomingChat(message));
    }

    /**
     * Initiates the alt scanning process.
     */
    private void startAltScan() {
        clearAltData();
        queueAllPlayers();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("Starting alt scans..."), false);
        }
        if (playerQueue.isEmpty()) {
            client.player.sendMessage(Text.literal("No players found to scan."), false);
            return;
        }
        scanningInProgress = true;
        processNextPlayer();
    }

    /**
     * Fills playerQueue using the network handler’s player list (tab list) if available,
     * otherwise falls back to the world's player list.
     */
    private void queueAllPlayers() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                String name = entry.getProfile().getName();
                if (!name.equals(client.player.getGameProfile().getName())) {
                    playerQueue.add(name);
                }
            }
        } else if (client.world != null && client.player != null) {
            client.world.getPlayers().forEach(player -> {
                String name = player.getGameProfile().getName();
                if (!name.equals(client.player.getGameProfile().getName())) {
                    playerQueue.add(name);
                }
            });
        }
        System.out.println("Queued players count: " + playerQueue.size());
    }

    /**
     * Processes the next player in the queue.
     * Sends the "/alts <name>" command and schedules finalization after SCAN_DELAY.
     */
    private void processNextPlayer() {
        if (playerQueue.isEmpty()) {
            finishAllScans();
            return;
        }
        currentScanning = playerQueue.poll();
        altNamesBuffer.clear();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.networkHandler.sendChatCommand("alts " + currentScanning);
        }

        scheduler.schedule(() -> MinecraftClient.getInstance().execute(() -> {
            finalizeCurrentScan();
            processNextPlayer();
        }), SCAN_DELAY, TimeUnit.MILLISECONDS);
    }

    /**
     * Finalizes the scan for the current player by processing the buffered alt data.
     * This revised logic splits a line only on commas, so that alt names with spaces remain intact.
     */
    private void finalizeCurrentScan() {
        if (currentScanning != null && !altNamesBuffer.isEmpty()) {
            List<String> finalList = new ArrayList<>();
            for (String line : altNamesBuffer) {
                if (line.contains(",")) {
                    String[] parts = line.split(",");
                    for (String part : parts) {
                        String alt = part.trim();
                        if (!alt.isEmpty()) {
                            finalList.add(alt);
                        }
                    }
                } else {
                    String alt = line.trim();
                    if (!alt.isEmpty()) {
                        finalList.add(alt);
                    }
                }
            }
            System.out.println("Scanned player " + currentScanning + " alt list: " + finalList);
            altData.put(currentScanning, finalList);
            altNamesBuffer.clear();
        }
        currentScanning = null;
    }

    /**
     * Determines the overall status for a player based on their alt data.
     * Returns a Formatting color:
     *  - RED if any alt is banned,
     *  - YELLOW if any alt is IP banned,
     *  - GREEN if any alt is online,
     *  - GRAY otherwise (offline).
     *
     * This assumes that alt entries contain one of these keywords.
     */
    private Formatting determinePlayerStatus(List<String> alts) {
        boolean online = false;
        boolean banned = false;
        boolean ipbanned = false;
        for (String alt : alts) {
            String lower = alt.toLowerCase();
            if (lower.contains("banned")) {
                banned = true;
            }
            if (lower.contains("ipbanned") || lower.contains("ip-banned")) {
                ipbanned = true;
            }
            if (lower.contains("online")) {
                online = true;
            }
        }
        if (banned) return Formatting.RED;
        if (ipbanned) return Formatting.YELLOW;
        if (online) return Formatting.GREEN;
        return Formatting.GRAY;
    }

    /**
     * Called when scanning is complete.
     * Displays flagged players (those with 2+ alt accounts) with colored names and alt counts.
     */
    private void finishAllScans() {
        scanningInProgress = false;
        // Finalize any pending scan.
        finalizeCurrentScan();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        List<String> flagged = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : altData.entrySet()) {
            if (entry.getValue().size() >= 2) {
                flagged.add(entry.getKey());
            }
        }
        if (flagged.isEmpty()) {
            client.player.sendMessage(Text.literal("No flagged players."), false);
        } else {
            client.player.sendMessage(Text.literal("Flagged players:"), false);
            for (String name : flagged) {
                List<String> alts = altData.get(name);
                Formatting color = determinePlayerStatus(alts);
                int count = alts.size();
                Text message = Text.literal(name + " (" + count + " accounts)").formatted(color);
                client.player.sendMessage(message, false);
            }
        }
    }

    /**
     * Intercepts incoming chat messages.
     * If scanning is active, stores messages as alt data.
     */
    private boolean handleIncomingChat(Text text) {
        String message = text.getString().trim();
        if (message.isEmpty() || !scanningInProgress) {
            return true;
        }
        // Optionally ignore system messages.
        if (message.startsWith("Scanning ") || message.equalsIgnoreCase("No Player's Tagged.")) {
            return true;
        }
        altNamesBuffer.add(message);
        return true;
    }

    /**
     * Returns an unmodifiable view of the alt data.
     */
    public Map<String, List<String>> getAltData() {
        return Collections.unmodifiableMap(altData);
    }

    /**
     * Clears all stored alt data.
     */
    public void clearAltData() {
        altData.clear();
        altNamesBuffer.clear();
        currentScanning = null;
        scanningInProgress = false;
        playerQueue.clear();
    }
}
