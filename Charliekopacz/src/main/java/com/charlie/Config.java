package com.charlie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Config {
    public static boolean enabled = false;
    public static boolean autoRejoin = false;
    public static String lastServerAddress = "";
    public static int blockLimit = 6;
    public static boolean headMovement = false;
    public static boolean autoDrop = false;
    public static MiningMode miningMode = MiningMode.LINIA;
    
    // Auto Drop Settings
    public static int dropIntervalMinutes = 2;
    public static Set<Integer> slotsToDrop = new HashSet<>();
    
    // Command Settings
    public static List<AutoCommand> autoCommands = new ArrayList<>();
    
    public enum MiningMode {
        LINIA, KWADRAT
    }
    
    public static class AutoCommand {
        public String command;
        public int intervalMinutes;
        public long lastExecutionTime;
        
        public AutoCommand(String command, int intervalMinutes) {
            this.command = command;
            this.intervalMinutes = intervalMinutes;
            this.lastExecutionTime = 0;
        }
    }

    // Statistics
    public static int cycles = 0;
    public static long startTime = 0;
    public static long totalTimeMillis = 0;

    static {
        // Default empty slots for commands
        for (int i = 0; i < 5; i++) {
            autoCommands.add(new AutoCommand("", 1));
        }
    }
}
