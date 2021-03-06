package net.launchers.mod.initializer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.launchers.mod.block.LauncherBlock;
import net.launchers.mod.block.abstraction.AbstractLauncherBlock;
import net.launchers.mod.loader.LLoader;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class LCommands
{
    private static final String LAUNCHER_ID = "l";
    private static final String P_LAUNCHER_ID = "p";
    private static final String E_LAUNCHER_ID = "e";
    public static void initialize(CommandDispatcher<CommandSource> dispatcher)
    {
        ArrayList<String> launchersList = new ArrayList<>();
        dispatcher.register(Commands.literal("lcalc")
                .then(Commands.argument("first", word()).suggests(suggestedStrings())
                        .executes(ctx ->
                        {
                            launchersList.add(getString(ctx, "first"));
                            return checkIdsAndPrintForce(launchersList, ctx);
                        })
                        .then(Commands.argument("second", word()).suggests(suggestedStrings())
                                .executes(ctx ->
                                        {
                                            launchersList.add(getString(ctx, "first"));
                                            launchersList.add(getString(ctx, "second"));
                                            return checkIdsAndPrintForce(launchersList, ctx);
                                        }
                                )
                                .then(Commands.argument("third", word()).suggests(suggestedStrings())
                                        .executes(ctx ->
                                                {
                                                    launchersList.add(getString(ctx, "first"));
                                                    launchersList.add(getString(ctx, "second"));
                                                    launchersList.add(getString(ctx, "third"));
                                                    return checkIdsAndPrintForce(launchersList, ctx);
                                                }
                                        )
                                        .then(Commands.argument("fourth", word()).suggests(suggestedStrings())
                                                .executes(ctx ->
                                                        {
                                                            launchersList.add(getString(ctx, "first"));
                                                            launchersList.add(getString(ctx, "second"));
                                                            launchersList.add(getString(ctx, "third"));
                                                            launchersList.add(getString(ctx, "fourth"));
                                                            return checkIdsAndPrintForce(launchersList, ctx);
                                                        }
                                                )
                                        )
                                )
                        )
                )
                .executes(ctx ->
                {
                    return checkIdsAndPrintForce(launchersList, ctx);
                })
        );
    }
    private static boolean areLauncherIdValid(ArrayList<String> ids)
    {
        for(String current : ids)
        {
            if(!current.equals("l") && !current.equals("p") && !current.equals("e"))
            {
                return false;
            }
        }
        return !ids.isEmpty();
    }

    private static float getStackForceByLauncherId(String id)
    {
        switch(id)
        {
            case LAUNCHER_ID:
                return ((AbstractLauncherBlock)LBlocks.LAUNCHER_BLOCK.get().getBlock()).stackMultiplier;
            case P_LAUNCHER_ID:
                return ((AbstractLauncherBlock)LBlocks.POWERED_LAUNCHER_BLOCK.get().getBlock()).stackMultiplier;
            case E_LAUNCHER_ID:
                return ((AbstractLauncherBlock)LBlocks.EXTREME_LAUNCHER_BLOCK.get().getBlock()).stackMultiplier;
            default:
                return 0F;
        }
    }

    private static float getBaseForceByLauncherId(String id)
    {
        switch(id)
        {
            case LAUNCHER_ID:
                return ((AbstractLauncherBlock)LBlocks.LAUNCHER_BLOCK.get().getBlock()).baseMultiplier;
            case P_LAUNCHER_ID:
                return ((AbstractLauncherBlock)LBlocks.POWERED_LAUNCHER_BLOCK.get().getBlock()).baseMultiplier;
            case E_LAUNCHER_ID:
                return ((AbstractLauncherBlock)LBlocks.EXTREME_LAUNCHER_BLOCK.get().getBlock()).baseMultiplier;
            default:
                return 0F;
        }
    }

    private static float getForce(ArrayList<String> ids)
    {
        float base = getBaseForceByLauncherId(ids.get(0));
        float multipier = 1F;
        for(int i = 1; i < ids.size(); i++)
        {
            multipier += getStackForceByLauncherId(ids.get(i));
        }
        return base * multipier;
    }

    private static int checkIdsAndPrintForce(ArrayList<String> launchersList, CommandContext<CommandSource> context)
    {
        if(!areLauncherIdValid(launchersList))
        {
            context.getSource().sendFailure((new StringTextComponent("\nOne or more parameters are not correct.\n" +
                    "Usage:\n" +
                    "l: Launcher\n" +
                    "p: Powered Launcher\n" +
                    "e: Extreme Launcher\n")));
            launchersList.clear();
            return 0;
        }
        String printString = "\nStack: \n";
        for(int i = 0; i < launchersList.size(); i++)
        {
            printString += launchersList.get(i) + "\n";
        }
        printString += "Force: " + getForce(launchersList);
        context.getSource().sendSuccess(new StringTextComponent(printString), false);
        launchersList.clear();
        return 1;
    }

    public static SuggestionProvider<CommandSource> suggestedStrings()
    {
        ArrayList<String> suggestions = new ArrayList<>();
        suggestions.add(LAUNCHER_ID);
        suggestions.add(P_LAUNCHER_ID);
        suggestions.add(E_LAUNCHER_ID);
        return (ctx, builder) -> getSuggestionsBuilder(builder, suggestions);
    }

    private static CompletableFuture<Suggestions> getSuggestionsBuilder(SuggestionsBuilder builder, List<String> list)
    {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

        if(list.isEmpty())
        { // If the list is empty then return no suggestions
            return Suggestions.empty(); // No suggestions
        }

        for(String str : list)
        { // Iterate through the supplied list
            if(str.toLowerCase(Locale.ROOT).startsWith(remaining))
            {
                builder.suggest(str); // Add every single entry to suggestions list.
            }
        }
        return builder.buildFuture(); // Create the CompletableFuture containing all the suggestions
    }
}

