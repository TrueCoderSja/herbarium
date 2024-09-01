package com.dextern.herbarium;

import com.badlogic.gdx.Game;
import com.dextern.herbarium.screens.ModelDisplayer;
import com.dextern.herbarium.screens.Park;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {

    @Override
    public void create() {
        setScreen(new Park(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
