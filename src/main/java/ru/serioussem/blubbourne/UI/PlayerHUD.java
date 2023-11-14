package ru.serioussem.blubbourne.UI;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ru.serioussem.blubbourne.Entity;
import ru.serioussem.blubbourne.UI.inventory.InventoryItem.ItemTypeID;
import ru.serioussem.blubbourne.UI.inventory.InventoryItemLocation;
import ru.serioussem.blubbourne.UI.inventory.InventoryUI;
import ru.serioussem.blubbourne.map.MapManager;
import ru.serioussem.blubbourne.profile.ProfileManager;
import ru.serioussem.blubbourne.profile.ProfileObserver;

public class PlayerHUD implements Screen, ProfileObserver {

    private static final String TAG = PlayerHUD.class.getSimpleName();

    private Stage _stage;
    private Viewport _viewport;
    private Camera _camera;
    private Entity _player;

    private StatusUI _statusUI;
    private InventoryUI _inventoryUI;
//    private ConversationUI _conversationUI;
//    private StoreInventoryUI _storeInventoryUI;
//    private QuestUI _questUI;
//    private BattleUI _battleUI;

    private Dialog _messageBoxUI;
    private Json _json;
    private MapManager _mapMgr;

//    private Array<AudioObserver> _observers;
//    private ScreenTransitionActor _transitionActor;

//    private ShakeCamera _shakeCam;
//    private ClockActor _clock;

    private static final String INVENTORY_FULL = "Your inventory is full!";


    public PlayerHUD(Camera camera, Entity player, MapManager mapMgr) {
        _camera = camera;
        _player = player;
        _mapMgr = mapMgr;
        _viewport = new ScreenViewport(_camera);
        _stage = new Stage(_viewport);

        _statusUI = new StatusUI();
        _inventoryUI = new InventoryUI();

        _stage.addActor(_statusUI);
        _stage.addActor(_inventoryUI);
    }

    @Override
    public void onNotify(ProfileManager profileManager, ProfileEvent event) {
        switch (event) {
            case PROFILE_LOADED:
                boolean firstTime = profileManager.getIsNewProfile();

                if (firstTime) {
                    InventoryUI.clearInventoryItems(_inventoryUI.getInventorySlotTable());
                    InventoryUI.clearInventoryItems(_inventoryUI.getEquipSlotTable());
                    _inventoryUI.resetEquipSlots();

//                    _questUI.setQuests(new Array<QuestGraph>());

                    //add default items if first time
                    Array<ItemTypeID> items = _player.getEntityConfig().getInventory();
                    Array<InventoryItemLocation> itemLocations = new Array<InventoryItemLocation>();
                    for (int i = 0; i < items.size; i++) {
                        itemLocations.add(new InventoryItemLocation(i, items.get(i).toString(), 1, InventoryUI.PLAYER_INVENTORY));
                    }
                    InventoryUI.populateInventory(_inventoryUI.getInventorySlotTable(), itemLocations, _inventoryUI.getDragAndDrop(), InventoryUI.PLAYER_INVENTORY, false);
                    profileManager.setProperty("playerInventory", InventoryUI.getInventory(_inventoryUI.getInventorySlotTable()));

                    //start the player with some money
//                    _statusUI.setGoldValue(20);
//                    _statusUI.setStatusForLevel(1);

//                    _clock.setTotalTime(60 * 60 * 12); //start at noon
//                    profileManager.setProperty("currentTime", _clock.getTotalTime());
                } else {
                    int goldVal = profileManager.getProperty("currentPlayerGP", Integer.class);

                    Array<InventoryItemLocation> inventory = profileManager.getProperty("playerInventory", Array.class);
                    InventoryUI.populateInventory(_inventoryUI.getInventorySlotTable(), inventory, _inventoryUI.getDragAndDrop(), InventoryUI.PLAYER_INVENTORY, false);

                    Array<InventoryItemLocation> equipInventory = profileManager.getProperty("playerEquipInventory", Array.class);
                    if (equipInventory != null && equipInventory.size > 0) {
                        _inventoryUI.resetEquipSlots();
                        InventoryUI.populateInventory(_inventoryUI.getEquipSlotTable(), equipInventory, _inventoryUI.getDragAndDrop(), InventoryUI.PLAYER_INVENTORY, false);
                    }

//                    Array<QuestGraph> quests = profileManager.getProperty("playerQuests", Array.class);
//                    _questUI.setQuests(quests);

                    int xpMaxVal = profileManager.getProperty("currentPlayerXPMax", Integer.class);
                    int xpVal = profileManager.getProperty("currentPlayerXP", Integer.class);

                    int hpMaxVal = profileManager.getProperty("currentPlayerHPMax", Integer.class);
                    int hpVal = profileManager.getProperty("currentPlayerHP", Integer.class);

                    int mpMaxVal = profileManager.getProperty("currentPlayerMPMax", Integer.class);
                    int mpVal = profileManager.getProperty("currentPlayerMP", Integer.class);

                    int levelVal = profileManager.getProperty("currentPlayerLevel", Integer.class);

                    //set the current max values first
                    _statusUI.setXPValueMax(xpMaxVal);
                    _statusUI.setHPValueMax(hpMaxVal);
                    _statusUI.setMPValueMax(mpMaxVal);

                    _statusUI.setXPValue(xpVal);
                    _statusUI.setHPValue(hpVal);
                    _statusUI.setMPValue(mpVal);

                    //then add in current values
                    _statusUI.setGoldValue(goldVal);
                    _statusUI.setLevelValue(levelVal);

                    float totalTime = profileManager.getProperty("currentTime", Float.class);
//                    _clock.setTotalTime(totalTime);
                }

                break;
            case SAVING_PROFILE:
//                profileManager.setProperty("playerQuests", _questUI.getQuests());
                profileManager.setProperty("playerInventory", InventoryUI.getInventory(_inventoryUI.getInventorySlotTable()));
                profileManager.setProperty("playerEquipInventory", InventoryUI.getInventory(_inventoryUI.getEquipSlotTable()));
                profileManager.setProperty("currentPlayerGP", _statusUI.getGoldValue());
                profileManager.setProperty("currentPlayerLevel", _statusUI.getLevelValue());
                profileManager.setProperty("currentPlayerXP", _statusUI.getXPValue());
                profileManager.setProperty("currentPlayerXPMax", _statusUI.getXPValueMax());
                profileManager.setProperty("currentPlayerHP", _statusUI.getHPValue());
                profileManager.setProperty("currentPlayerHPMax", _statusUI.getHPValueMax());
                profileManager.setProperty("currentPlayerMP", _statusUI.getMPValue());
                profileManager.setProperty("currentPlayerMPMax", _statusUI.getMPValueMax());
//                profileManager.setProperty("currentTime", _clock.getTotalTime());
                break;
            case CLEAR_CURRENT_PROFILE:
//                profileManager.setProperty("playerQuests", new Array<QuestGraph>());
                profileManager.setProperty("playerInventory", new Array<InventoryItemLocation>());
                profileManager.setProperty("playerEquipInventory", new Array<InventoryItemLocation>());
                profileManager.setProperty("currentPlayerGP", 0);
                profileManager.setProperty("currentPlayerLevel", 0);
                profileManager.setProperty("currentPlayerXP", 0);
                profileManager.setProperty("currentPlayerXPMax", 0);
                profileManager.setProperty("currentPlayerHP", 0);
                profileManager.setProperty("currentPlayerHPMax", 0);
                profileManager.setProperty("currentPlayerMP", 0);
                profileManager.setProperty("currentPlayerMPMax", 0);
                profileManager.setProperty("currentTime", 0);
                break;
            default:
                break;
        }
    }

    @Override
    public void show() {
//        _shakeCam.reset();
    }

    public Stage getStage() {
        return _stage;
    }

    @Override
    public void render(float delta) {
//        if( _shakeCam.isCameraShaking() ){
//            Vector2 shakeCoords = _shakeCam.getNewShakePosition();
//            _camera.position.x = shakeCoords.x + _stage.getWidth()/2;
//            _camera.position.y = shakeCoords.y + _stage.getHeight()/2;
//        }
        _stage.act(delta);
        _stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        _stage.getViewport().update(width, height, true);
//        _battleUI.validate();
//        _battleUI.resize();
    }

    @Override
    public void pause() {
//        _battleUI.resetDefaults();
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        _stage.dispose();
    }

}
