package me.quota73.mixins;

import me.quota73.KittyMod;
import me.quota73.TimeFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {

    @Inject(method = "connect", at = @At("HEAD"), cancellable = true)
    private static void onConnect(Screen screen, MinecraftClient client, ServerAddress address, ServerInfo info, boolean reuseScreen, CookieStorage cookie, CallbackInfo ci) {

        String host = address.getAddress().split(":")[0];

        if ((host.equalsIgnoreCase("hypixel.net") || host.equalsIgnoreCase("mc.hypixel.net")) && KittyMod.isBanned) {
            ci.cancel();

            client.setScreen(new DisconnectedScreen(
                    new MultiplayerScreen(new TitleScreen()),
                    Text.literal("Connecting to the server..."),
                    Text.empty(),
                    Text.literal("Cancel")
            ));
            new Thread(() -> {
                KittyMod.safeSleep(KittyMod.random.nextInt(350, 650));
                client.execute(() -> client.setScreen(new DisconnectedScreen(
                        new MultiplayerScreen(new TitleScreen()),
                        Text.literal("Connection Lost"),
                        Text.literal("§cYou are temporarily banned for §f" + TimeFormat.secondsToFancy(KittyMod.banTime + 2591999 - Instant.now().getEpochSecond()) + "§c from this server!"
                                + "\n\n§7Reason: §fCheating through the use of unfair game advantages.\n§fFind out more: §b§nhttps://www.hypixel.net/appeal\n\n§7Ban ID: §f#" + KittyMod.banID + "\n§7Sharing your Ban ID may affect the processing of your appeal!")
                )));
            }).start();
        }
    }
}