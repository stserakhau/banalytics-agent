package com.banalytics.box.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
@Service
public class ScriptExecutionService implements InitializingBean {
    private final Map<Integer, CompiledScript> compiledScriptCache = new ConcurrentHashMap<>();


    private final RestOperations scriptingRestOperations;

    private ScriptEngineManager manager;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.manager = new ScriptEngineManager(ScriptExecutionService.class.getClassLoader());
    }

    public Object execute(Map<String, Object> context, String script) throws Exception {
        NashornScriptEngine engine = (NashornScriptEngine) manager.getEngineByName("nashorn");

        int scriptKey = script.hashCode();
        CompiledScript cs = compiledScriptCache.get(scriptKey);
        if (cs == null) {
            cs = engine.compile(script);
            compiledScriptCache.put(scriptKey, cs);
        }

        ScriptContext sc = engine.getContext();
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            sc.setAttribute(key, value, ScriptContext.ENGINE_SCOPE);
        }
        attachApi(sc);
        return cs.eval(sc);//engine.eval(script);
    }

    private void attachApi(ScriptContext scriptContext) {
        scriptContext.setAttribute("log", log, ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute("rest", scriptingRestOperations, ScriptContext.ENGINE_SCOPE);
    }
}
