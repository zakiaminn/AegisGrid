package com.zakiaminahmad.aegisgrid;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.List;

/**
 * Handles enemy movement, wall-attacking logic, and SpriteSheet animations.
 * @author Zaki
 */
public class Enemy {
    public Vector2 position;
    private List<AStar.Node> path;
    private int currentPathIndex = 0;
    private float speed = 100f;

    public float maxHealth = 100f;
    public float currentHealth = 100f;
    public boolean reachedTarget = false;

    // Breach logic
    private float attackCooldown = 1.0f;
    private float attackTimer = 0f;
    private float attackDamage = 25f;

    // Animation state
    private float stateTime = 0f;
    private Animation<TextureRegion> walkAnimation;
    private TextureRegion currentFrame;

    public Enemy(float startX, float startY) {
        position = new Vector2(startX, startY);
    }

    public void setAnimation(TextureRegion[][] frames, float frameDuration) {
        walkAnimation = new Animation<>(frameDuration, frames[0]);
    }

    public Animation<TextureRegion> getWalkAnimation() {
        return walkAnimation;
    }

    public void setPath(List<AStar.Node> path) {
        this.path = path;
        this.currentPathIndex = 0;
    }

    public void takeDamage(float amount) {
        currentHealth -= amount;
        if (currentHealth < 0) currentHealth = 0;
    }

    public void update(float delta, int tileSize, Wall[][] map) {
        stateTime += delta;

        // Failsafe for missing path
        if (path == null || currentPathIndex >= path.size()) {
            reachedTarget = true;
            return;
        }

        AStar.Node targetNode = path.get(currentPathIndex);

        // Stop and attack if a player built a wall in front of us
        Wall targetWall = map[targetNode.x][targetNode.y];
        if (targetWall != null) {
            attackTimer += delta;
            if (attackTimer >= attackCooldown) {
                targetWall.takeDamage(attackDamage);
                attackTimer = 0f;
            }
            return; // Don't process movement this frame
        }

        // Vector movement towards the next path node
        float targetX = targetNode.x * tileSize;
        float targetY = targetNode.y * tileSize;
        float directionX = targetX - position.x;
        float directionY = targetY - position.y;
        float distance = (float) Math.sqrt(directionX * directionX + directionY * directionY);
        float moveDistance = speed * delta;

        if (distance < moveDistance) {
            position.x = targetX;
            position.y = targetY;
            currentPathIndex++;
        } else {
            position.x += (directionX / distance) * moveDistance;
            position.y += (directionY / distance) * moveDistance;
        }
    }

    public void draw(SpriteBatch batch, TextureRegion region, int tileSize) {
        currentFrame = (walkAnimation != null) ? walkAnimation.getKeyFrame(stateTime, true) : region;

        // Shadow offset
        batch.setColor(0f, 0f, 0f, 0.5f);
        batch.draw(currentFrame, position.x + 4, position.y - 4, tileSize, tileSize);

        // Main sprite
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(currentFrame, position.x, position.y, tileSize, tileSize);
    }

    public void drawHealthBar(ShapeRenderer shapeRenderer, int tileSize) {
        shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1);
        shapeRenderer.rect(position.x, position.y + tileSize + 2, tileSize, 6);

        shapeRenderer.setColor(0.1f, 0.8f, 0.1f, 1);
        shapeRenderer.rect(position.x, position.y + tileSize + 2, tileSize * (currentHealth / maxHealth), 6);
    }
}
