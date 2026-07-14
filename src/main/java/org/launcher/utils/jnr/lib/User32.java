package org.launcher.utils.jnr.lib;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.annotations.Encoding;
import org.launcher.utils.jnr.callback.LowLevelKeyboardProc;
import org.launcher.utils.jnr.struct.ICONINFO;

public interface User32 {
    User32 INSTANCE = LibraryLoader.create(User32.class).stdcall().load("user32");
    int DI_MASK   = 0x0001;
    int DI_IMAGE  = 0x0002;
    int DI_NORMAL = DI_MASK | DI_IMAGE;
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
    long GetWindowLongPtrW(long hwnd, int nIndex);
    long SetWindowLongPtrW(long hwnd, int nIndex, long dwNewLong);
    Pointer GetForegroundWindow();
    int GetWindowLongPtrA(long hWnd, int nIndex);
    int SetWindowLongPtrA(long hWnd, int nIndex, long dwNewLong);
    boolean SetWindowPos(
            long hWnd,
            long hWndInsertAfter,
            int X, int Y, int cx, int cy,
            int uFlags
    );
    Pointer SetWindowsHookExW(
            int idHook,
            LowLevelKeyboardProc lpfn,
            Pointer hMod,
            int dwThreadId
    );
    Pointer CallNextHookEx(
            Pointer hhk,
            int nCode,
            Pointer wParam,
            Pointer lParam
    );
    boolean UnhookWindowsHookEx(Pointer hhk);
    int GetKeyboardState(byte[] lpKeyState);
    long GetKeyboardLayout(int idThread);
    int ToUnicodeEx(
            int wVirtKey,
            int wScanCode,
            byte[] lpKeyState,
            char[] pwszBuff,
            int cchBuff,
            int wFlags,
            long dwhkl
    );
    boolean DestroyIcon(Pointer hIcon);
    boolean DrawIconEx(
            Pointer hdc,
            int x,
            int y,
            Pointer hIcon,
            int cx,
            int cy,
            int step,
            Pointer hBrush,
            int flags
    );
    Pointer GetDC(Pointer hwnd);
    int ReleaseDC(Pointer hwnd, Pointer hdc);
    boolean GetIconInfo(
            Pointer hIcon,
            ICONINFO iconInfo
    );

    int PrivateExtractIconsW(
            @Encoding("UTF-16LE") String file,
            int iconIndex,
            int cxIcon,
            int cyIcon,
            Pointer phicon,
            Pointer piconid,
            int nIcons,
            int flags
    );
}
