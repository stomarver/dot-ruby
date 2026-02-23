package SwordsGame.client.core;

import java.util.EnumMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class HotkeyManager {
    private final Map<HotkeyAction, Integer> singleKeyBindings = new EnumMap<>(HotkeyAction.class);
    private final Map<HotkeyAction, Boolean> pressedState = new EnumMap<>(HotkeyAction.class);
    private final Map<String, Boolean> comboPressedState = new java.util.HashMap<>();

    public HotkeyManager() {
        bind(HotkeyAction.TOGGLE_VIRTUAL_RES, GLFW_KEY_F7);
        bind(HotkeyAction.TOGGLE_DEBUG_INFO, GLFW_KEY_F8);
        bind(HotkeyAction.TOGGLE_CHUNK_BOUNDS, GLFW_KEY_B);
        bind(HotkeyAction.FOG_PLUS, GLFW_KEY_KP_ADD);
        bind(HotkeyAction.FOG_MINUS, GLFW_KEY_KP_SUBTRACT);
    }

    public void bind(HotkeyAction action, int glfwKey) {
        singleKeyBindings.put(action, glfwKey);
        pressedState.putIfAbsent(action, false);
    }

    public boolean isDown(long windowHandle, HotkeyAction action) {
        Integer key = singleKeyBindings.get(action);
        if (key == null) {
            return false;
        }
        return glfwGetKey(windowHandle, key) == GLFW_PRESS;
    }

    public boolean consumePress(long windowHandle, HotkeyAction action) {
        boolean down = isDown(windowHandle, action);
        boolean wasDown = pressedState.getOrDefault(action, false);
        pressedState.put(action, down);
        return down && !wasDown;
    }

    public boolean consumeComboPress(long windowHandle, int modifierKey, int mainKey) {
        String comboId = modifierKey + "+" + mainKey;
        boolean modifierDown = glfwGetKey(windowHandle, modifierKey) == GLFW_PRESS;
        boolean mainDown = glfwGetKey(windowHandle, mainKey) == GLFW_PRESS;
        boolean down = modifierDown && mainDown;
        boolean wasDown = comboPressedState.getOrDefault(comboId, false);
        comboPressedState.put(comboId, down);
        return down && !wasDown;
    }
}
