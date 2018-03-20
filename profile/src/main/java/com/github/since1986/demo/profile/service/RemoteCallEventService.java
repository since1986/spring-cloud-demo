package com.github.since1986.demo.profile.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public interface RemoteCallEventService {

    void consume() throws Throwable;

    void save(String json) throws IOException;
}
