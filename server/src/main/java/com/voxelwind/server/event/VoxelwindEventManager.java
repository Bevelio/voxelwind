package com.voxelwind.server.event;

import com.voxelwind.api.event.Event;
import com.voxelwind.api.event.EventManager;
import com.voxelwind.api.event.Listener;
import com.voxelwind.server.event.firehandlers.ReflectionEventFireHandler;

import java.lang.reflect.Method;
import java.util.*;

public class VoxelwindEventManager implements EventManager {
    private final Map<Class<? extends Event>, EventFireHandler> eventHandlers = new HashMap<>();
    private final List<Object> listeners = new ArrayList<>();

    @Override
    public void register(Object listener) {
        listeners.add(listener);
        bakeHandlers();
    }

    @Override
    public void fire(Event event) {
        EventFireHandler handler = eventHandlers.get(event.getClass());
        if (handler != null) {
            handler.fire(event);
        }
    }

    @Override
    public void unregister(Object listener) {
        listeners.remove(listener);
        bakeHandlers();
    }

    private void bakeHandlers() {
        Map<Class<? extends Event>, List<ReflectionEventFireHandler.ListenerMethod>> listenerMap = new HashMap<>();

        for (Object listener : listeners) {
            for (Method method : listener.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Listener.class)) {
                    listenerMap.computeIfAbsent((Class<? extends Event>) method.getParameterTypes()[0], (k) -> new ArrayList<>())
                            .add(new ReflectionEventFireHandler.ListenerMethod(listener, method));
                }
            }
        }

        for (List<ReflectionEventFireHandler.ListenerMethod> methods : listenerMap.values()) {
            Collections.sort(methods);
        }

        for (Map.Entry<Class<? extends Event>, List<ReflectionEventFireHandler.ListenerMethod>> entry : listenerMap.entrySet()) {
            eventHandlers.put(entry.getKey(), new ReflectionEventFireHandler(entry.getValue()));
        }
    }
}