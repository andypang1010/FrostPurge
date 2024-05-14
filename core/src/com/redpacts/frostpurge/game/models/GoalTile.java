package com.redpacts.frostpurge.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.redpacts.frostpurge.game.controllers.CollisionController;
import com.redpacts.frostpurge.game.util.FilmStrip;

/**
 * Class representing tiles in the game scene
 * Each tile has certain attributes that affects the game
 */
public class GoalTile extends TileModel{

    /** Type of the tile */
    private TileType type;
    private FilmStrip activeTexture;
    private boolean active;
    private String label;
    private boolean done;
    /** Scale from texture to screen */
    private float scale;
    float time;

    /**
     * Returns the texture of this tile
     *
     * @return texture of this tile
     */
    public Texture getTexture(){
        return this.texture;
    }

    /**
     * Returns the type of this tile
     *
     * @return type of this tile
     */
    public TileType getType(){
        return this.type;
    }
    public FilmStrip getFilmStrip() { return this.activeTexture;}
    public boolean isActive() {return this.active;}
    public void activate() {active = true;}
    public void deactivate() {active = false;}

    /**
     * Create a tile with the specified texture
     *
     * @param textureActive    The active texture of the tile
     */
    public GoalTile(TextureRegion textureActive, Vector2 position, String label, float scale, int base, int rows, int cols, int size){
        this.type = TileType.SWAMP;
        this.texture = new FilmStrip(textureActive.getTexture(), rows, cols, size).getTexture();
        this.activeTexture = new FilmStrip(textureActive.getTexture(), rows, cols, size);
        this.active = false;
        this.done = false;
        this.time = 0;
        this.label = label;
        this.position = position;
        this.scale = scale;
        this.base  = base;
    }
    protected void processFilmStrip() {
        if(!active || done) return;
        Filter filter = body.getFixtureList().first().getFilterData();
        filter.categoryBits = CollisionController.PhysicsConstants.CATEGORY_EMPTY;
        body.getFixtureList().first().setFilterData(filter);

        time += Gdx.graphics.getDeltaTime();
        int frame = activeTexture.getFrame();
        if (time >= .125){
            frame++;
            time = 0f;
            if (frame >= activeTexture.getSize()) {
                done = true;
                frame = 0;
            }
            activeTexture.setFrame(frame);
        }
    }

    @Override
    public void createBody(World world) {
        System.out.println("BODY CREATED");
        BodyDef bodyDef = new BodyDef();
        bodyDef.active = true;
        bodyDef.type = BodyDef.BodyType.StaticBody;

        PolygonShape shape = new PolygonShape();

        if(label.equals("big")) {
            // Set the position of the obstacle
            bodyDef.position.set(this.getPosition().cpy().add(64f * 8 / 2, 64f * 3 / 2).scl(0.1f));

            body = world.createBody(bodyDef);
            body.setUserData(this);
            shape.setAsBox(6.4f * 8 / 2, 6.4f * 1 / 2);
        } else {
            // Set the position of the obstacle
            bodyDef.position.set(this.getPosition().cpy().add(64f * 6 / 2, 64f * 3 / 2).scl(0.1f));

            body = world.createBody(bodyDef);
            body.setUserData(this);
            shape.setAsBox(6.4f * 6 / 2, 6.4f * 1 / 2);
        }
        this.shape = shape;

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        fixtureDef.filter.categoryBits = CollisionController.PhysicsConstants.CATEGORY_SWAMP;
        fixtureDef.filter.maskBits = CollisionController.PhysicsConstants.CATEGORY_PLAYER;

        body.createFixture(fixtureDef);
        shape.dispose();
    }
}
