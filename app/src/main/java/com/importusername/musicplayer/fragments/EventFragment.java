package com.importusername.musicplayer.fragments;

import androidx.fragment.app.Fragment;
import com.importusername.musicplayer.interfaces.IEventFragment;
import com.importusername.musicplayer.interfaces.IEventFragmentAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment containing methods for creating an event pipeline. Used for signaling a fragment's parent fragment of an event.
 */
public class EventFragment extends Fragment implements IEventFragment {
    private final Map<String, IEventFragmentAction> eventMap = new HashMap<>();

    public EventFragment() {
        super();
    }

    public EventFragment(int contentLayoutId) {
        super(contentLayoutId);
    }

    @Override
    public void setFragmentEventListener(String eventName, IEventFragmentAction action) {
        if (eventName != null && action != null) {
            this.eventMap.put(eventName, action);
        }
    }

    @Override
    public void emitFragmentEvent(String eventName, Object data) {
        if (this.eventMap.get(eventName) != null) {
            this.eventMap.get(eventName).eventAction(data);
        }
    }
}
