/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.image.statemachine.a5;

import java.util.HashMap;

/**
 *
 * @author Dennis
 */
public class State {

    /**
     * The set of transitions leading away from this state, and their
     * destinations.
     */
    private final HashMap<Integer, Transition> transitions = new HashMap<>();
    /**
     * The Runnable to be called on entering this state.
     */
    private final Runnable onEntryRunnable;
    /**
     * The Runnable to be called on exiting this state.
     */
    private final Runnable onExitRunnable;
    /**
     * The Runnable to be called while residing in the current state.
     */
    private final Runnable onDoRunnable;

    /**
     * Create a new state with the given Runnables to be called on entry and
     * exit.
     *
     * @param onEntry the Runnable to be called on entering this state
     * @param onExit the Runnable to be called on exiting this state
     * @param onDo the Runnable to be called while residing in this state
     */
    public State(final Runnable onEntry, final Runnable onExit,
            final Runnable onDo) {
        onEntryRunnable = onEntry;
        onExitRunnable = onExit;
        onDoRunnable = onDo;
    }

    /**
     * Adds a transition from this state to a provided state with an optional
     * Runnable to be called during the transition.
     *
     * @param signal the name of this transition
     * @param neighborState the state the transition should lead to
     * @param r a Runnable to be called during the transition, or null
     */
    public final void addNeighbor(final int signal, final State neighborState,
            final Runnable r) {
        transitions.put(signal, new Transition(this, neighborState, r));
    }

    /**
     * Checks if this state has a transition associated with the given name.
     *
     * @param name the name of the transition to look for
     * @return whether this state has a transition associated with the given
     * name
     */
    protected final boolean hasTransitionByName(final int signal) {
        return transitions.containsKey(signal);
    }

    /**
     * Get the transition from this state associated with the given name.
     *
     * @param name the name of the transition
     * @return the associated transition
     */
    protected final Transition getTransitionByName(final int signal) {
        return transitions.get(signal);
    }

    /**
     * Perform the transition given by this name. If no such transition exists,
     * no code is executed and the method returns the object called.
     *
     * @param name the name of the desired transition
     * @return the state the transition leads to
     */
    public final State transitionByName(final int signal) {
        if (hasTransitionByName(signal)) {
            return this;
        }
        Transition t = getTransitionByName(signal);
        exit();
        State newState = t.getDestination();
        t.transition();
        newState.entry();
        return newState;
    }

    /**
     * Calls the entry runnable provided in the constructor if it exists.
     */
    protected final void exit() {
        if (onExitRunnable != null) {
            onExitRunnable.run();
        }
    }

    /**
     * Calls the entry runnable provided in the constructor if it exists.
     */
    protected final void entry() {
        if (onEntryRunnable != null) {
            onEntryRunnable.run();
        }
    }

    /**
     * Calls the do runnable provided in the constructor if it exists.
     */
    protected final int doStuff() {
        if (onDoRunnable != null) {
            onDoRunnable.run();    
        }
        return 0;
    }

}
