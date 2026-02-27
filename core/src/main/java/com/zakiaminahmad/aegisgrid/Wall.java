package com.zakiaminahmad.aegisgrid;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Destructible wall object.
 * Stores a neighborIndex so the renderer knows which auto-tiled sprite to draw.
 * @author Zaki
 */
public class Wall {
    public int gridX, gridY;
    public float maxHealth = 150f;
    public float currentHealth = 150f;

    // Crucial for the bitmasking auto-tile system
    public int neighborIndex = 0;

    public Wall(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public void takeDamage(float amount) {
        currentHealth -= amount;
        if (currentHealth < 0) currentHealth = 0;
    }

    public void drawHealthBar(ShapeRenderer shapeRenderer, int tileSize) {
        // Only show health bar if the wall has taken damage
        if (currentHealth >= maxHealth) return;

        float pixelX = gridX * tileSize;
        float pixelY = gridY * tileSize;

        shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1);
        shapeRenderer.rect(pixelX, pixelY + tileSize - 8, tileSize, 6);

        shapeRenderer.setColor(0.1f, 0.8f, 0.1f, 1);
        float healthPercentage = currentHealth / maxHealth;
        shapeRenderer.rect(pixelX, pixelY + tileSize - 8, tileSize * healthPercentage, 6);
    }
}
