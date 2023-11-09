package ru.serioussem.blubbourne.component;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import ru.serioussem.blubbourne.Entity;
import ru.serioussem.blubbourne.map.Map;
import ru.serioussem.blubbourne.map.MapManager;

public abstract class PhysicsComponent implements Component {
    private static final String TAG = PhysicsComponent.class.getSimpleName();

    protected Vector2 _nextEntityPosition;
    protected Vector2 _currentEntityPosition;
    protected Entity.Direction _currentDirection;
    protected Json _json;
    protected Vector2 _velocity;
    public Rectangle _boundingBox;
    protected BoundingBoxLocation _boundingBoxLocation;

    public static enum BoundingBoxLocation {
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        CENTER,
    }

    public PhysicsComponent() {
    }

    protected boolean isCollisionWithMapEntities(Entity entity, MapManager mapMgr) {
        Array<Entity> entities = mapMgr.getCurrentMapEntities();
        boolean isCollisionWithMapEntities = false;

        for (Entity mapEntity : entities) {
            // Check for testing against self
            if (mapEntity.equals(entity))
                continue;
            Rectangle targetRect = mapEntity.getCurrentBoundingBox();
            if (_boundingBox.overlaps(targetRect)) {
                entity.sendMessage(MESSAGE.COLLISION_WITH_ENTITY);
                isCollisionWithMapEntities = true;
                break;
            }
        }
        return isCollisionWithMapEntities;
    }

    protected boolean isCollision(Entity entitySource, Entity entityTarget) {
        boolean isCollisionWithMapEntities = false;
        if (entitySource.equals(entityTarget))
            return false;
        if (entitySource.getCurrentBoundingBox().overlaps(entityTarget.getCurrentBoundingBox())) {
            entitySource.sendMessage(MESSAGE.COLLISION_WITH_ENTITY);
            isCollisionWithMapEntities = true;
        }
        return isCollisionWithMapEntities;
    }

    protected boolean isCollisionWithMapLayer(Entity entity, MapManager mapMgr){
            return false;
    }
    protected void setNextPositionToCurrent(Entity entity){

    }
    protected void calculateNextPosition(float deltaTime){

    }

    protected void initBoundingBox(float percentageWidthReduced, float percentageHeightReduced){
        //Update the current bounding box
        float width;
        float height;

        float origWidth =  Entity.FRAME_WIDTH;
        float origHeight = Entity.FRAME_HEIGHT;

        float widthReductionAmount = 1.0f - percentageWidthReduced; //.8f for 20% (1 - .20)
        float heightReductionAmount = 1.0f - percentageHeightReduced; //.8f for 20% (1 - .20)

        if( widthReductionAmount > 0 && widthReductionAmount < 1){
            width = Entity.FRAME_WIDTH * widthReductionAmount;
        }else{
            width = Entity.FRAME_WIDTH;
        }

        if( heightReductionAmount > 0 && heightReductionAmount < 1){
            height = Entity.FRAME_HEIGHT * heightReductionAmount;
        }else{
            height = Entity.FRAME_HEIGHT;
        }

        if( width == 0 || height == 0){
            Gdx.app.debug(TAG, "Width and Height are 0!! " + width + ":" + height);
        }

        //Need to account for the unitscale, since the map coordinates will be in pixels
        float minX;
        float minY;

        if( Map.UNIT_SCALE > 0 ) {
            minX = _nextEntityPosition.x / Map.UNIT_SCALE;
            minY = _nextEntityPosition.y / Map.UNIT_SCALE;
        }else{
            minX = _nextEntityPosition.x;
            minY = _nextEntityPosition.y;
        }

        _boundingBox.setWidth(width);
        _boundingBox.setHeight(height);

        switch(_boundingBoxLocation){
            case BOTTOM_LEFT:
                _boundingBox.set(minX, minY, width, height);
                break;
            case BOTTOM_CENTER:
                _boundingBox.setCenter(minX + origWidth/2, minY + origHeight/4);
                break;
            case CENTER:
                _boundingBox.setCenter(minX + origWidth/2, minY + origHeight/2);
                break;
        }

        //Gdx.app.debug(TAG, "SETTING Bounding Box for " + entity.getEntityConfig().getEntityID() + ": (" + minX + "," + minY + ")  width: " + width + " height: " + height);
    }

    protected void updateBoundingBoxPosition(Vector2 position) {
        //Need to account for the unitscale, since the map
        //coordinates will be in pixels
        float minX;
        float minY;
        if (Map.UNIT_SCALE > 0) {
            minX = position.x / Map.UNIT_SCALE;
            minY = position.y / Map.UNIT_SCALE;
        } else {
            minX = position.x;
            minY = position.y;
        }
        switch (_boundingBoxLocation) {
            case BOTTOM_LEFT:
                _boundingBox.set(minX, minY,
                        _boundingBox.getWidth(),
                        _boundingBox.getHeight());
                break;
            case BOTTOM_CENTER:
                _boundingBox.setCenter(
                        minX + Entity.FRAME_WIDTH / 2,
                        minY + Entity.FRAME_HEIGHT / 4);
                break;
            case CENTER:
                _boundingBox.setCenter(
                        minX + Entity.FRAME_WIDTH / 2,
                        minY + Entity.FRAME_HEIGHT / 2);
                break;
        }
    }


    @Override
    public void dispose() {

    }

    @Override
    public void receiveMessage(String message) {

    }

    public abstract void update(Entity entity, MapManager mapMgr, float delta);
}
