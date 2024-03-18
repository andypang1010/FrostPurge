package com.redpacts.frostpurge.game.controllers;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.ai.fsm.*;
import com.badlogic.gdx.utils.Queue;

import com.badlogic.gdx.math.Vector2;
import com.redpacts.frostpurge.game.models.*;
import com.redpacts.frostpurge.game.util.EnemyStates;
import com.redpacts.frostpurge.game.util.TileGraph;
import com.redpacts.frostpurge.game.views.GameCanvas;


public class EnemyController extends CharactersController implements StateMachine<EnemyController, EnemyStates> {

    private Vector2 moveDirection = new Vector2();
    private float speedMultiplier = 1;
    /*
    FSM
    */
    PlayerModel playerModel;
    TileModel startPatrolTile, endPatrolTile;
    EnemyStates initState;
    EnemyStates currentState;
    EnemyStates prevState = null;

    /*
    PATHFINDING
    */
    MapModel board;
    TileGraph tileGraph;
    TileModel previousTile;
    Queue<TileModel> pathQueue = new Queue<>();

    EnemyController(EnemyModel enemy, PlayerModel targetPlayerModel, TileModel startPatrolTile, TileModel endPatrolTile, EnemyStates initState, TileGraph tileGraph, MapModel board) {
        this.model = enemy;
        playerModel = targetPlayerModel;
        this.startPatrolTile = startPatrolTile;
        this.endPatrolTile = endPatrolTile;
        setInitialState(initState);
        this.tileGraph = tileGraph;
        model.setPosition(startPatrolTile.cx, startPatrolTile.cy);
        previousTile = startPatrolTile;
        this.board = board;
    }

    public void setGoal(TileModel goalTile) {
        pathQueue.clear();
        GraphPath<TileModel> graphPath = tileGraph.findPath(previousTile, goalTile);
        for (int i = 1; i < graphPath.getCount(); i++) {
            pathQueue.addLast(graphPath.get(i));
//            System.out.println(graphPath.get(i).getCenter());
        }

        setMoveDirection();
    }

    private void checkCollision() {
        if (pathQueue.size > 0) {
            TileModel targetTile = pathQueue.first();
            if (Vector2.dst(model.getPosition().x, model.getPosition().y, targetTile.cx, targetTile.cy) < targetTile.getTexture().getWidth()) {
                reachNextTile();
            }
        }
    }

    private void reachNextTile() {
        this.previousTile = pathQueue.first();
        pathQueue.removeFirst();

        if (pathQueue.size == 0) {
            reachDestination();
        } else {
            setMoveDirection();
        }
    }

    private void reachDestination() {
        stop();
    }

    private void setMoveDirection() {
        if (pathQueue.notEmpty()) {
            TileModel nextTile = pathQueue.first();
            moveDirection = new Vector2(nextTile.cx - model.getPosition().x, model.getPosition().y - nextTile.cy).nor();
        }
    }


    @Override
    public void update() {
        switch (currentState) {
            case PATROL:
                System.out.println("PATROLLING");
                // Naive check for state transitions (If within certain distance, transition to chase state)
                if (Vector2.dst(playerModel.getPosition().x, playerModel.getPosition().y, model.getPosition().x, model.getPosition().y) > 100) {
                    if (Vector2.dst(startPatrolTile.cx, startPatrolTile.cy, model.getPosition().x, model.getPosition().y) < 5) {
                        setGoal(endPatrolTile);
                    }
                    else {
                        setGoal(startPatrolTile);
                    }
                }
                else {
                    changeState(EnemyStates.CHASE);
                }
                break;
            case CHASE:
                System.out.println("CHASING: " + modelPositionToTile(playerModel).getCenter().toString());
                setGoal(modelPositionToTile(playerModel));
                break;
        };

        moveToNextTile();
        checkCollision();
    }

    private void moveToNextTile() {
        Vector2 vel = moveDirection;
        vel.scl(1);
        accelerate(vel.x, vel.y);

        Vector2 newLocation = model.getPosition().add(model.getVelocity());
        model.setPosition(newLocation.x, newLocation.y);
    }

    public void draw(GameCanvas canvas){
        model.drawCharacter(canvas, (float) Math.toDegrees(model.getRotation()), Color.RED, "idle", false);
    }

    private TileModel modelPositionToTile(CharactersModel model) {
        return board.getTileState(board.screenToBoard(model.getPosition().x), board.screenToBoard(model.getPosition().y));
    }

    @Override
    public void changeState(EnemyStates enemyState) {
        prevState = currentState;
        currentState = enemyState;
    }

    public boolean revertToPreviousState() {
        if (prevState == null) {
            return false;
        }

        currentState = prevState;
        return true;
    }

    @Override
    public void setInitialState(EnemyStates enemyState) {
        initState = enemyState;
        currentState = enemyState;
    }

    public void setGlobalState(EnemyStates enemyState) {

    }

    @Override
    public EnemyStates getCurrentState() {
        return currentState;
    }

    public EnemyStates getGlobalState() {
        return null;
    }

    @Override
    public EnemyStates getPreviousState() {
        return prevState;
    }

    @Override
    public boolean isInState(EnemyStates enemyState) {
        return currentState == enemyState;
    }

    public boolean handleMessage(Telegram telegram) {
        return false;
    }

}
