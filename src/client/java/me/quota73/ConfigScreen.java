package me.quota73;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.option.SpruceToggleBooleanOption;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.tooltip.TooltipData;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import dev.lambdaurora.spruceui.widget.container.tabbed.SpruceTabbedWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class ConfigScreen extends SpruceScreen {
    private final @Nullable Screen parent;

    public ConfigScreen(@Nullable Screen parent) {
        super(Text.literal("KittyMod Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        SpruceTabbedWidget tabbedWidget = new SpruceTabbedWidget(Position.of(this, 0, 4), this.width, this.height - 35 - 4, this.title);
        tabbedWidget.addTabEntry(Text.literal("Haiii :3"), null, (width, height) -> {
            var container = new SpruceContainerWidget(Position.origin(), width, height);
            container.addChildren((containerWidth, containerHeight, widgetAdder) -> {
                widgetAdder.accept(new SpruceLabelWidget(Position.of(0, 16),
                        Text.literal("""
                                 Hii!§6 Thank you§f for using§b KittyMod§f <3
                                \s
                                 I develop this mod in my free time, yada yada yada,§d meow meow meow§f, you know the spiel.
                                 Whoever you are, because you're using this mod, you are now a§d catgirl§f. hehe :3
                                \s
                                 If you look to your left, you can see why you're actually here (or at least, why I§o think§r you're here): to configure§b KittyMod§f!"""),
                        containerWidth));
            });
            return container;
        });
        tabbedWidget.addSeparatorEntry(Text.literal("Features"));

        tabbedWidget.addTabEntry(Text.literal("Chat"), Text.literal("Features that interact with the chat.").formatted(Formatting.GRAY),
                (width, height) -> {
                    var list = new SpruceOptionListWidget(Position.origin(), width, height);

                    list.addOptionEntry(new SpruceToggleBooleanOption("AutoMeow",
                            () -> KittyMod.config.autoMeowEnabled,
                            bee -> KittyMod.config.autoMeowEnabled = bee,
                            TooltipData.builder().text("Toggles AutoMeow, which automatically responds to any messages ending in meow that don't contain your username.").build()),
                            new SpruceToggleBooleanOption("AutoWoof",
                                    () -> KittyMod.config.autoWoofEnabled,
                                    bee -> KittyMod.config.autoWoofEnabled = bee,
                                    TooltipData.builder().text("Toggles AutoWoof, which automatically responds to any messages ending in woof that don't contain your username.").build()));

                    list.addOptionEntry(new SpruceToggleBooleanOption("Phoenix pet message",
                            () -> KittyMod.config.phoenixMessageEnabled,
                            bee -> KittyMod.config.phoenixMessageEnabled = bee,
                            TooltipData.builder().text("Toggles the custom Phoenix pet proc message, changing it from saying it§e saved you from Certain Death§f to saying it§e saved you from Epstein's Island§f.").build()),
                            new SpruceToggleBooleanOption("Colored Chats",
                                    () -> KittyMod.config.chatColorsEnabled,
                                    bee -> KittyMod.config.chatColorsEnabled = bee,
                                    TooltipData.builder().text("Toggles chat colors, which turns things like &6 into §6color codes§f. This works on any server.").build()));

                    list.addOptionEntry(new SpruceToggleBooleanOption("Rank hider",
                            () -> KittyMod.config.rankHiderEnabled,
                            bee -> KittyMod.config.rankHiderEnabled = bee,
                            TooltipData.builder().text("Hides player ranks in Hypixel's chats. This also works on Fakepixel.").build()),
                            new SpruceToggleBooleanOption("Hide Discord warning",
                                    () -> KittyMod.config.hideDiscordWarningEnabled,
                                    bee -> KittyMod.config.hideDiscordWarningEnabled = bee,
                                    TooltipData.builder().text("Hides §cPlease be mindful of Discord links in chat as they may pose a security risk§f from the chat.").build()));

                    list.addOptionEntry(new SpruceToggleBooleanOption("Shorter chat types",
                            () -> KittyMod.config.shortChatsEnabled,
                            bee -> KittyMod.config.shortChatsEnabled = bee,
                            TooltipData.builder().text("Shortens Hypixel's chat types, for example: '§9Party§8 > §f' would become '§7[§9PC§7] '§r. This also works on Fakepixel.").build()),
                            new SpruceToggleBooleanOption("Disable empty lines",
                                    () -> KittyMod.config.disableWhitespaceEnabled,
                                    bee -> KittyMod.config.disableWhitespaceEnabled = bee,
                                    TooltipData.builder().text("Hides empty lines and excess whitespace from the chat. This works on any server, and may also function as a chat clear bypass.").build()));

                    list.addOptionEntry(new SpruceToggleBooleanOption("Hide Fire Sales",
                            () -> KittyMod.config.hideFireSaleEnabled,
                            bee -> KittyMod.config.hideFireSaleEnabled = bee,
                            TooltipData.builder().text("Hides Hypixel's §c§l§kA §c§lFIRE SALE §kA§f message from the chat.").build()), null);

                    return list;
                });

        tabbedWidget.addTabEntry(Text.literal("Visual"), Text.literal("Features that change what you see.").formatted(Formatting.GRAY),
                (width, height) -> {
                    var list = new SpruceOptionListWidget(Position.origin(), width, height);

                    list.addOptionEntry(new SpruceToggleBooleanOption("Phoenix pet cooldown bar",
                            () -> KittyMod.config.phoenixBarEnabled,
                            bee -> KittyMod.config.phoenixBarEnabled = bee,
                            TooltipData.builder().text("Adds a bar at the top of your screen to show the Phoenix pet's Rekindle ability cooldown.").build()),
                            new SpruceToggleBooleanOption("Glassorite",
                                    () -> KittyMod.config.Glassorite,
                                    bee -> KittyMod.config.Glassorite = bee,
                                    TooltipData.builder().text("Turns Diorite and Polished Diorite at the Storm phase of F7/M7 into Glass (client-side). This§o should§r work on any server, though it's locked to specific coordinates.").build()));

                    return list;
                });

        tabbedWidget.addTabEntry(Text.literal("Debug"), Text.literal("Quite literally that, it's just debugging options.").formatted(Formatting.GRAY),
                (width, height) -> {
            var list = new SpruceOptionListWidget(Position.origin(), width, height);

            list.addOptionEntry(new SpruceToggleBooleanOption("Chat debugging",
                    () -> KittyMod.config.debug,
                    bee -> KittyMod.config.debug = bee,
                    TooltipData.builder().text("Toggles the chat-related debug logging.").build()), null);

            return list;
        });

//        tabbedWidget.addTabEntry(Text.literal(""));


        this.addDrawableChild(tabbedWidget);

        this.addDrawableChild(new SpruceButtonWidget(Position.of(this, this.width / 2 - 75, this.height - 29), 150, 20, SpruceTexts.GUI_DONE,
                btn -> this.client.setScreen(this.parent)).asVanilla());
    }
}
