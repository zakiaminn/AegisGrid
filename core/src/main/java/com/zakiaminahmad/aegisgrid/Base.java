package com.zakiaminahmad.aegisgrid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * The main objective the player must protect.
 * @author Zaki
 */
public class Base {
    public int gridX, gridY;
    public float maxHealth = 500f;
    public float currentHealth = 500f;
    private int tileSize;

    public Base(int gridX, int gridY, int tileSize) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.tileSize = tileSize;
    }

    public void takeDamage(float amount) {
        currentHealth -= amount;
        if (currentHealth < 0) currentHealth = 0;
    }

    public void draw(SpriteBatch batch, Texture texture) {
        // Scaling the base up to 3x3 tiles so it looks imposing on the map
        float drawSize = tileSize * 3;
        float drawX = (gridX * tileSize) - tileSize;
        float drawY = (gridY * tileSize) - tileSize;

        // Draw a fake drop shadow for depth
        batch.setColor(0f, 0f, 0f, 0.5f);
        batch.draw(texture, drawX + 10, drawY - 10, drawSize, drawSize);

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(texture, drawX, drawY, drawSize, drawSize);
    }

    public void drawHealthBar(ShapeRenderer shapeRenderer) {
        float drawSize = tileSize * 3;
        float drawX = (gridX * tileSize) - tileSize;
        float drawY = (gridY * tileSize) - tileSize;

        shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1);
        shapeRenderer.rect(drawX, drawY + drawSize + 5, drawSize, 8);

        shapeRenderer.setColor(0.1f, 0.8f, 0.1f, 1);
        shapeRenderer.rect(drawX, drawY + drawSize + 5, drawSize * (currentHealth / maxHealth), 8);
    }
}
