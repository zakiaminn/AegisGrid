package com.zakiaminahmad.aegisgrid;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Game Loop and Systems Manager.
 * @author Zaki
 */
public class AegisGrideGame extends ApplicationAdapter {
    public enum GameState { PREP, DEFEND, GAMEOVER }
    public enum BuildMode { NONE, WALL, TOWER }

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont largeFont;

    // Assets
    private Texture groundTexture;
    private Texture baseTexture;
    private Texture wallTexture; // Back to a single seamless texture
    private Texture towerTexture;
    private Texture enemySheet;
    private TextureRegion[] enemyWalkFrames;
    private Texture projectileTexture;
    private Texture spawnTowerTexture;

    // Grid Architecture
    private final int tileSize = 64;
    private final int cols = 20;
    private final int rows = 12;
    private Wall[][] map = new Wall[cols][rows];

    private List<Enemy> enemies;
    private List<Tower> towers;
    private List<Projectile> projectiles;
    private EnemySpawner spawner;
    private Base mainBase;

    private GameState currentState = GameState.PREP;
    private BuildMode currentMode = BuildMode.NONE;
    private float prepTimer = 15f;
    private int money = 150;
    private final int wallCost = 10;
    private final int towerCost = 50;
    private int wave = 1;

    private Rectangle wallButton;
    private Rectangle towerButton;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(cols * tileSize, rows * tileSize, camera);
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        font = new BitmapFont(); font.getData().setScale(1.5f);
        largeFont = new BitmapFont(); largeFont.getData().setScale(3.5f);

        // --- Asset Loading ---
        groundTexture = new Texture("ground.png");
        baseTexture = new Texture("base.png");
        wallTexture = new Texture("wall.png"); // The seamless wall block
        towerTexture = new Texture("tower.png");
        projectileTexture = new Texture("bullet.png");
        spawnTowerTexture = new Texture("spawn_tower.png");

        // Load & Slice Enemy Animation Strip (4 frames)
        enemySheet = new Texture("enemy.png");
        int frameWidth = enemySheet.getWidth() / 4;
        TextureRegion[][] enemyTmp = TextureRegion.split(enemySheet, frameWidth, enemySheet.getHeight());
        enemyWalkFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) enemyWalkFrames[i] = enemyTmp[0][i];

        enemies = new ArrayList<>();
        towers = new ArrayList<>();
        projectiles = new ArrayList<>();
        spawner = new EnemySpawner();
        mainBase = new Base(12, 5, tileSize);

        int worldHeight = rows * tileSize;
        wallButton = new Rectangle(50, worldHeight - 90, 200, 60);
        towerButton = new Rectangle(270, worldHeight - 90, 200, 60);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void recalculateEnemyPaths() {
        for (Enemy e : enemies) {
            int gx = Math.max(0, Math.min(cols - 1, (int) (e.position.x / tileSize)));
            int gy = Math.max(0, Math.min(rows - 1, (int) (e.position.y / tileSize)));

            List<AStar.Node> path = AStar.findPath(map, gx, gy, mainBase.gridX, mainBase.gridY, false);
            if (path == null) path = AStar.findPath(map, gx, gy, mainBase.gridX, mainBase.gridY, true);
            if (path != null) e.setPath(path);
        }
    }

    private void resetGame() {
        mainBase.currentHealth = mainBase.maxHealth;
        enemies.clear(); towers.clear(); projectiles.clear();
        money = 150; wave = 1; currentState = GameState.PREP;
        currentMode = BuildMode.NONE; prepTimer = 15f;
        spawner.enemiesSpawnedThisWave = 0; spawner.enemiesPerWave = 5;
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) map[i][j] = null;
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float delta = Gdx.graphics.getDeltaTime();

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // ================= LOGIC PHASE ================= //
        if (mainBase.currentHealth <= 0 && currentState != GameState.GAMEOVER) currentState = GameState.GAMEOVER;

        if (currentState == GameState.GAMEOVER) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) resetGame();
        }
        else if (currentState == GameState.PREP) {
            prepTimer -= delta;
            if (prepTimer <= 0) currentState = GameState.DEFEND;

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                viewport.unproject(mouse);

                if (wallButton.contains(mouse.x, mouse.y)) currentMode = BuildMode.WALL;
                else if (towerButton.contains(mouse.x, mouse.y)) currentMode = BuildMode.TOWER;
                else {
                    int gx = (int) (mouse.x / tileSize);
                    int gy = (int) (mouse.y / tileSize);

                    if (gx >= 0 && gx < cols && gy >= 0 && gy < rows) {
                        if (currentMode == BuildMode.WALL) {
                            if (map[gx][gy] == null && money >= wallCost) {
                                map[gx][gy] = new Wall(gx, gy); money -= wallCost;
                                recalculateEnemyPaths();
                            } else if (map[gx][gy] != null) {
                                map[gx][gy] = null; money += wallCost;
                                recalculateEnemyPaths();
                            }
                        } else if (currentMode == BuildMode.TOWER && map[gx][gy] == null && money >= towerCost) {
                            towers.add(new Tower(gx, gy)); money -= towerCost;
                        }
                    }
                }
            }
        }
        else if (currentState == GameState.DEFEND) {
            spawner.update(delta, enemies, map, tileSize, mainBase);
            if (spawner.enemiesSpawnedThisWave >= spawner.enemiesPerWave && enemies.isEmpty()) {
                wave++; currentState = GameState.PREP; prepTimer = 15f; spawner.nextWave();
            }
        }

        // --- THE MISSING WALL DESTRUCTION LOGIC ---
        if (currentState != GameState.GAMEOVER) {
            // Check for dead walls FIRST
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    if (map[i][j] != null && map[i][j].currentHealth <= 0) {
                        map[i][j] = null; // Destroy the wall
                        recalculateEnemyPaths(); // Tell enemies the path is clear
                    }
                }
            }

            for (Tower t : towers) t.update(delta, enemies, projectiles, tileSize);

            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy e = enemies.get(i);
                e.update(delta, tileSize, map);

                if (e.reachedTarget) { mainBase.takeDamage(20f); enemies.remove(i); }
                else if (e.currentHealth <= 0) { money += 15; enemies.remove(i); }
            }

            for (int i = projectiles.size() - 1; i >= 0; i--) {
                Projectile p = projectiles.get(i);
                p.update(delta, tileSize);
                if (!p.active) projectiles.remove(i);
            }
        }

        // ================= RENDER PHASE ================= //

        batch.begin();
        // 1. Draw Ground
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                batch.draw(groundTexture, i * tileSize, j * tileSize, tileSize, tileSize);
            }
        }

        // 2. Draw Entities
        batch.draw(spawnTowerTexture, -tileSize, 4 * tileSize, tileSize * 3, tileSize * 3);

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if (map[i][j] != null) {
                    batch.draw(wallTexture, i * tileSize, j * tileSize, tileSize, tileSize);
                }
            }
        }

        mainBase.draw(batch, baseTexture);

        for (Enemy e : enemies) {
            if (e.getWalkAnimation() == null) e.setAnimation(new TextureRegion[][]{enemyWalkFrames}, 0.15f);
            e.draw(batch, enemyWalkFrames[0], tileSize);
        }
        for (Tower t : towers) t.draw(batch, towerTexture, tileSize);
        for (Projectile p : projectiles) p.draw(batch, projectileTexture);
        batch.end();

        // 3. Draw UI Boxes & Health Bars
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (currentState == GameState.PREP) {
            shapeRenderer.setColor(currentMode == BuildMode.WALL ? 0.3f : 0.5f, currentMode == BuildMode.WALL ? 0.9f : 0.5f, 0.3f, 1);
            shapeRenderer.rect(wallButton.x, wallButton.y, wallButton.width, wallButton.height);
            shapeRenderer.setColor(currentMode == BuildMode.TOWER ? 0.3f : 0.5f, currentMode == BuildMode.TOWER ? 0.9f : 0.5f, 0.3f, 1);
            shapeRenderer.rect(towerButton.x, towerButton.y, towerButton.width, towerButton.height);
        }

        mainBase.drawHealthBar(shapeRenderer);
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) if (map[i][j] != null) map[i][j].drawHealthBar(shapeRenderer, tileSize);
        }
        for (Enemy e : enemies) e.drawHealthBar(shapeRenderer, tileSize);
        shapeRenderer.end();

        // 4. Draw UI Text
        batch.begin();
        float screenH = rows * tileSize;
        if (currentState == GameState.GAMEOVER) {
            largeFont.setColor(1, 0, 0, 1);
            largeFont.draw(batch, "GAME OVER", (cols * tileSize / 2f) - 150, screenH / 2f);
        } else {
            font.setColor(1, 1, 1, 1);
            font.draw(batch, "Bank: $" + money + " | Wave: " + wave + " | Phase: " + currentState, 20, screenH - 20);
            if (currentState == GameState.PREP) {
                font.draw(batch, "TIME: " + (int)prepTimer, (cols * tileSize) - 150, screenH - 20);
                font.draw(batch, "WALL ($10)", wallButton.x + 35, wallButton.y + 40);
                font.draw(batch, "TOWER ($50)", towerButton.x + 30, towerButton.y + 40);
            }
        }
        batch.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose(); batch.dispose(); font.dispose(); largeFont.dispose();
        groundTexture.dispose(); baseTexture.dispose(); wallTexture.dispose();
        towerTexture.dispose(); enemySheet.dispose(); projectileTexture.dispose(); spawnTowerTexture.dispose();
    }
}
