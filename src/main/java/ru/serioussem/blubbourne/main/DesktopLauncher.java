package ru.serioussem.blubbourne.main;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ru.serioussem.blubbourne.BludBourne;

public class DesktopLauncher {
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.title = "BludBourne";
        config.useGL30 = false;
        config.width = 1200;
        config.height = 900;
        config.samples = 3;
        Gdx.app = new LwjglApplication(new BludBourne(), config);
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
    }
}
