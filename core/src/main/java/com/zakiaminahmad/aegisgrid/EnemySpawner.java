package com.zakiaminahmad.aegisgrid;

import java.util.List;

/**
 * Manages wave pacing and spawns enemies with initial calculated paths.
 * @author Zaki
 */
public class EnemySpawner {
    private float spawnTimer = 0f;
    private float spawnInterval = 1.5f;
    public int enemiesSpawnedThisWave = 0;
    public int enemiesPerWave = 5;

    public void update(float delta, List<Enemy> enemies, Wall[][] map, int tileSize, Base targetBase) {
        if (enemiesSpawnedThisWave >= enemiesPerWave) return;
        spawnTimer += delta;

        if (spawnTimer >= spawnInterval) {
            // Spawn hardcoded to grid Y: 5, X: 0
            Enemy newEnemy = new Enemy(0, 5 * tileSize);

            // Try clean path first, fallback to wall-breaking path
            List<AStar.Node> path = AStar.findPath(map, 0, 5, targetBase.gridX, targetBase.gridY, false);
            if (path == null) {
                path = AStar.findPath(map, 0, 5, targetBase.gridX, targetBase.gridY, true);
            }
            if (path != null) newEnemy.setPath(path);

            enemies.add(newEnemy);
            spawnTimer = 0f;
            enemiesSpawnedThisWave++;
        }
    }

    public void nextWave() {
        enemiesSpawnedThisWave = 0;
        enemiesPerWave += 2; // Increase difficulty linearly
    }
}
