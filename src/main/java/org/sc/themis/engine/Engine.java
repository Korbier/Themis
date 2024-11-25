package org.sc.themis.engine;

import org.sc.themis.engine.exception.EngineGamestateNotFoundException;
import org.sc.themis.gamestate.Gamestate;
import org.sc.themis.input.Input;
import org.sc.themis.input.InputDescriptor;
import org.sc.themis.renderer.Renderer;
import org.sc.themis.scene.Scene;
import org.sc.themis.shared.TObject;
import org.sc.themis.shared.assertion.Assertions;
import org.sc.themis.shared.exception.ThemisException;
import org.sc.themis.window.Window;

public class Engine extends TObject<EngineDescriptor> {

    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(Engine.class);

    private final Window window;
    private final Input input;
    private final Renderer renderer;
    private final Scene scene;

    private EngineStatus status = EngineStatus.STOPPED;

    private Gamestate nextGamestate;
    private Gamestate currentGamestate;

    public Engine(EngineDescriptor descriptor) {
        super(descriptor);
        this.window = new Window( descriptor.window() );
        this.input = new Input( this.window, new InputDescriptor() );
        this.renderer = new Renderer( descriptor.renderer() );
        this.scene = new Scene( this.renderer, descriptor.scene() );
    }

    @Override
    public void setup() throws ThemisException {

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

    }

    private void loadRequestedGamestate() {
        if ( this.nextGamestate != null ) {
            this.cleanupGamestate( this.currentGamestate );
            this.loadGamestate( this.nextGamestate );
            this.nextGamestate = null;
        }
    }

    private void loadGamestate(Gamestate gamestate) {
        this.currentGamestate = gamestate;
    }

    private void cleanupGamestate(Gamestate gamestate) {
    }

    private void update( long tpf ) throws ThemisException {
        this.currentGamestate.update( this.scene, tpf );
    }

    private void input( long tpf ) {
        this.currentGamestate.input( this.scene, this.input, tpf );
    }

    private void render( long tpf ) {
        this.renderer.render( this.scene, tpf );
    }

}
