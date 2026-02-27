package com.zakiaminahmad.aegisgrid;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.List;

/**
 * Static defense structure.
 * @author Zaki
 */
public class Tower {
    public int gridX, gridY;
    private float range = 300f;
    private float cooldown = 1.0f;
    private float timer = 0f;
    private float rotationAngle = 0f;

    public Tower(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public void update(float delta, List<Enemy> enemies, List<Projectile> projectiles, int tileSize) {
        timer += delta;
        Enemy target = null;
        float closestDist = range;

        float centerX = (gridX * tileSize) + (tileSize / 2f);
        float centerY = (gridY * tileSize) + (tileSize / 2f);

        for (Enemy e : enemies) {
            float eDist = (float) Math.sqrt(Math.pow(e.position.x + (tileSize / 2f) - centerX, 2) +
                Math.pow(e.position.y + (tileSize / 2f) - centerY, 2));
            if (eDist < closestDist) {
                closestDist = eDist;
                target = e;
            }
        }

        if (target != null) {
            // Calculate rotation to face enemy. Offset by 0 or 180 based on how your asset was drawn.
            rotationAngle = (float) Math.toDegrees(Math.atan2(target.position.y + (tileSize / 2f) - centerY,
                target.position.x + (tileSize / 2f) - centerX));
            if (timer >= cooldown) {
                projectiles.add(new Projectile(centerX, centerY, target));
                timer = 0f;
            }
        }
    }

    public void draw(SpriteBatch batch, Texture texture, int tileSize) {
        TextureRegion region = new TextureRegion(texture);
        float x = gridX * tileSize;
        float y = gridY * tileSize;

        // Draw with the rotation applied
        batch.draw(region, x, y, tileSize / 2f, tileSize / 2f, tileSize, tileSize, 1f, 1f, rotationAngle);
    }
}
