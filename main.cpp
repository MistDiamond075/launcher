#define WINVER 0x0600
#define _WIN32_WINNT 0x0600
#include <windows.h>
#include <vector>
#include <string>
#include <thread>
#include <atomic>
#include <chrono>
#include <mutex>
#include <filesystem>
#include <fstream>
#include <iomanip>

constexpr wchar_t PIPE_NAME[] = LR"(\\.\pipe\LauncherWatchdog)";
constexpr DWORD HEARTBEAT_TIMEOUT = 10000;
constexpr int MAX_RESTARTS = 5;
std::atomic<int> restartCount{0};
std::atomic<bool> shutdownRequested = std::atomic_bool(false);
std::wstring launcherPath;
std::wstring launcherDir;
std::wstring logPath;
std::atomic<uint64_t> lastHeartbeat{0};

PROCESS_INFORMATION javaProcess{};

std::mutex logMutex;

bool initLauncherPath()
{
    wchar_t path[MAX_PATH];

    DWORD len = GetModuleFileNameW(nullptr, path, MAX_PATH);
    if (len == 0 || len == MAX_PATH)
        return false;

    launcherDir.assign(path);

    size_t pos = launcherDir.find_last_of(L"\\/");
    if (pos == std::wstring::npos)
        return false;

    launcherDir.erase(pos);

    launcherPath = launcherDir + L"\\launcher.exe";
    logPath = launcherDir + L"\\watchdog.log";

    return true;
}

enum Command : uint8_t
{
    CMD_HEARTBEAT = 1,
    CMD_EXIT      = 2
};

void log(const std::wstring& msg)
{
   std::lock_guard<std::mutex> lock(logMutex);

    std::wofstream out(
        std::filesystem::path(logPath),
        std::ios::app
    );

    if (!out.is_open())
        return;

    SYSTEMTIME st;
    GetLocalTime(&st);

    out
        << L"["
        << std::setfill(L'0')
        << std::setw(2) << st.wDay << L"."
        << std::setw(2) << st.wMonth << L"."
        << std::setw(4) << st.wYear << L" "
        << std::setw(2) << st.wHour << L":"
        << std::setw(2) << st.wMinute << L":"
        << std::setw(2) << st.wSecond
        << L"] "
        << msg
        << std::endl;
}

bool startJava(const std::wstring& arguments)
{
     STARTUPINFOW si{};
    si.cb = sizeof(si);

    std::wstring cmd = L"\"" + launcherPath + L"\"";

    if (!arguments.empty())
    {
        cmd += L" ";
        cmd += arguments;
    }

    std::vector<wchar_t> cmdLine(cmd.begin(), cmd.end());
    cmdLine.push_back(L'\0');
    restartCount++;
    if (!CreateProcessW(
            launcherPath.c_str(),
            cmdLine.data(),
            nullptr,
            nullptr,
            FALSE,
            0,
            nullptr,
            launcherDir.c_str(),
            &si,
            &javaProcess))
    {
        log(L"CreateProcess failed. Error=" + std::to_wstring(GetLastError()));
        return false;
    }

    log(L"Launcher started. PID=" + std::to_wstring(javaProcess.dwProcessId));
    return true;
}

void stopJava()
{
    log(L"Stopping process...");
    if (javaProcess.hProcess != nullptr)
    {
        log(L"TerminateProcess()");
        TerminateProcess(javaProcess.hProcess, 0);
        WaitForSingleObject(javaProcess.hProcess, 3000);

        CloseHandle(javaProcess.hProcess);
        CloseHandle(javaProcess.hThread);
log(L"Process stopped");
        javaProcess = {};
    }
}

void ipcServer()
{
    while (!shutdownRequested)
    {
        HANDLE pipe = CreateNamedPipeW(
            PIPE_NAME,
            PIPE_ACCESS_INBOUND,
            PIPE_TYPE_MESSAGE | PIPE_READMODE_MESSAGE | PIPE_WAIT,
            1,
            0,
            256,
            0,
            nullptr);
log(L"Pipe created");
        if (pipe == INVALID_HANDLE_VALUE)
            continue;

        BOOL connected = ConnectNamedPipe(pipe, nullptr);
log(L"Client connected");
        if (!connected &&
            GetLastError() != ERROR_PIPE_CONNECTED)
        {
            log(L"ConnectNamedPipe failed. Error=" + std::to_wstring(GetLastError()));
            CloseHandle(pipe);
            continue;
        }
restartCount = 0;
        char buffer[256];

        while (true)
        {
            DWORD available = 0;

            if (!PeekNamedPipe(
                    pipe,
                    nullptr,
                    0,
                    nullptr,
                    &available,
                    nullptr))
            {
                log(L"PeekNamedPipe failed. Error=" + std::to_wstring(GetLastError()));
                break;
            }

            if (available == 0)
            {
                Sleep(50);
                continue;
            }

            uint8_t cmd;
            DWORD read = 0;

            if (!ReadFile(pipe, &cmd, sizeof(cmd), &read, nullptr))
                break;

            if (read != sizeof(cmd))
                continue;

            switch (cmd)
            {
                case CMD_HEARTBEAT:
                    lastHeartbeat = GetTickCount64();
                    log(L"Heartbeat");
                    break;

                case CMD_EXIT:
                    log(L"Shutdown requested");
                    shutdownRequested = true;
                    break;

                default:
                    log(L"Unknown command");
                    break;
            }
            if(shutdownRequested){
                break;
            }
        }
log(L"Client disconnected");
        DisconnectNamedPipe(pipe);
        CloseHandle(pipe);
    }
}

int WINAPI wWinMain(
    HINSTANCE hInstance,
    HINSTANCE,
    PWSTR lpCmdLine,
    int nCmdShow)
{
    ShowWindow(GetConsoleWindow(), SW_HIDE);
    std::wstring arguments = lpCmdLine;
    lastHeartbeat = GetTickCount64();

    if (!initLauncherPath())
{
    MessageBoxW(
        nullptr,
        L"Unable to determine launcher path.",
        L"Watchdog",
        MB_ICONERROR);

    return 1;
}

    std::thread(ipcServer).detach();
    log(std::wstring(launcherPath.begin(),launcherPath.end()));
    log(std::wstring(launcherDir.begin(),launcherDir.end()));
    startJava(arguments);

    while (!shutdownRequested)
    {
            if (restartCount >= MAX_RESTARTS){
                MessageBoxW(
                    nullptr,
                    L"Failed to start Launcher.\n"
                    L"Too many failed attempts",
                    L"Watchdog",
                    MB_ICONERROR | MB_OK);

                break;
        }
        if (javaProcess.hProcess && WaitForSingleObject(javaProcess.hProcess, 0) == WAIT_OBJECT_0)
        {
            stopJava();
            startJava(arguments);
            lastHeartbeat = GetTickCount64();
        }

        uint64_t now = GetTickCount64();

        if (now - lastHeartbeat > HEARTBEAT_TIMEOUT)
        {
            stopJava();
            startJava(arguments);
            lastHeartbeat = GetTickCount64();
        }
        Sleep(1000);
    }
    //stopJava();
    return 0;
}
