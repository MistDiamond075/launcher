package org.launcher.service.win;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;

public interface User32 {
    User32 INSTANCE = LibraryLoader.create(User32.class).stdcall().load("user32");
    interface EnumWindowsProc {
        @Delegate
        int invoke(Pointer hWnd, Pointer lParam);
    }
    interface WinEventProc {
        @Delegate
        void invoke(
                Pointer hWinEventHook,
                int event,
                Pointer hwnd,
                int idObject,
                int idChild,
                int idEventThread,
                int dwmsEventTime
        );
    }
    Pointer SetWinEventHook(
            int eventMin,
            int eventMax,
            Pointer hmodWinEventProc,
            WinEventProc lpfnWinEventProc,
            int idProcess,
            int idThread,
            int dwFlags
    );
    Pointer GetParent(Pointer hWnd);
    int UnhookWinEvent(Pointer hWinEventHook);
    int IsWindowVisible(Pointer hWnd);
    int GetWindowTextLengthW(Pointer hWnd);
    int GetWindowTextW(Pointer hWnd, char[] lpString, int nMaxCount);
    int EnumWindows(EnumWindowsProc lpEnumFunc, Pointer lParam);
    int GetMessageW(Pointer lpMsg, Pointer hWnd, int wMsgFilterMin, int wMsgFilterMax);
    int PeekMessageW(Pointer lpMsg, Pointer hWnd, int wMsgFilterMin, int wMsgFilterMax, int wRemoveMsg);
    int TranslateMessage(Pointer lpMsg);
    int DispatchMessageW(Pointer lpMsg);
    int PostThreadMessageW(int idThread, int msg, int wParam, int lParam);
    int GetWindowThreadProcessId(Pointer hWnd, int[] lpdwProcessId);
}
