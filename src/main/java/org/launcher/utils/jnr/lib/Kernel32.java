package org.launcher.utils.jnr.lib;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Encoding;

public interface Kernel32 {
    Kernel32 INSTANCE = LibraryLoader.create(Kernel32.class).load("kernel32");
    int GetCurrentThreadId();
    int GetLastError();
    Pointer CreateJobObjectW(Pointer lpJobAttributes, @Encoding("UTF-16LE") String lpName);

    int AssignProcessToJobObject(Pointer hJob, Pointer hProcess);

    int CloseHandle(Pointer hObject);

    int SetInformationJobObject(
            Pointer hJob,
            int JobObjectInfoClass,
            Pointer lpJobObjectInfo,
            int cbJobObjectInfoLength
    );
    int CreateProcessW(
            @Encoding("UTF-16LE")
            String applicationName,
            Pointer commandLine,
            Pointer processAttributes,
            Pointer threadAttributes,
            boolean inheritHandles,
            int creationFlags,
            Pointer environment,
            Pointer currentDirectory,
            Pointer startupInfo,
            Pointer processInformation
    );
    Pointer CreateIoCompletionPort(
            Pointer FileHandle,
            Pointer ExistingCompletionPort,
            Pointer CompletionKey,
            int NumberOfConcurrentThreads
    );
    int GetQueuedCompletionStatus(
            Pointer CompletionPort,
            Pointer lpNumberOfBytes,
            Pointer lpCompletionKey,
            Pointer lpOverlapped,
            int dwMilliseconds
    );
    boolean PostQueuedCompletionStatus(
            Pointer CompletionPort,
            int dwNumberOfBytesTransferred,
            Pointer dwCompletionKey,
            Pointer lpOverlapped
    );
    int ResumeThread(Pointer hThread);
}
