package ru.serioussem.blubbourne;

import com.badlogic.gdx.utils.Json;
import ru.serioussem.blubbourne.component.Component;
import ru.serioussem.blubbourne.component.impl.PlayerGraphicsComponent;
import ru.serioussem.blubbourne.component.impl.PlayerInputComponent;
import ru.serioussem.blubbourne.component.impl.PlayerPhysicsComponent;

import java.util.Hashtable;

public class EntityFactory {

    private static Json _json = new Json();
    private static EntityFactory _instance = null;
    private Hashtable<String, EntityConfig> _entities;

    public static enum EntityType {
        PLAYER,
        DEMO_PLAYER,
        NPC
    }

    public static String PLAYER_CONFIG = "scripts/player.json";

    static public Entity getEntity(EntityType entityType) {
        Entity entity = null;
        switch (entityType) {
            case PLAYER:
                entity = new Entity(new PlayerInputComponent(), new PlayerPhysicsComponent(), new PlayerGraphicsComponent());
                entity.setEntityConfig(Entity.getEntityConfig(EntityFactory.PLAYER_CONFIG));
                entity.sendMessage(Component.MESSAGE.LOAD_ANIMATIONS, _json.toJson(entity.getEntityConfig()));
                return entity;
            case DEMO_PLAYER:
                entity = new Entity(new NPCInputComponent(), new PlayerPhysicsComponent(), new PlayerGraphicsComponent());
                return entity;
            case NPC:
                entity = new Entity(new NPCInputComponent(), new NPCPhysicsComponent(), new NPCGraphicsComponent());
                return entity;
            default:
                return null;
        }
    }
}
