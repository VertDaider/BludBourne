package ru.serioussem.blubbourne;

import com.badlogic.gdx.Game;
import ru.serioussem.blubbourne.screen.MainGameScreen;

public class BludBourne extends Game {
    private static MainGameScreen _mainGameScreen;

    @Override
    public void create() {
        _mainGameScreen = new MainGameScreen(this);
        setScreen(_mainGameScreen);
    }

    @Override
    public void dispose() {
        _mainGameScreen.dispose();
    }
}
