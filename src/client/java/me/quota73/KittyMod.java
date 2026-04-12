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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class KittyMod implements ClientModInitializer {
    public static final boolean enableCheats = false;
    private static final Gson gson = new Gson();
    public static final Random random = new Random();
    public static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("kittymod.json");
    public static ConfigClass config = null;
    public static final List<String> meows = List.of(
            "purrr :3", "nya", "mrrow",
            "mrrp", "maow", "nyaaa", "moew", "purr",
            "meow :3", "mroww", "mrow", "meow", "nya～",
            "mew", "nyan", "prrp");
    public static final List<String> woofs = List.of(
            "woof :3", "wruff", "ruff", "wuff",
            "arf! arf!!", "arf arf", "*pants cutely*",
            "awoooo～", "grrrr..", "woof!", "bark! bark!",
            "*sniff sniff*", "wroof");

    private static final Map<String, Integer> blueOffset = Map.of(
            "minecraft:blue_stained_glass_pane", 0,
            "minecraft:green_stained_glass_pane", 1,
            "minecraft:yellow_stained_glass_pane", 2,
            "minecraft:orange_stained_glass_pane", 3,
            "minecraft:red_stained_glass_pane", 4
    );

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

    public static void safeSleep(int millis) { try { Thread.sleep(millis); } catch (InterruptedException ignored) {} }

	@Override
	public void onInitializeClient() {
        AtomicBoolean firstTick = new AtomicBoolean(true);
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

        if (!enableCheats) {
            config.autoChangeAllToSameColor = false;
            config.autoClickInOrder = false;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            if (firstTick.get()) {
                System.out.println("First tick");

                AtomicInteger tc = new AtomicInteger();
                new Thread(() -> {
                    while (true) {
                        safeSleep(10);
                        tc.set(tc.get()+1);
                        if (tc.get() >= 5_000) {
                            System.out.println("im alive");
                            if (mc.player instanceof ClientPlayerEntity player)
                                System.out.println(player.currentScreenHandler instanceof ScreenHandler);
                            else System.out.println("mc.player: false");
                            System.out.println(mc.currentScreen instanceof Screen);
                            tc.set(0);
                        }
                        AtomicReference<String> lastScreen = new AtomicReference<>("none");
                        if (mc.player instanceof ClientPlayerEntity player
                                && player.currentScreenHandler instanceof ScreenHandler handler
                                && mc.currentScreen instanceof Screen screen) {
                            AtomicReference<String> title = new AtomicReference<>();
                            mc.execute(() -> title.set(screen.getTitle().getString()));
                            if (title.get() instanceof String realTitle && !lastScreen.get().equals(realTitle)) {
                                lastScreen.set(realTitle);
                                safeSleep(100);
                                if (realTitle.equals("Click in order!") && config.autoClickInOrder) {
//                                List<Integer> order = new ArrayList<>();
                                    mc.execute(() -> {
                                        mc.inGameHud.getChatHud().addMessage(Text.literal("Click in order found!"));
                                        for (int i = 10; i <= 16; i++) {
                                            Slot slot = handler.slots.get(i);
                                            ItemStack stack = slot.getStack();
                                            System.out.println("Slot " + i + ": " + stack.getName());
                                        }

                                        for (int i = 19; i <= 25; i++) {
                                            Slot slot = handler.slots.get(i);
                                            ItemStack stack = slot.getStack();
                                            System.out.println("Slot " + i + ": " + stack.getName());
                                        }
                                    });
//                                safeSleep(random.nextInt(400, 600));
//                                mc.execute(() -> handler.onSlotClick(10, 0, SlotActionType.THROW, mc.player););

                                } else if (realTitle.equals("Change all to same color!") && config.autoChangeAllToSameColor) {
                                    Map<Integer, Integer> offsetRequired = new HashMap<>();
                                    mc.execute(() -> {
                                        mc.inGameHud.getChatHud().addMessage(Text.literal("Change all to same color found!"));

                                        for (int i = 12; i <= 14; i++) {
                                            Slot slot = handler.slots.get(i);
                                            ItemStack stack = slot.getStack();
                                            System.out.println("Slot " + i + ": " + stack.getItem().toString());
                                            if (blueOffset.get(stack.getItem().toString()) instanceof Integer amount && amount != 0)
                                                offsetRequired.put(i, amount);
                                        }

                                        for (int i = 21; i <= 23; i++) {
                                            Slot slot = handler.slots.get(i);
                                            ItemStack stack = slot.getStack();
                                            System.out.println("Slot " + i + ": " + stack.getItem().toString());
                                            if (blueOffset.get(stack.getItem().toString()) instanceof Integer amount && amount != 0)
                                                offsetRequired.put(i, amount);
                                        }

                                        for (int i = 30; i <= 32; i++) {
                                            Slot slot = handler.slots.get(i);
                                            ItemStack stack = slot.getStack();
                                            System.out.println("Slot " + i + ": " + stack.getItem().toString());
                                            if (blueOffset.get(stack.getItem().toString()) instanceof Integer amount && amount != 0)
                                                offsetRequired.put(i, amount);
                                        }
                                    });
                                    System.out.println(offsetRequired);
                                }
                            }
                        }
                    }
                }).start();

                firstTick.set(false);
            }

            if (config.phoenixEnabled) {
                int width = mc.getWindow().getScaledWidth();

                boolean active = cooldownRemaining.get() != 0L;
                int colorToUse = active ? Color.DARK_BLUE : Color.GREEN;


                drawContext.fill(0, 0, width, 4, colorToUse);

                if (active)
                    drawContext.fill(0, 0, Math.round((cooldownRemaining.get() / 60f) * width), 4, Color.DARK_RED);
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, source) -> {
            dispatcher.register(ClientCommandManager.literal("kittymod")
                    .then(ClientCommandManager.literal("Toggles")
                            .then(ClientCommandManager.literal("PhoenixBar").executes(this::togglePhoenix))
                            .then(ClientCommandManager.literal("ShortChats").executes(this::toggleShortChats))
                            .then(ClientCommandManager.literal("RankHider").executes(this::toggleRankHider))
                            .then(ClientCommandManager.literal("ChatColors").executes(this::toggleChatColors))
                            .then(ClientCommandManager.literal("AutoMeow").executes(this::toggleAutoMeow))
                            .then(ClientCommandManager.literal("AutoWoof").executes(this::toggleAutoWoof))
                            .then(ClientCommandManager.literal("HideDiscordWarning").executes(this::toggleHideDiscordWarning))
                            .then(ClientCommandManager.literal("HideFireSale").executes(this::toggleHideFireSale))
                            .then(ClientCommandManager.literal("Debug").executes(this::toggleDebug)))
                    .then(ClientCommandManager.literal("Tests")
                            .then(ClientCommandManager.literal("RainbowMessage").executes(this::rainbowTest))
                            .then(ClientCommandManager.literal("YouAreCaredFor").executes(this::supportiveMessage))
                    )
                    .then(ClientCommandManager.literal("Cheats")
                            .then(enableCheats ? ClientCommandManager.literal("AutoTerms")
                                    .then(ClientCommandManager.literal("ClickInOrder").executes(this::toggleAutoClickInOrder))
                                    .then(ClientCommandManager.literal("ChangeAllToSameColor").executes(this::toggleAutoChangeAllToSameColor))
                             : ClientCommandManager.literal("lmao you thought"))
                    )
            );
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((text, ignored) -> {
            if (config.debug) System.out.println(text.getString());
            return (!config.disableWhitespaceEnabled || !List.of("  ", " ", "{\"text\":\"\",\"extra\":[\" \"],\"italic\":false}", "{\"text\":\"\",\"extra\":[\"  \"],\"italic\":false}").contains(gson.toJson(TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow())))
                    && (!config.hideFireSaleEnabled || !text.contains(Text.literal("§c§lFIRE SALE")));
        });

        ClientReceiveMessageEvents.MODIFY_GAME.register((text, ignored) -> {
            if (text.getString().equals("Your Phoenix Pet saved you from certain death!") && config.phoenixEnabled) {
                cooldownRemaining.set(60L);

                return Text.empty()
                        .append(Text.literal("Your ").setStyle(Color.toStyle(Color.YELLOW)))
                        .append(Text.literal("Phoenix Pet ").setStyle(Color.toStyle(Color.RED)))
                        .append(Text.literal("saved you from Epstein's Island! ").setStyle(Color.toStyle(Color.YELLOW)));
            }

            String jsonText = gson.toJson(TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow());

            if (config.autoMeowEnabled && mc.player != null && !text.getString().contains(mc.player.getGameProfile().name()) && text.getString().toLowerCase().endsWith("meow")) {
                new Thread(() -> {
                    safeSleep(random.nextInt(450, 650));
                    mc.execute(() -> mc.player.networkHandler.sendChatMessage(meows.get(random.nextInt(meows.size()))));
                }).start();
            }

            if (config.autoWoofEnabled && mc.player != null && !text.getString().contains(mc.player.getGameProfile().name()) && text.getString().toLowerCase().endsWith("woof")) {
                new Thread(() -> {
                    safeSleep(random.nextInt(450, 650));
                    mc.execute(() -> mc.player.networkHandler.sendChatMessage(woofs.get(random.nextInt(woofs.size()))));
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


        ClientTickEvents.START_CLIENT_TICK.register((client) -> {
            if (cooldownRemaining.get() != 0L) {
                long ct = System.currentTimeMillis();
                if (ct >= lt.get() + 1000L) {
                    lt.set(ct);
                    long remain = cooldownRemaining.get();
                    if (remain != 0) cooldownRemaining.set(remain - 1);
                }
            }
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

    private int toggleFeature(boolean currentValue, String featureName, Consumer<Boolean> setter) {
        setter.accept(!currentValue);
        if (MinecraftClient.getInstance() instanceof MinecraftClient mc)
            mc.inGameHud.getChatHud().addMessage(Text.literal(featureName + " is now " + (currentValue ? "disabled" : "enabled") + "."));
        saveConfig();
        return 1;
    }

    private int togglePhoenix(CommandContext<FabricClientCommandSource> ctx) {
        return toggleFeature(config.phoenixEnabled, "Phoenix Bar", val -> config.phoenixEnabled = val); }

    private int toggleRankHider(CommandContext<FabricClientCommandSource> ctx) {
        return toggleFeature(config.rankHiderEnabled, "Rank Hider", val -> config.rankHiderEnabled = val); }

    private int toggleShortChats(CommandContext<FabricClientCommandSource> ctx) {
        return toggleFeature(config.shortChatsEnabled, "Short Chats", val -> config.shortChatsEnabled = val); }

    private int toggleChatColors(CommandContext<FabricClientCommandSource> ctx) {
        return toggleFeature(config.chatColorsEnabled, "Chat Colors", val -> config.chatColorsEnabled = val); }

    private int toggleAutoMeow(CommandContext<FabricClientCommandSource> ctx) {
        return toggleFeature(config.autoMeowEnabled, "Auto Meow", val -> config.autoMeowEnabled = val); }

    private int toggleAutoWoof(CommandContext<FabricClientCommandSource> ctx) {
        return toggleFeature(config.autoWoofEnabled, "Auto Woof", val -> config.autoWoofEnabled = val); }

    private int toggleHideDiscordWarning(CommandContext<FabricClientCommandSource> ctx) {
        return toggleFeature(config.hideDiscordWarningEnabled, "Hide Discord server warning", val -> config.hideDiscordWarningEnabled = val); }

    private int toggleHideFireSale(CommandContext<FabricClientCommandSource> ctx) {
        return toggleFeature(config.hideFireSaleEnabled, "Hide Fire Sale message", val -> config.hideFireSaleEnabled = val); }

    private int toggleAutoClickInOrder(CommandContext<FabricClientCommandSource> ctx) {
        return toggleFeature(config.autoClickInOrder, "Auto Click in order!", val -> config.autoClickInOrder = val); }

    private int toggleAutoChangeAllToSameColor(CommandContext<FabricClientCommandSource> ctx) {
        return toggleFeature(config.autoChangeAllToSameColor, "Auto Change all to same color!", val -> config.autoChangeAllToSameColor = val); }

    private int toggleDebug(CommandContext<FabricClientCommandSource> ctx) {
        return toggleFeature(config.debug, "Debug", val -> config.debug = val); }
}