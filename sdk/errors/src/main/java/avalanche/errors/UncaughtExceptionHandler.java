package avalanche.errors;

import android.os.Process;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

import avalanche.core.ingestion.models.json.DefaultLogSerializer;
import avalanche.core.ingestion.models.json.LogSerializer;
import avalanche.core.utils.AvalancheLog;
import avalanche.core.utils.StorageHelper;
import avalanche.errors.ingestion.models.JavaErrorLog;
import avalanche.errors.ingestion.models.json.JavaErrorLogFactory;
import avalanche.errors.utils.ErrorLogHelper;

class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final LogSerializer mLogSerializer;

    private final boolean mIgnoreDefaultExceptionHandler = false;

    private Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

    UncaughtExceptionHandler() {
        register();
        mLogSerializer = new DefaultLogSerializer();
        mLogSerializer.addLogFactory(JavaErrorLog.TYPE, JavaErrorLogFactory.getInstance());
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        if (!ErrorReporting.isEnabled() && mDefaultUncaughtExceptionHandler != null) {
            mDefaultUncaughtExceptionHandler.uncaughtException(thread, exception);
        } else {
            JavaErrorLog errorLog = ErrorLogHelper.createErrorLog(null, thread, exception, Thread.getAllStackTraces(), ErrorReporting.getInstance().getInitializeTimestamp());
            try {
                File errorLogDirectory = ErrorLogHelper.getErrorStorageDirectory();
                File errorLogFile = new File(errorLogDirectory, errorLog.getId().toString() + ".json");
                String errorLogString = mLogSerializer.serializeLog(errorLog);
                StorageHelper.InternalStorage.write(errorLogFile, errorLogString);
            } catch (JSONException e) {
                AvalancheLog.error("Error serializing error log to JSON", e);
            } catch (IOException e) {
                AvalancheLog.error("Error writing error log to file", e);
            }
            if (!mIgnoreDefaultExceptionHandler && mDefaultUncaughtExceptionHandler != null) {
                mDefaultUncaughtExceptionHandler.uncaughtException(thread, exception);
            } else {
                Process.killProcess(Process.myPid());
                System.exit(10);
            }
        }
    }

    private void register() {
        if (!mIgnoreDefaultExceptionHandler) {
            mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        }
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void unregister() {
        Thread.setDefaultUncaughtExceptionHandler(mDefaultUncaughtExceptionHandler);
    }
}
