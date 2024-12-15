package org.sc.themis.engine;

import org.sc.themis.engine.exception.EngineGamestateNotFoundException;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.input.Input;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.renderer.activity.RendererActivity;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.tobject.TObject;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.window.Window;

public class Engine extends TObject {

    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(Engine.class);

    private final Window window;
    private final Input input;
    private final Renderer renderer;
    private final Scene scene;

    private EngineStatus status = EngineStatus.STOPPED;

    private Gamestate nextGamestate;
    private Gamestate currentGamestate;

    public Engine(Configuration configuration, RendererActivity activity ) {
        super(configuration);
        this.window = new Window( configuration );
        this.input = new Input( configuration, this.window );
        this.renderer = new Renderer( configuration, this.window, this.input, activity );
        this.scene = new Scene( configuration );
    }

    @Override
    public void setup() throws ThemisException {

        LOG.trace("Engine initialisation ... ");

        this.window.setup();
        this.input.setup();
        this.renderer.setup();
        this.scene.setup();

        LOG.trace("Engine initialized");

    }

    @Override
    public void cleanup() throws ThemisException {
        this.scene.cleanup();
        this.renderer.cleanup();
        this.input.cleanup();
        this.window.cleanup();
    }

    public void run() throws ThemisException {

        Assertions.notNull( this.nextGamestate, new EngineGamestateNotFoundException() );

        this.status = EngineStatus.RUNNING;
        this.loop();

    }

    public void stop() {
        this.status = EngineStatus.STOPPED;
    }

    public void setGamestate( Gamestate gamestate ) {
        this.nextGamestate = gamestate;
    }

    private void loop() throws ThemisException {

        long targetfps     = 60;
        long initialTime = System.currentTimeMillis();
        float timeU       = 1000.0f / targetfps; //60 updates par seconde
        double deltaUpdate = 0;
        long updateTime  = initialTime;

        while ( this.status == EngineStatus.RUNNING && !this.window.shouldClose()) {

            loadRequestedGamestate();

            this.window.poll();

            long now = System.currentTimeMillis();
            final long value = now - initialTime;

            deltaUpdate += (value) / timeU;

            this.input( value );

            if (deltaUpdate >= 1) {
                long diffTimeMilis = now - updateTime;
                this.update( diffTimeMilis );
                updateTime = now;
                deltaUpdate--;
            }

            this.render( value );

            initialTime = now;

        }

        this.cleanupGamestate();

    }

    private void loadRequestedGamestate() throws ThemisException {
        if ( this.nextGamestate != null ) {
            this.cleanupGamestate();
            this.loadGamestate( this.nextGamestate );
            this.nextGamestate = null;
        }
    }

    private void loadGamestate( Gamestate gamestate ) throws ThemisException {
        this.currentGamestate = gamestate;
        this.currentGamestate.setup( this.renderer, this.scene );
    }

    private void cleanupGamestate() {
        if ( this.currentGamestate != null ) {
            try {
                this.currentGamestate.cleanup( this.renderer, this.scene );
            } catch (ThemisException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void update( long tpf ) {
        this.currentGamestate.update( this.scene, tpf );
    }

    private void input( long tpf ) {
        this.currentGamestate.input( this.scene, this.input, tpf );
    }

    private void render( long tpf ) throws ThemisException {
        this.renderer.render( this.scene, tpf );
    }

}
