package com.zakiaminahmad.aegisgrid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Homing missile system fired by Towers.
 * @author Zaki
 */
public class Projectile {
    public Vector2 position;
    private Enemy target;
    private float speed = 500f;
    private float damage = 35f;
    public boolean active = true;
    private int size = 16;

    public Projectile(float startX, float startY, Enemy target) {
        this.position = new Vector2(startX, startY);
        this.target = target;
    }

    public void update(float delta, int tileSize) {
        // Despawn bullet if enemy dies mid-flight
        if (target == null || target.currentHealth <= 0) {
            active = false;
            return;
        }

        // Aim for the center of the enemy tile
        float targetX = target.position.x + (tileSize / 2f);
        float targetY = target.position.y + (tileSize / 2f);

        float dirX = targetX - position.x;
        float dirY = targetY - position.y;
        float distance = (float) Math.sqrt(dirX * dirX + dirY * dirY);

        if (distance < speed * delta) {
            target.takeDamage(damage);
            active = false;
        } else {
            position.x += (dirX / distance) * speed * delta;
            position.y += (dirY / distance) * speed * delta;
        }
    }

    public void draw(SpriteBatch batch, Texture texture) {
        batch.draw(texture, position.x - (size / 2f), position.y - (size / 2f), size, size);
    }
}
