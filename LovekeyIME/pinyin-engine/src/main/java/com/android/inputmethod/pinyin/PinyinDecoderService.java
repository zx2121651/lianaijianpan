package com.android.inputmethod.pinyin;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.android.inputmethod.pinyin.R;

public class PinyinDecoderService extends Service {
    public native static boolean nativeImOpenDecoder(byte fn_sys_dict[], byte fn_usr_dict[]);
    public native static boolean nativeImOpenDecoderFd(FileDescriptor fd, long startOffset, long length, byte fn_usr_dict[]);
    public native static void nativeImSetMaxLens(int maxSpsLen, int maxHzsLen);
    public native static boolean nativeImCloseDecoder();
    public native static int nativeImSearch(byte pyBuf[], int pyLen);
    public native static int nativeImDelSearch(int pos, boolean is_pos_in_splid, boolean clear_fixed_this_step);
    public native static void nativeImResetSearch();
    public native static int nativeImAddLetter(byte ch);
    public native static String nativeImGetPyStr(boolean decoded);
    public native static int nativeImGetPyStrLen(boolean decoded);
    public native static int[] nativeImGetSplStart();
    public native static String nativeImGetChoice(int choiceId);
    public native static int nativeImChoose(int choiceId);
    public native static int nativeImCancelLastChoice();
    public native static int nativeImGetFixedLen();
    public native static boolean nativeImCancelInput();
    public native static boolean nativeImFlushCache();
    public native static int nativeImGetPredictsNum(String fixedStr);
    public native static String nativeImGetPredictItem(int predictNo);

    // Sync related
    public native static String nativeSyncUserDict(byte[] user_dict, String tomerge);
    public native static boolean nativeSyncBegin(byte[] user_dict);
    public native static boolean nativeSyncFinish();
    public native static String nativeSyncGetLemmas();
    public native static int nativeSyncPutLemmas(String tomerge);
    public native static int nativeSyncGetLastCount();
    public native static int nativeSyncGetTotalCount();
    public native static boolean nativeSyncClearLastGot();
    public native static int nativeSyncGetCapacity();

    private final static int MAX_PATH_FILE_LENGTH = 100;
    private static boolean inited = false;

    private String mUsr_dict_file;

    static {
        try {
            System.loadLibrary("jni_pinyinime");
        } catch (UnsatisfiedLinkError ule) {
            Log.e("PinyinDecoderService", "WARNING: Could not load jni_pinyinime natives");
        }
    }

    private boolean getUsrDictFileName(byte usr_dict[]) {
        if (null == usr_dict) return false;
        for (int i = 0; i < mUsr_dict_file.length(); i++)
            usr_dict[i] = (byte) mUsr_dict_file.charAt(i);
        usr_dict[mUsr_dict_file.length()] = 0;
        return true;
    }

    public void initPinyinEngine() {
        if (inited) return;
        byte usr_dict[];
        usr_dict = new byte[MAX_PATH_FILE_LENGTH];

        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.dict_pinyin);
        if (getUsrDictFileName(usr_dict)) {
            inited = nativeImOpenDecoderFd(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength(), usr_dict);
        }
        try {
            afd.close();
        } catch (IOException e) {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mUsr_dict_file = getFileStreamPath("usr_dict.dat").getPath();
        try {
            openFileOutput("dummy", 0).close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        initPinyinEngine();
    }

    @Override
    public void onDestroy() {
        nativeImCloseDecoder();
        inited = false;
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public PinyinDecoderService getService() {
            return PinyinDecoderService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
