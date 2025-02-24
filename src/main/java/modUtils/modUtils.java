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
                searchAlts.getInstance()
        );

    }

    /**
     * Automatically loads all commands.
     */
    private void loadCommands(Command... commands){
        for (Command command : commands){
            command.register();
        }
    }



}
