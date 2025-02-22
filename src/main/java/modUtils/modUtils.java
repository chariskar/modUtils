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
 * along with <Your Project Name>. If not, see <https://www.gnu.org/licenses/>.
 */
package modUtils;

import modUtils.commands.Command;
import modUtils.commands.searchAlts;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class modUtils implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("modUtils");

    @Override
    public void onInitializeClient() {
        MinecraftClient client = MinecraftClient.getInstance();
        loadCommands(
                new searchAlts()
        );
        ClientReceiveMessageEvents.GAME.register(this::onGameMessageReceived);
    }

    /**
     * Automatically loads all commands.
     */
    private void loadCommands(Command... commands){
        for (Command command : commands){
            command.register();
        }
    }

    /**
     * detect if a co command has been used, so the output can be logged to be analysed further
     */
    private void onGameMessageReceived(Text message, boolean overlay) {
        String messageContent = message.getString();
        Pattern pattern = Pattern.compile("^/(co l|co lookup|coreprotect l|coreprotect lookup)\\b");
        Matcher matcher = pattern.matcher(message.toString());
        StringBuilder coreProtectBuffer = new StringBuilder();
        if (messageContent.contains("CoreProtect - Lookup searching")) {
            coreProtectBuffer.setLength(0); // clear old data
        }

        coreProtectBuffer.append(messageContent).append("\n");

        if (messageContent.contains("No block data found") || messageContent.contains("Lookup Results")) {
            // TODO: implement the actual logic on how the message is collected
        }

    }

}
