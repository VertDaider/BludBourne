package ru.serioussem.blubbourne.component.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import ru.serioussem.blubbourne.Entity;
import ru.serioussem.blubbourne.map.Map;
import ru.serioussem.blubbourne.map.MapManager;
import ru.serioussem.blubbourne.component.Component;
import ru.serioussem.blubbourne.component.PhysicsComponent;

public class PlayerPhysicsComponent extends PhysicsComponent {
    private static final String TAG = PlayerPhysicsComponent.class.getSimpleName();
    private Entity.State _state;
    private Vector3 _mouseSelectCoordinates;
    private boolean _isMouseSelectEnabled = false;
    private String _previousDiscovery;
    private String _previousEnemySpawn;

    private Ray _selectionRay;
    private float _selectRayMaximumDistance = 32.0f;

    public PlayerPhysicsComponent() {
        _mouseSelectCoordinates = new Vector3(0, 0, 0);
        _selectionRay = new Ray(new Vector3(), new Vector3());
    }

    @Override
    public void receiveMessage(String message) {
        String[] strings = message.split(Component.MESSAGE_TOKEN);

        if (strings.length == 0)
            return;

        //Specifically for messages with 1 object payload
        if (strings.length == 2) {
            if (strings[0].equalsIgnoreCase(MESSAGE.INIT_START_POSITION.toString())) {
                _currentEntityPosition = _json.fromJson(Vector2.class, strings[1]);
                _nextEntityPosition.set(_currentEntityPosition.x, _currentEntityPosition.y);
            } else if (strings[0].equalsIgnoreCase(MESSAGE.CURRENT_STATE.toString())) {
                _state = _json.fromJson(Entity.State.class, strings[1]);
            } else if (strings[0].equalsIgnoreCase(MESSAGE.CURRENT_DIRECTION.toString())) {
                _currentDirection = _json.fromJson(Entity.Direction.class, strings[1]);
            } else if (strings[0].equalsIgnoreCase(MESSAGE.INIT_SELECT_ENTITY.toString())) {
                _mouseSelectCoordinates = _json.fromJson(Vector3.class, strings[1]);
                _isMouseSelectEnabled = true;
            }
        }
    }

    private void selectMapEntityCandidate(MapManager mapMgr) {
        Array<Entity> currentEntities = mapMgr.getCurrentMapEntities();
        //Convert screen coordinates to world coordinates,
        //then to unit scale coordinates
        mapMgr.getCamera().unproject(_mouseSelectCoordinates);
        _mouseSelectCoordinates.x /= Map.UNIT_SCALE;
        _mouseSelectCoordinates.y /= Map.UNIT_SCALE;

        for (Entity mapEntity : currentEntities) {
            //Don't break, reset all entities
            mapEntity.sendMessage(MESSAGE.ENTITY_DESELECTED);
            Rectangle mapEntityBoundingBox = mapEntity.getCurrentBoundingBox();
            if (mapEntity.getCurrentBoundingBox().contains(_mouseSelectCoordinates.x, _mouseSelectCoordinates.y)) {
                //Check distance
                _selectionRay.set(_boundingBox.x, _boundingBox.y, 0.0f, mapEntityBoundingBox.x, mapEntityBoundingBox.y, 0.0f);
                float distance = _selectionRay.origin.dst(_selectionRay.direction);
                if (distance <= _selectRayMaximumDistance) {
                    //We have a valid entity selection Picked/Selected
                    Gdx.app.debug(TAG, "Selected Entity! " + mapEntity.getEntityConfig().getEntityID());
                    mapEntity.sendMessage(MESSAGE.ENTITY_SELECTED);
                }
            }
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void update(Entity entity, MapManager mapMgr, float delta) {
        //We want the hitbox to be at the feet for a better feel
        updateBoundingBoxPosition(_nextEntityPosition);
        updatePortalLayerActivation(mapMgr);
        if (_isMouseSelectEnabled) {
            selectMapEntityCandidate(mapMgr);
            _isMouseSelectEnabled = false;
        }
        if (isCollisionWithMapLayer(entity, mapMgr) && isCollisionWithMapEntities(entity, mapMgr) && _state == Entity.State.WALKING) {
            setNextPositionToCurrent(entity);

            Camera camera = mapMgr.getCamera();
            camera.position.set(_currentEntityPosition.x, _currentEntityPosition.y, 0f);
            camera.update();
        } else {
            updateBoundingBoxPosition(_currentEntityPosition);
        }
        calculateNextPosition(delta);
    }

    private void updatePortalLayerActivation(MapManager mapMgr) {

    }
}
