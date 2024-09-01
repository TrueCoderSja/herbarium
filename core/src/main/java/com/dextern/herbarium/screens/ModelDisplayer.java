    package com.dextern.herbarium.screens;

    import com.badlogic.gdx.Game;
    import com.badlogic.gdx.Gdx;
    import com.badlogic.gdx.Screen;
    import com.badlogic.gdx.assets.loaders.ModelLoader;
    import com.badlogic.gdx.graphics.Color;
    import com.badlogic.gdx.graphics.GL20;
    import com.badlogic.gdx.graphics.PerspectiveCamera;
    import com.badlogic.gdx.graphics.g2d.BitmapFont;
    import com.badlogic.gdx.graphics.g2d.SpriteBatch;
    import com.badlogic.gdx.graphics.g3d.Attribute;
    import com.badlogic.gdx.graphics.g3d.Environment;
    import com.badlogic.gdx.graphics.g3d.Model;
    import com.badlogic.gdx.graphics.g3d.ModelBatch;
    import com.badlogic.gdx.graphics.g3d.ModelInstance;
    import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
    import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
    import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;

    import com.badlogic.gdx.Input;  // Add this import
    import com.badlogic.gdx.input.GestureDetector;
    import com.badlogic.gdx.math.MathUtils;
    import com.badlogic.gdx.math.Vector2;
    import com.badlogic.gdx.scenes.scene2d.Stage;
    import com.badlogic.gdx.scenes.scene2d.ui.Label;
    import com.badlogic.gdx.scenes.scene2d.ui.Skin;
    import com.badlogic.gdx.scenes.scene2d.ui.Table;
    import com.badlogic.gdx.utils.Align;
    import com.badlogic.gdx.utils.viewport.ScreenViewport;
    import com.dextern.herbarium.Herb;

    public class ModelDisplayer implements Screen, GestureDetector.GestureListener {

        private final Game game;
        private final Park park;
        private final Herb herb;
        private float lastZoomDistance;
        private boolean isTwoFingerDrag;

        public ModelDisplayer(Game game, Park park, Herb herb) {
            this.game=game;
            this.park=park;
            this.herb=herb;
        }

        private PerspectiveCamera camera;
        private ModelBatch modelBatch;
        private Model model;
        private ModelInstance modelInstance;
        private Environment environment;
        private BitmapFont font;
        private Stage stage;

        public void show() {
            modelBatch = new ModelBatch();
            environment = new Environment();
            environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
            environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));

            camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            camera.position.set(10f, 10f, 10f);
            camera.lookAt(0f, 0f, 0f);
            camera.near = 1f;
            camera.far = 300f;
            camera.update();

            // Load the model
            ModelLoader loader = new ObjLoader();
            model = loader.loadModel(Gdx.files.internal("3d/10432_Aloe_Plant_v1_max2008_it2.obj"));
            modelInstance = new ModelInstance(model);
            modelInstance.materials.get(0).set(ColorAttribute.createDiffuse(Color.GREEN));


            // Initialize the Stage and Skin
            stage = new Stage(new ScreenViewport());
            Skin skin = new Skin();

            // Create a label and add it to a Table
            Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
            Label modelLabel = new Label(herb.EnglishName, labelStyle);
            Label sName=new Label(herb.ScientificName, labelStyle);
            Label uses=new Label(herb.Uses, labelStyle);
            Label sideEffects=new Label(herb.SideEffects, labelStyle);
            Table table = new Table();
            table.setFillParent(true);
            table.align(Align.left);
            table.bottom().pad(100);
            table.add(modelLabel).padBottom(10); // Adds the first label
            table.row(); // Moves to the next row
            table.add(sName).padBottom(10); // Adds the second label
            table.row(); // Moves to the next row
            table.add(uses); // Adds the third lab
            table.add(sideEffects);
            stage.addActor(table);

            GestureDetector gestureDetector = new GestureDetector(this);
            Gdx.input.setInputProcessor(gestureDetector);

            // Initialize the lastZoomDistance
            lastZoomDistance = 0f;
        }


        @Override
        public void render(float delta) {
            handleInput();

            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

            // Render 3D model
            modelBatch.begin(camera);
            modelBatch.render(modelInstance, environment);
            modelBatch.end();

            // Render 2D UI
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
        }


        private void handleInput() {
            float moveSpeed = 0.1f;  // Movement speed in units per second
            float rotateSpeed = 1f; // Rotation speed in degrees per second
            float zoomSpeed = 1f;   // Zoom speed (change in FOV per second)

            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                    modelInstance.transform.translate(-moveSpeed, 0f, 0f);  // Move left
                }
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                    modelInstance.transform.translate(moveSpeed, 0f, 0f);   // Move right
                }
                if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                    modelInstance.transform.translate(0f, 0f, -moveSpeed);  // Move forward
                }
                if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                    modelInstance.transform.translate(0f, 0f, moveSpeed);   // Move backward
                }
            } else {
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                    modelInstance.transform.rotate(0f, 1f, 0f, rotateSpeed);  // Rotate left (Y-axis)
                }
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                    modelInstance.transform.rotate(0f, 1f, 0f, -rotateSpeed); // Rotate right (Y-axis)
                }
                if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                    modelInstance.transform.rotate(1f, 0f, 0f, rotateSpeed);  // Rotate up (X-axis)
                }
                if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                    modelInstance.transform.rotate(1f, 0f, 0f, -rotateSpeed); // Rotate down (X-axis)
                }
            }

            if (Gdx.input.isKeyPressed(Input.Keys.PAGE_UP)) {
                camera.fieldOfView -= zoomSpeed;  // Zoom in
                camera.update();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.PAGE_DOWN)) {
                camera.fieldOfView += zoomSpeed;  // Zoom out
                camera.update();
            }

            if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                game.setScreen(park);
            }

            // Clamp the FOV to reasonable values
            camera.fieldOfView = MathUtils.clamp(camera.fieldOfView, 10f, 100f);
        }



        @Override
        public void resize(int width, int height) {

        }

        @Override
        public void pause() {

        }

        @Override
        public void resume() {

        }

        @Override
        public void hide() {

        }

        @Override
        public void dispose() {
            modelBatch.dispose();
            model.dispose();
        }

        public boolean touchDown(float x, float y, int pointer, int button) {
            isTwoFingerDrag = Gdx.input.isTouched(1);  // Check if a second finger is touched
            return true;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            if (Gdx.input.isTouched(2)) { // Check if there are three fingers on the screen
                game.setScreen(park);
                return true;
            }
            return false;
        }

        @Override
        public boolean longPress(float x, float y) {
            return false;
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            if (isTwoFingerDrag) {
                // Two-finger drag: Move the model
                modelInstance.transform.translate(-deltaX * 0.01f, 0f, deltaY * 0.01f);
            } else {
                // One-finger drag: Rotate the model
                modelInstance.transform.rotate(0f, 1f, 0f, -deltaX * 0.5f); // Rotate around Y-axis
                modelInstance.transform.rotate(1f, 0f, 0f, deltaY * 0.5f);  // Rotate around X-axis
            }
            return true;
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            // Zooming the camera
            float zoomAmount = (initialDistance - distance) * 0.02f;
            camera.fieldOfView += zoomAmount;
            camera.fieldOfView = MathUtils.clamp(camera.fieldOfView, 10f, 100f);
            camera.update();
            return true;
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            return false;
        }

        @Override
        public void pinchStop() {
        }
    }
