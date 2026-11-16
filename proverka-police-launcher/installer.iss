#define AppName "Proverka Police Suite"
#define AppVersion "1.0.0"
#define Publisher "Proverka Studio"
#define ExeName "ProverkaPoliceLauncher.exe"
[Setup]
AppId={{8C2CDB2B-8F9A-4C78-AC9A-1DA0A4A1C102}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher={#Publisher}
DefaultDirName={autopf}\Proverka Studio\Proverka Police Suite
DefaultGroupName=Proverka Police Suite
OutputDir=..\dist
OutputBaseFilename=ProverkaPoliceSuite-Setup
Compression=lzma2
SolidCompression=yes
ArchitecturesInstallIn64BitMode=x64
WizardStyle=modern
UninstallDisplayIcon={app}\{#ExeName}
[Files]
Source: "..\publish\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
[Tasks]
Name: "desktopicon"; Description: "Создать ярлык на рабочем столе"; GroupDescription: "Дополнительные задачи:"
[Icons]
Name: "{group}\Proverka Police Launcher"; Filename: "{app}\{#ExeName}"
Name: "{autodesktop}\Proverka Police Launcher"; Filename: "{app}\{#ExeName}"; Tasks: desktopicon
[Run]
Filename: "{app}\{#ExeName}"; Description: "Запустить Proverka Police Launcher"; Flags: postinstall nowait skipifsilent