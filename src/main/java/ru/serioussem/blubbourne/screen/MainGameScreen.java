package ru.serioussem.blubbourne.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Json;
import ru.serioussem.blubbourne.BludBourne;
import ru.serioussem.blubbourne.Entity;
import ru.serioussem.blubbourne.EntityFactory;
import ru.serioussem.blubbourne.UI.PlayerHUD;
import ru.serioussem.blubbourne.component.Component;
import ru.serioussem.blubbourne.map.Map;
import ru.serioussem.blubbourne.map.MapFactory;
import ru.serioussem.blubbourne.map.MapManager;
import ru.serioussem.blubbourne.profile.ProfileManager;

public class MainGameScreen implements Screen {
    private static final String TAG = MainGameScreen.class.getSimpleName();
    private static final int VIEWPORT_WIDTH = 16;
    private static final int VIEWPORT_HEIGHT = 16;

    private static class VIEWPORT {
        static float viewportWidth;
        static float viewportHeight;
        static float virtualWidth;
        static float virtualHeight;
        static float physicalWidth;
        static float physicalHeight;
        static float aspectRatio;
    }

    public static enum GameState {
        SAVING,
        LOADING,
        RUNNING,
        PAUSED,
        GAME_OVER
    }
    private static GameState _gameState;

    protected OrthogonalTiledMapRenderer _mapRenderer = null;
    protected MapManager _mapMgr;
    protected OrthographicCamera _camera = null;
    protected OrthographicCamera _hudCamera = null;

    private Json _json;
    private BludBourne _game;
    private InputMultiplexer _multiplexer;

    private Entity _player;
    private PlayerHUD _playerHUD;

    public MainGameScreen(BludBourne game){
        _game = game;
        _mapMgr = new MapManager();
        _json = new Json();

        setGameState(GameState.RUNNING);

        //_camera setup
        setupViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

        //get the current size
        _camera = new OrthographicCamera();
        _camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

        _player = EntityFactory.getEntity(EntityFactory.EntityType.PLAYER);
        _mapMgr.setPlayer(_player);
        _mapMgr.setCamera(_camera);

        _hudCamera = new OrthographicCamera();
        _hudCamera.setToOrtho(false, VIEWPORT.physicalWidth, VIEWPORT.physicalHeight);

        _playerHUD = new PlayerHUD(_hudCamera, _player, _mapMgr);

        _multiplexer = new InputMultiplexer();
        _multiplexer.addProcessor(_playerHUD.getStage());
        _multiplexer.addProcessor(_player.getInputProcessor());
        Gdx.input.setInputProcessor(_multiplexer);

        //Gdx.app.debug(TAG, "UnitScale value is: " + _mapRenderer.getUnitScale());
    }

    @Override
    public void show() {
        ProfileManager.getInstance().addObserver(_mapMgr);
        ProfileManager.getInstance().addObserver(_playerHUD);

        setGameState(GameState.LOADING);
        Gdx.input.setInputProcessor(_multiplexer);


        if( _mapRenderer == null ){
            _mapRenderer = new OrthogonalTiledMapRenderer(_mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
        }
    }

    @Override
    public void hide() {
        if( _gameState != GameState.GAME_OVER ){
            setGameState(GameState.SAVING);
        }

        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        if( _gameState == GameState.GAME_OVER ){
//            _game.setScreen(_game.getScreenType(BludBourne.ScreenType.GameOver));
        }

        if( _gameState == GameState.PAUSED ){
            _player.updateInput(delta);
            _playerHUD.render(delta);
            return;
        }
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        _mapRenderer.setView(_camera);

        _mapRenderer.getBatch().enableBlending();
        _mapRenderer.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if( _mapMgr.hasMapChanged() ){
            _mapRenderer.setMap(_mapMgr.getCurrentTiledMap());
            _player.sendMessage(Component.MESSAGE.INIT_START_POSITION, _json.toJson(_mapMgr.getPlayerStartUnitScaled()));

            _camera.position.set(_mapMgr.getPlayerStartUnitScaled().x, _mapMgr.getPlayerStartUnitScaled().y, 0f);
            _camera.update();

//            _playerHUD.updateEntityObservers();

            _mapMgr.setMapChanged(false);

//            _playerHUD.addTransitionToScreen();
        }

//        _mapMgr.updateLightMaps(_playerHUD.getCurrentTimeOfDay());
        TiledMapImageLayer lightMap = (TiledMapImageLayer)_mapMgr.getCurrentLightMapLayer();
        TiledMapImageLayer previousLightMap = (TiledMapImageLayer)_mapMgr.getPreviousLightMapLayer();

        if( lightMap != null) {
            _mapRenderer.getBatch().begin();
            TiledMapTileLayer backgroundMapLayer = (TiledMapTileLayer)_mapMgr.getCurrentTiledMap().getLayers().get(Map.BACKGROUND_LAYER);
            if( backgroundMapLayer != null ){
                _mapRenderer.renderTileLayer(backgroundMapLayer);
            }

            TiledMapTileLayer groundMapLayer = (TiledMapTileLayer)_mapMgr.getCurrentTiledMap().getLayers().get(Map.GROUND_LAYER);
            if( groundMapLayer != null ){
                _mapRenderer.renderTileLayer(groundMapLayer);
            }

            TiledMapTileLayer decorationMapLayer = (TiledMapTileLayer)_mapMgr.getCurrentTiledMap().getLayers().get(Map.DECORATION_LAYER);
            if( decorationMapLayer != null ){
                _mapRenderer.renderTileLayer(decorationMapLayer);
            }

            _mapRenderer.getBatch().end();

            _mapMgr.updateCurrentMapEntities(_mapMgr, _mapRenderer.getBatch(), delta);
            _player.update(_mapMgr, _mapRenderer.getBatch(), delta);
            _mapMgr.updateCurrentMapEffects(_mapMgr, _mapRenderer.getBatch(), delta);

            _mapRenderer.getBatch().begin();
            _mapRenderer.getBatch().setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE_MINUS_SRC_ALPHA);

            _mapRenderer.renderImageLayer(lightMap);
            _mapRenderer.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            _mapRenderer.getBatch().end();

            if( previousLightMap != null ){
                _mapRenderer.getBatch().begin();
                _mapRenderer.getBatch().setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE_MINUS_SRC_COLOR);
                _mapRenderer.renderImageLayer(previousLightMap);
                _mapRenderer.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                _mapRenderer.getBatch().end();
            }
        }else{
            _mapRenderer.render();
            _mapMgr.updateCurrentMapEntities(_mapMgr, _mapRenderer.getBatch(), delta);
            _player.update(_mapMgr, _mapRenderer.getBatch(), delta);
            _mapMgr.updateCurrentMapEffects(_mapMgr, _mapRenderer.getBatch(), delta);
        }

        _playerHUD.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        setupViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        _camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);
        _playerHUD.resize((int) VIEWPORT.physicalWidth, (int) VIEWPORT.physicalHeight);
    }

    @Override
    public void pause() {
        setGameState(GameState.SAVING);
        _playerHUD.pause();
    }

    @Override
    public void resume() {
        setGameState(GameState.LOADING);
        _playerHUD.resume();
    }

    @Override
    public void dispose() {
        if( _player != null ){
//            _player.unregisterObservers();
            _player.dispose();
        }

        if( _mapRenderer != null ){
            _mapRenderer.dispose();
        }

//        AudioManager.getInstance().dispose();
//        MapFactory.clearCache();
    }

    public static void setGameState(GameState gameState){
        switch(gameState){
            case RUNNING:
                _gameState = GameState.RUNNING;
                break;
            case LOADING:
                ProfileManager.getInstance().loadProfile();
                _gameState = GameState.RUNNING;
                break;
            case SAVING:
                ProfileManager.getInstance().saveProfile();
                _gameState = GameState.PAUSED;
                break;
            case PAUSED:
                if( _gameState == GameState.PAUSED ){
                    _gameState = GameState.RUNNING;
                }else if( _gameState == GameState.RUNNING ){
                    _gameState = GameState.PAUSED;
                }
                break;
            case GAME_OVER:
                _gameState = GameState.GAME_OVER;
                break;
            default:
                _gameState = GameState.RUNNING;
                break;
        }

    }

    private void setupViewport(int width, int height) {

        //Make the viewport a percentage of the total display area
        VIEWPORT.virtualWidth = width;
        VIEWPORT.virtualHeight = height;

        //Current viewport dimensions
        VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
        VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;

        //pixel dimensions of display
        VIEWPORT.physicalWidth = Gdx.graphics.getWidth();
        VIEWPORT.physicalHeight = Gdx.graphics.getHeight();

        //aspect ratio for current viewport
        VIEWPORT.aspectRatio = (VIEWPORT.virtualWidth / VIEWPORT.virtualHeight);

        //update viewport if there could be skewing
        if (VIEWPORT.physicalWidth / VIEWPORT.physicalHeight >= VIEWPORT.aspectRatio) {
            //Letterbox left and right
            VIEWPORT.viewportWidth = VIEWPORT.viewportHeight * (VIEWPORT.physicalWidth / VIEWPORT.physicalHeight);
            VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;
        } else {
            //letterbox above and below
            VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
            VIEWPORT.viewportHeight = VIEWPORT.viewportWidth * (VIEWPORT.physicalHeight / VIEWPORT.physicalWidth);
        }
        Gdx.app.debug(TAG, "WorldRenderer: virtual: (" + VIEWPORT.virtualWidth + "," + VIEWPORT.virtualHeight + ")");
        Gdx.app.debug(TAG, "WorldRenderer: viewport: (" + VIEWPORT.viewportWidth + "," + VIEWPORT.viewportHeight + ")");
        Gdx.app.debug(TAG, "WorldRenderer: physical: (" + VIEWPORT.physicalWidth + "," + VIEWPORT.physicalHeight + ")");
    }
}
