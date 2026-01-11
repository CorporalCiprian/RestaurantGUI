package org.example.ui.util;

import javafx.concurrent.Task;

public final class FxAsync {

    private FxAsync() {
    }

    public static <T> void submit(Task<T> task) {
        if (task == null) throw new IllegalArgumentException("task");
        org.example.ui.util.AppExecutors.db().execute(task);
    }
}
