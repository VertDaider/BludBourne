package ru.serioussem.blubbourne.component;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Json;
import ru.serioussem.blubbourne.Entity;

import java.util.HashMap;
import java.util.Map;

public abstract class InputComponent implements Component, InputProcessor {
    protected Entity.Direction _currentDirection = null;
    protected Entity.State _currentState = null;
    protected Json _json;

    protected enum Keys {
        LEFT, RIGHT, UP, DOWN, QUIT
    }

    protected enum Mouse {
        SELECT, DOACTION
    }

    protected static Map<Keys, Boolean> keys = new HashMap<>();
    protected static Map<Mouse, Boolean> mouseButtons = new HashMap<>();

    //initialize the hashmap for inputs
    static {
        keys.put(Keys.LEFT, false);
        keys.put(Keys.RIGHT, false);
        keys.put(Keys.UP, false);
        keys.put(Keys.DOWN, false);
        keys.put(Keys.QUIT, false);
    }

    ;

    static {
        mouseButtons.put(Mouse.SELECT, false);
        mouseButtons.put(Mouse.DOACTION, false);
    }

    public InputComponent() {
        _json = new Json();
    }

    public abstract void update(Entity entity, float delta);

    @Override
    public void dispose() {

    }

    @Override
    public void receiveMessage(String message) {

    }
}
