package org.sc.themis.input;

import org.joml.Vector2f;
import org.sc.themis.shared.Configuration;
import org.sc.themis.shared.tobject.TObject;
import org.sc.themis.window.Window;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class Input extends TObject {

    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(Input.class);

    private final Window window;

    private final Vector2f mousePosition         = new Vector2f();
    private final Vector2f displayVector         = new Vector2f();
    private final Vector2f previousMousePosition = new Vector2f( -1.0f, -1.0f );
    private final Map<Integer, Boolean> pressed  = new HashMap<>();

    private boolean leftMouseButtonPressed  = false;
    private boolean rightMouseButtonPressed = false;
    private boolean mouseInWindow           = false;


    public Input( Configuration configuration, Window window ) {
        super(configuration);
        this.window = window;
    }

    @Override
    public void setup() {
        setupMousePositionCallback();
        setupCursorCallback();
        setupMouseBoutonCallback();
        setupKeyCallback();
        setupPollListener();
        LOG.trace( "Input initialized" );
    }

    @Override
    public void cleanup() {}

    private void setupMousePositionCallback() {
        glfwSetCursorPosCallback( this.window.getHandle(), (_, xpos, ypos) -> {
            mousePosition.x = (float) xpos;
            mousePosition.y = (float) ypos;
        });
    }

    private void setupCursorCallback() {
        glfwSetCursorEnterCallback( this.window.getHandle(), (_, entered) -> mouseInWindow = entered);
    }

    private void setupMouseBoutonCallback() {
        glfwSetMouseButtonCallback( this.window.getHandle(), (_, button, action, mode) -> {
            leftMouseButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            rightMouseButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
    }

    private void setupKeyCallback() {
        glfwSetKeyCallback( this.window.getHandle(), (w, key, _, action, _) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(w, true);
            }
        });
    }

    private void setupPollListener() {
        this.window.addPollListener( this::mousePoll );
    }

    public Vector2f getMousePosition() {
        return this.mousePosition;
    }

    public boolean isLeftButtonPressed() {
        return leftMouseButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return rightMouseButtonPressed;
    }

    public boolean isKeyPressed( int code ) {
        return glfwGetKey( this.window.getHandle(), code ) == GLFW_PRESS;
    }

    public boolean isKeyPressedNoRepeat( int code ) {

        boolean isPressed = isKeyPressed( code );

        if ( isPressed ) {

            if ( this.pressed.containsKey(code) && this.pressed.get( code ) ) {
                return false;
            }

            this.pressed.put( code, true );
            return true;

        }

        this.pressed.put( code, false );
        return false;

    }

    public Vector2f getDisplayVector() {
        return this.displayVector;
    }

    private void mousePoll() {

        displayVector.x = 0;
        displayVector.y = 0;

        if (previousMousePosition.x > 0 && previousMousePosition.y > 0 && mouseInWindow) {

            double  deltax  = mousePosition.x - previousMousePosition.x;
            double  deltay  = mousePosition.y - previousMousePosition.y;
            boolean rotateX = deltax != 0;
            boolean rotateY = deltay != 0;

            if (rotateX) {
                displayVector.y = (float) deltax;
            }

            if (rotateY) {
                displayVector.x = (float) deltay;
            }

        }

        previousMousePosition.x = mousePosition.x;
        previousMousePosition.y = mousePosition.y;

    }


}
