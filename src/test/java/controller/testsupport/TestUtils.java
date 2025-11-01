package controller.testsupport;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class TestUtils {

    public static RequestDispatcher stubForward(HttpServletRequest req, String path) {
        RequestDispatcher rd = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(path)).thenReturn(rd);
        return rd;
    }

    public static RequestDispatcher mockDispatcher(HttpServletRequest req) {
        RequestDispatcher rd = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(anyString())).thenReturn(rd);
        return rd;
    }

    public static Map<String, Object> createSessionMap() {
        return new HashMap<>();
    }

    public static HttpSession mockSession(Map<String, Object> sessionMap) {
        HttpSession session = mock(HttpSession.class);
        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return sessionMap.get(key);
        }).when(session).getAttribute(anyString());

        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            sessionMap.put(key, value);
            return null;
        }).when(session).setAttribute(anyString(), any());

        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            sessionMap.remove(key);
            return null;
        }).when(session).removeAttribute(anyString());

        return session;
    }

    public static Part mockPart(String name, String fileName, String contentType, byte[] content) throws IOException {
        Part part = mock(Part.class);
        when(part.getName()).thenReturn(name);
        when(part.getSubmittedFileName()).thenReturn(fileName);
        when(part.getContentType()).thenReturn(contentType);
        when(part.getSize()).thenReturn((long) content.length);
        InputStream inputStream = new ByteArrayInputStream(content);
        when(part.getInputStream()).thenReturn(inputStream);
        return part;
    }

    /**
     * Reflection utility to set private/final fields for testing.
     */
    public static void forceSet(Object target, String fieldName, Object value) {
        Class<?> c = target.getClass();
        while (c != null) {
            try {
                java.lang.reflect.Field f = c.getDeclaredField(fieldName);
                f.setAccessible(true);
                try {
                    // Attempt to clear final modifier when possible (best-effort)
                    java.lang.reflect.Field modField = java.lang.reflect.Field.class.getDeclaredField("modifiers");
                    modField.setAccessible(true);
                    modField.setInt(f, f.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
                } catch (Throwable ignored) { /* best-effort */ }
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalArgumentException("Field not found: " + fieldName + " in " + target.getClass());
    }
}
