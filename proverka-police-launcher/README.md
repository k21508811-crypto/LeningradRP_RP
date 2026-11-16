# Proverka Police Launcher
Publisher: Proverka Studio | Version: 1.0.0 | Windows 10/11 x64

Separate Windows launcher for Proverka Police Mod and GTA V Legacy + LSPDFR + RAGE Plugin Hook.

Initial implementation includes:
- standalone Windows EXE (self-contained .NET 8 WinForms);
- choose and persist GTA V folder;
- check GTA5.exe, RAGEPluginHook.exe, LSPDFR and plugin DLL;
- launch RAGEPluginHook.exe with GTA V as working directory;
- scan recent RPH/LSPDFR log lines for errors;
- display implemented/planned feature status;
- create ZIP backup of LSPDFR, Plugins/plugins and RPH configuration;
- local rule-based assistant for common errors;
- check GitHub latest release metadata.

Build:
dotnet publish proverka-police-launcher/ProverkaPoliceLauncher.csproj -c Release -r win-x64 --self-contained true /p:PublishSingleFile=true

The backup covers mod folders/configuration, not the complete GTA V game directory. The assistant is currently a local diagnostic helper, not a hosted AI model. Update checking is implemented; unattended updating is deliberately deferred until release package verification and rollback are added.