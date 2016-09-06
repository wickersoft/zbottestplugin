/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.image.statemachine.a5;

/**
 *
 * @author Dennis
 */
public class Transition {

    /**
     * The state from which this transition originates.
     */
    private final State origin;
    /**
     * The state this transition leads to.
     */
    private final State destination;
    /**
     * A runnable that can be called using transition(), or null.
     */
    private final Runnable transition;

    /**
     * Create a new transition between two given states.
     * Optionally, a Runnable can be provided for later retrieval
     * @param origi the state from which this transition originates
     * @param destinatio the state this transition leads to
     * @param r a runnable that can be called using transition(), or null
     */
    public Transition(final State origi, final State destinatio,
            final Runnable r) {
        this.origin = origi;
        this.destination = destinatio;
        this.transition = r;
    }

    /**
     * Get the origin of this transition.
     *
     * @return the origin of this transition.
     */
    public final State getOrigin() {
        return origin;
    }

    /**
     * Get the destination of this transition.
     *
     * @return the destination of this transition.
     */
    public final State getDestination() {
        return destination;
    }

    /**
     * Perform the action associated with this transition.
     */
    public final void transition() {
        if (transition != null) {
            transition.run();
        }
    }

}
