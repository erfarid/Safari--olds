package safariolds.controller;

import java.util.ArrayList;
import java.util.List;

public class State {
    private static GameMode currentMode = GameMode.DAY;
    private static final List<SpeedListener> listeners = new ArrayList<>();
    
    private State() {} // Prevent instantiation
    
    public static void setMode(GameMode mode) {
        if (mode != currentMode) {
            currentMode = mode;
            notifyListeners();
            System.out.println("Game speed changed to: " + mode);
        }
    }
    
    public static GameMode getMode() {
        return currentMode;
    }
    
    public static void addListener(SpeedListener listener) {
        listeners.add(listener);
    }
    
    public static void removeListener(SpeedListener listener) {
        listeners.remove(listener);
    }
    
    private static void notifyListeners() {
        for (SpeedListener listener : listeners) {
            listener.speedChanged(currentMode);
        }
    }
    
    public interface SpeedListener {
        void speedChanged(GameMode newMode);
    }
}