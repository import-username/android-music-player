package com.importusername.musicplayer.interfaces;

/**
 * Core methods that an event fragment should include.
 */
public interface IEventFragment {
    void setFragmentEventListener(String eventName, IEventFragmentAction action);

    void emitFragmentEvent(String eventName, Object data);
}
