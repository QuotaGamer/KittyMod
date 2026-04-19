package me.quota73;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class KittyMod implements ClientModInitializer {
    // Fake-ban stuff
    public static boolean isBanned = false;
    public static long    banTime = 0;
    public static String  banID = randomHex(8);

    // And everything else
    private static final Gson gson = new Gson();
    public static final Random random = new Random();
    public static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("kittymod.json");
    public static ConfigClass config = null;
    public static Logger logger = LoggerFactory.getLogger("kittymod");
    public static final List<String> meows = List.of(
            "purrr :3", "nya", "mrrow",
            "mrrp", "maow", "nyaaa", "moew", "purr",
            "meow :3", "mroww", "mrow", "meow", "nya～",
            "mew", "nyan", "prrp", "miau");
    public static final List<String> woofs = List.of(
            "woof :3", "wruff", "ruff", "wuff",
            "arf! arf!!", "arf arf", "*pants cutely*",
            "awoooo～", "grrrr..", "woof!", "bark! bark!",
            "*sniff sniff*", "wroof");

    public static Text rainbow(String text) {
        MutableText out = Text.empty();
        int len = text.length();
        double mod = random.nextDouble(0.1, 4);

        for (int i = 0; i < len; i++) {
            float hue = (float) (i * mod) / (float) len;
            int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);

            out.append(Text.literal(String.valueOf(text.charAt(i)))
                    .setStyle(Style.EMPTY.withColor(rgb)));
        }
        return out;
    }

    public static String randomHex(int charAmount) {
        String chars = "0123456789ABCDEF";
        StringBuilder sb = new StringBuilder(charAmount);
        Random localRandom = new Random();

        for (int i = 0; i < charAmount; i++)
            sb.append(chars.charAt(localRandom.nextInt(16)));

        return sb.toString();
    }

    public static void safeSleep(int millis) { try { Thread.sleep(millis); } catch (InterruptedException ignored) {} }

	@Override
	public void onInitializeClient() {
        AtomicLong cooldownRemaining = new AtomicLong(0L);
        AtomicLong lt = new AtomicLong(System.currentTimeMillis());
        if (Files.exists(configPath)) {
            try (FileReader reader = new FileReader(configPath.toFile())) { config = gson.fromJson(reader, ConfigClass.class);
            } catch (Exception e) {
                System.err.println(e.getClass() + ": " + e.getMessage());
                config = new ConfigClass();
                saveConfig();
            }
        } else {
            config = new ConfigClass();
            saveConfig();
        }

        MinecraftClient mc = MinecraftClient.getInstance();

        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            if (config.phoenixBarEnabled) {
                int width = mc.getWindow().getScaledWidth();
                boolean active = cooldownRemaining.get() != 0L;
                drawContext.fill(0, 0, width, 4, active ? Color.DARK_BLUE : Color.GREEN);
                if (active) drawContext.fill(0, 0, Math.round((cooldownRemaining.get() / 60f) * width), 4, Color.DARK_RED);
            }
        });


        ClientCommandRegistrationCallback.EVENT.register((dispatcher, source) ->
            dispatcher.register(ClientCommandManager.literal("kittymod")
                    .then(ClientCommandManager.literal("Tests")
                            .then(ClientCommandManager.literal("RainbowMessage").executes(this::rainbowTest)))
                    .then(ClientCommandManager.literal("Config").executes((ctx) -> {
                        mc.execute(() -> mc.setScreen(new ConfigScreen(mc.currentScreen)));
                        return 1;
                    }))
            ));

        ClientReceiveMessageEvents.ALLOW_GAME.register((text, ignored) -> {
            String matchText = text.getString().toLowerCase();

            if (config.debug) logger.info(text.getString());
            if ((matchText.startsWith("puzzle fail!") && (!matchText.contains("killed a blaze in the wrong order! yikes!"))
                    || matchText.startsWith("[statue] oruo the omniscient: yikes")) && (!FabricLoader.getInstance().isModLoaded("haruaddon"))) {
                if (mc.player != null) {
                    new Thread(() -> {
                        safeSleep(250);
                        mc.execute(() -> mc.player.networkHandler.sendChatCommand("lobby"));
                        safeSleep(500);
                        mc.execute(() -> mc.player.networkHandler.sendChatCommand("lobby"));
                        safeSleep(1250);
                        mc.execute(() -> mc.inGameHud.getChatHud().addMessage(Text.literal("§eYour SkyBlock Profile§c§l has been wiped§e as you or a co-op member was determined to be boosting."
                        + "\n§eIf you believe this to be in error, you can contact our support team: §b§nsupport.hypixel.net")));
                        safeSleep(350);
                        mc.execute(() -> mc.player.networkHandler.sendChatCommand("limbo"));
                        safeSleep(3500);
                        isBanned = true;
                        banTime = Instant.now().getEpochSecond();
                        mc.player.networkHandler.getConnection().disconnect(Text.literal("§cYou are temporarily banned for §f" + TimeFormat.secondsToFancy(KittyMod.banTime + 2591999 - Instant.now().getEpochSecond()) + "§c from this server!"
                                + "\n\n§7Reason: §fCheating through the use of unfair game advantages.\n§fFind out more: §b§nhttps://www.hypixel.net/appeal\n\n§7Ban ID: §f#" + KittyMod.banID + "\n§7Sharing your Ban ID may affect the processing of your appeal!"));
                    }).start();
                } return true;
            } return (!config.disableWhitespaceEnabled || !List.of("  ", " ", "{\"text\":\"\",\"extra\":[\" \"],\"italic\":false}", "{\"text\":\"\",\"extra\":[\"  \"],\"italic\":false}").contains(gson.toJson(TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow())))
                    && (!config.hideFireSaleEnabled || !text.contains(Text.literal("§c§lFIRE SALE")));
        });

        ClientReceiveMessageEvents.MODIFY_GAME.register((text, ignored) -> {
            String matchText = text.getString().toLowerCase().replaceAll("§.", "");
            String whatChat;
            if (matchText.startsWith("party > ")) whatChat = "pchat";
            else if (matchText.startsWith("guild > ")) whatChat = "gchat";
            else if (matchText.startsWith("co-op > ")) whatChat = "cchat";
            else if (matchText.startsWith("officer > ")) whatChat = "ochat";
            else if (matchText.startsWith("to ")) whatChat = "r"; // this is technically wrong, but I don't care~ :3
            else if (matchText.startsWith("from ")) whatChat = "r";
            else whatChat = "achat";
            if (config.debug) logger.info("Chat detected: " + whatChat);

            if (text.getString().equals("Your Phoenix Pet saved you from certain death!")) {
                if (config.phoenixBarEnabled) cooldownRemaining.set(60L);

                if (config.phoenixMessageEnabled) return Text.literal("§eYour §cPhoenix Pet§e saved you from Epstein's Island!");
            }

            String jsonText = gson.toJson(TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow());

            if (config.autoMeowEnabled && mc.player != null && !text.getString().contains(mc.player.getGameProfile().name()) && matchText.endsWith("meow")) {
                new Thread(() -> {
                    safeSleep(random.nextInt(450, 650));
                    mc.execute(() -> mc.player.networkHandler.sendChatCommand(whatChat + " " + meows.get(random.nextInt(meows.size()))));
                }).start();
            }

            if (config.autoWoofEnabled && mc.player != null && !text.getString().contains(mc.player.getGameProfile().name()) && matchText.endsWith("woof")) {
                new Thread(() -> {
                    safeSleep(random.nextInt(450, 650));
                    mc.execute(() -> mc.player.networkHandler.sendChatCommand(whatChat + " " + woofs.get(random.nextInt(woofs.size()))));
                }).start();
            }

            if (config.debug) mc.inGameHud.getChatHud().addMessage(Text.literal(jsonText));

            if (config.shortChatsEnabled)
                jsonText = jsonText.replaceFirst(Pattern.quote("§9Party §8\\u003e "), "§7[§9PC§7] ") // works for party chats
                                   .replaceFirst(Pattern.quote("§2Guild \\u003e "), "§7[§2GC§7] ") // works for guild chats
                                   .replaceFirst(Pattern.quote("{\"text\":\"To \""), "{\"text\":\"§d→ §f\"") // works for outgoing DMs
                                   .replaceFirst(Pattern.quote("{\"text\":\"From \""), "{\"text\":\"§d← §f\"") // works for incoming DMs

                                   .replaceFirst(Pattern.quote("{\"text\":\"Guild \\u003e \""), "{\"text\":\"§7[§2GJ§7] §2\"") // works for guild joins
                                   .replaceFirst(Pattern.quote("{\"text\":\"Friend \\u003e \""), "{\"text\":\"§7[§aFJ§7] §2\""); // works for friend joins

            if (config.rankHiderEnabled) {
                jsonText = jsonText.replaceAll("§[0-9a-fk-or]\\[(VIP|MVP)(§[0-9a-fk-or])?(\\+{1,2})?(§[0-9a-fk-or])?] ", "§7")
                        .replaceAll("\\{\"text\":\"\\[MVP\",\"color\":\"#55FFFF\".*?},\\{\"text\":\"(\\+{1,2})?\",\"color\":\"#55FF55\".*?},\\{\"text\":\"] ([^\"}]*)\",\"color\":\"#55FFFF\".*?}", "{\"text\":\"§7$1\"}");

                if (jsonText.startsWith("{\"text\":\"\",\"extra\":[{\"text\":\" ☠ \",\"color\":\"red\"}"))
                    jsonText = jsonText.replace("\"color\":\"aqua\"", "\"color\":\"gray\"")
                               .replace("\"color\":\"light_green\"", "\"color\":\"gray\"");
            }

            if (config.hideDiscordWarningEnabled) {
                jsonText = jsonText.replaceAll(",?\\{\"text\":\"\\\\n\",\"strikethrough\":false},\\{\"text\":\"Please be mindful of Discord links in chat as they may pose a security risk\",\"color\":\"red\",\"strikethrough\":false},?", "");
            }

            if (config.chatColorsEnabled)
                jsonText = jsonText.replaceAll("&([0-9a-fk-or])", "§$1")
                                   .replaceAll("\\\\u0026([0-9a-fk-or])", "§$1");

            return TextCodecs.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(jsonText)).getOrThrow();
        });


        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (cooldownRemaining.get() != 0L) {
                long ct = System.currentTimeMillis();
                if (ct >= lt.get() + 1000L) {
                    lt.set(ct);
                    long remain = cooldownRemaining.get();
                    if (remain != 0) cooldownRemaining.set(remain - 1);
                }
            }

            if (client.world instanceof ClientWorld world
                    && config.Glassorite) {
                for (BlockPos blockPosition : BlockPos.iterate(new Box(40, 169, 36, 50, 206, 45))) {
                    BlockState state = world.getBlockState(blockPosition);

                    if (state.isOf(Blocks.DIORITE) || state.isOf(Blocks.POLISHED_DIORITE)) {
                        world.setBlockState(blockPosition, Blocks.GREEN_STAINED_GLASS.getDefaultState());
                    }
                }

                for (BlockPos blockPosition : BlockPos.iterate(new Box(40, 169, 60, 50, 206, 69))) {
                    BlockState state = world.getBlockState(blockPosition);

                    if (state.isOf(Blocks.DIORITE) || state.isOf(Blocks.POLISHED_DIORITE)) {
                        world.setBlockState(blockPosition, Blocks.YELLOW_STAINED_GLASS.getDefaultState());
                    }
                }

                for (BlockPos blockPosition : BlockPos.iterate(new Box(95, 169, 60, 110, 206, 69))) {
                    BlockState state = world.getBlockState(blockPosition);

                    if (state.isOf(Blocks.DIORITE) || state.isOf(Blocks.POLISHED_DIORITE)) {
                        world.setBlockState(blockPosition, Blocks.PURPLE_STAINED_GLASS.getDefaultState());
                    }
                }

                for (BlockPos blockPosition : BlockPos.iterate(new Box(95, 169, 36, 110, 206, 45))) {
                    BlockState state = world.getBlockState(blockPosition);

                    if (state.isOf(Blocks.DIORITE) || state.isOf(Blocks.POLISHED_DIORITE)) {
                        world.setBlockState(blockPosition, Blocks.RED_STAINED_GLASS.getDefaultState());
                    }
                }
            }
        });
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (config.cancelInteractCooldown && client.player != null) client.player.getItemCooldownManager().remove(Registries.ITEM.getId(Items.ENDER_PEARL));
        });
	}

    private int rainbowTest(CommandContext<FabricClientCommandSource> ctx) {
        if (MinecraftClient.getInstance() instanceof MinecraftClient mc)
            mc.inGameHud.getChatHud().addMessage(rainbow("Here's a test rainbow message, nya~"));
        return 1;
    }

    private int supportiveMessage(CommandContext<FabricClientCommandSource> ctx) {
        List<Text> messages = List.of(
                Text.literal("§7Things§o will§r§7 get better, I promise."),
                Text.literal("§7Even if you think nobody does, Someone definitely still cares about you."),
                Text.literal("§7Whenever life gets§c bad§7, it'll always get§a better§7 after."),
                Text.literal("§7There's always §flight §7at the end of the tunnel."),
                Text.literal("§7You've got this, even if you don't§o think§r§7 you do now."));
        if (MinecraftClient.getInstance() instanceof MinecraftClient mc)
            mc.inGameHud.getChatHud().addMessage(messages.get(random.nextInt(0, messages.size())));
        return 1;
    }

    private void saveConfig() {
        try (FileWriter writer = new FileWriter(configPath.toFile())) { gson.toJson(config, writer);
        } catch (Exception e) { System.err.println(e.getClass() + ": " + e.getMessage()); }
    }
}