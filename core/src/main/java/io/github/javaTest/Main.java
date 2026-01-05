package io.github.javaTest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

// Que es spriteBatch exactamente?
// La logica del sprite.draw es => Dibujame dentro de [spriteBatch]?
// Que hace esta linea?   spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
// El render es el Update de unity?
/*
  Si tengo esto en el render:
  input();
  logic();

  Porque bucketSprite.setCenterX(touchPos.x); y bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth)); funcionan bien?
  Esto es lo que me confunde de gamedev, eso esta pasando al mismo tiempo? Porque una no superpone a la otra?
* */
// 1f de deltaTime es 1 segundo?
// setY vs translateY? en un sprite, cual es la diff?
// if (dropSprite.getY() < -dropHeight) dropSprites.removeIndex(i); No entiendo esta linea, porque comparar con dropHeight, no seria con viewportHeight?
// Que tan rapido sucede el render que puedo agregar y eliminar dentro de un for elementos, ademas de crear los rectangulos y colisiones ahi? Me saca mucho de onda.
//

public class Main extends ApplicationAdapter {
    private Texture backgroundTexture;
    private Texture buckedTexture;
    private Texture dropTexture;
    private Sound dropSound;
    private Music music;

    private SpriteBatch spriteBatch;
    private FitViewport viewport;

    Array<Sprite> dropSprites;
    private Sprite bucketSprite;

    Vector2 touchPos;

    float dropTimer;

    // Collisions
    Rectangle bucketRectangle;
    Rectangle dropRectangle;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();

        //Textures - Images
        backgroundTexture = new Texture("background.png");
        dropTexture = new Texture("drop.png");

        buckedTexture = new Texture("bucket.png");
        bucketSprite = new Sprite(buckedTexture);
        bucketSprite.setSize(1, 1);


        //Sounds Music - if the music is less than 10 secs then is a sound
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));

        viewport = new FitViewport(8, 5);

        touchPos = new Vector2();
        dropSprites = new Array<>();

        // Collisions
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();

        // Play background music
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        // Traduce los pixeles a unidades para saber que esta pasando en coordenadas del viewport.
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);

        bucketSprite.draw(spriteBatch);

        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    private void createDroplet() {
        float dropWith = 1f;
        float dropHeight = 1f;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWith, dropHeight);

        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWith));

        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite);
    }

    private void input() {
        float speed = 5f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucketSprite.translateX(speed * delta);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta);
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY()); //Get where the click/touch happened on screen
            viewport.unproject(touchPos); // Convert the units to the world units of the viewport
            bucketSprite.setCenterX(touchPos.x); // Change horizontal position of the bucket.
        }
    }

    private void logic() {
        float worldWidth = viewport.getWorldWidth();

        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();

        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));

        float delta = Gdx.graphics.getDeltaTime(); // retrieve the current delta

        // Setting collisions to bucket
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-2f * delta);

            // Setting collisions to drop
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            // if the top of the drop goes below the bottom of the view, remove it
            if (dropSprite.getY() < -dropHeight) dropSprites.removeIndex(i);

            if (bucketRectangle.overlaps(dropRectangle)) {
                dropSprites.removeIndex(i);
                dropSound.play(); // Play the sound
            }
        }
        
        dropTimer += delta; //Adds current delta to the timer
        if (dropTimer > 1f) {
            dropTimer = 0;
            createDroplet();
        }

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
    }
}
