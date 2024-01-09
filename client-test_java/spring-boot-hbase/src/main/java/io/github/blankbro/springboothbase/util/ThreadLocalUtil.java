package io.github.blankbro.springboothbase.util;

import org.springframework.util.StopWatch;

/**
 * ThreadLocalUtil
 *
 * @author Zexin Li
 * @date 2023-06-29 16:25
 */
public class ThreadLocalUtil {

    private ThreadLocalUtil() {
    }

    public static final ThreadLocal<StopWatch> stopWatchLocal = new ThreadLocal<>();
}
