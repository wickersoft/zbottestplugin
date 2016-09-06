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
public class StateMachine extends State {

    /**
     * The current state of the state machine.
     */
    private State currentState;

    /**
     * Initialize the state machine.
     *
     * @param initialState the initial state of the state machine
     */
    public StateMachine(final State initialState) {
        super(
                () -> {
                    initialState.entry();
                },
                () -> {
                    initialState.exit();
                },
                () -> {
                    initialState.doStuff();
                }
        );
        currentState = initialState;
    }

    /**
     * Returns the current state of the state machine.
     *
     * @return the current state of the state machine.
     */
    public final State getCurrentState() {
        return currentState;
    }

    /**
     * Calls the Runnable associated with this state.
     */
    public final void doStuff0() {
        currentState.doStuff();
    }

    /**
     * Perform a transition based on the input. This calls the current state's
     * exit(), followed by the transition's associated transition(), and finally
     * the new state's entry(). If no such transition exists, no code is
     * executed and no change of state occurs.
     *
     */
    public void transitionByName0(final int signal) {
        if (!currentState.hasTransitionByName(signal)) {
            return;
        }
        Transition t = currentState.getTransitionByName(signal);
        currentState.exit();
        currentState = t.getDestination();
        t.transition();
        currentState.entry();
    }
}
