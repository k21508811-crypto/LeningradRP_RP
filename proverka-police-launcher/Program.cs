using System.Diagnostics;
using System.IO.Compression;
using System.Net.Http;
using System.Text.Json;

namespace ProverkaPoliceLauncher;
internal static class Program
{
 [STAThread] static void Main(){ApplicationConfiguration.Initialize();Application.Run(new LauncherForm());}
}
public sealed class LauncherForm:Form
{
 const string Owner="k21508811-crypto", Repo="LeningradRP_RP", Dll="ProverkaPoliceMod.dll";
 readonly string cfg=Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),"ProverkaStudio","PoliceLauncher","settings.json");
 readonly TextBox log=new(){Multiline=true,ReadOnly=true,ScrollBars=ScrollBars.Vertical,BackColor=Color.FromArgb(10,17,29),ForeColor=Color.Gainsboro,BorderStyle=BorderStyle.FixedSingle,Dock=DockStyle.Fill,Font=new Font("Consolas",9)};
 readonly TextBox pathBox=new(){Width=410,ReadOnly=true};
 readonly Label status=new(){Text="Выбери папку GTA V",ForeColor=Color.Silver,AutoSize=true};
 string? game;
 readonly Color panel=Color.FromArgb(17,27,43),blue=Color.FromArgb(0,125,235);
 public LauncherForm()
 {
  Text="Proverka Police Launcher";Size=new Size(1100,740);MinimumSize=new Size(940,620);StartPosition=FormStartPosition.CenterScreen;BackColor=Color.FromArgb(7,12,22);ForeColor=Color.White;Font=new Font("Segoe UI",10);
  LoadConfig();BuildUi();Write("Proverka Police Launcher 1.0.0 — Proverka Studio");
 }
 void BuildUi()
 {
  var root=new TableLayoutPanel{Dock=DockStyle.Fill,ColumnCount=2,Padding=new Padding(14)};root.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute,235));root.ColumnStyles.Add(new ColumnStyle(SizeType.Percent,100));Controls.Add(root);
  var side=new Panel{Dock=DockStyle.Fill,BackColor=panel,Padding=new Padding(12)};root.Controls.Add(side,0,0);
  side.Controls.Add(new Label{Text="✦ PROVERKA\n   POLICE SUITE",Font=new Font("Segoe UI",17,FontStyle.Bold),ForeColor=Color.FromArgb(65,166,255),Dock=DockStyle.Top,Height=90});
  var menu=new FlowLayoutPanel{Dock=DockStyle.Top,FlowDirection=FlowDirection.TopDown,WrapContents=false,Height=390};side.Controls.Add(menu);
  Nav(menu,"Главная",Home);Nav(menu,"Проверка плагина",CheckPlugin);Nav(menu,"Просмотр функций",Features);Nav(menu,"Ошибки файлов",CheckLogs);Nav(menu,"Резервная копия",Backup);Nav(menu,"AI Police Assistant",Assistant);Nav(menu,"Обновления",CheckUpdates);
  side.Controls.Add(new Label{Text="Proverka Studio • 1.0.0",ForeColor=Color.Gray,Dock=DockStyle.Bottom,Height=28});
  var main=new TableLayoutPanel{Dock=DockStyle.Fill,RowCount=4,Padding=new Padding(16,5,5,5)};main.RowStyles.Add(new RowStyle(SizeType.Absolute,48));main.RowStyles.Add(new RowStyle(SizeType.Absolute,66));main.RowStyles.Add(new RowStyle(SizeType.Absolute,70));main.RowStyles.Add(new RowStyle(SizeType.Percent,100));root.Controls.Add(main,1,0);
  main.Controls.Add(new Label{Text="PROVERKA POLICE LAUNCHER",Font=new Font("Segoe UI",18,FontStyle.Bold),AutoSize=true},0,0);
  var p=new FlowLayoutPanel{Dock=DockStyle.Fill,BackColor=panel,Padding=new Padding(10),WrapContents=false};p.Controls.Add(new Label{Text="Папка GTA V:",AutoSize=true,Padding=new Padding(0,7,4,0)});p.Controls.Add(pathBox);p.Controls.Add(MakeButton("Обзор…",ChooseGame));main.Controls.Add(p,0,1);
  var actions=new FlowLayoutPanel{Dock=DockStyle.Fill,Padding=new Padding(0,8,0,4)};actions.Controls.Add(MakeButton("▶ ЗАЙТИ В ИГРУ",Launch,true));actions.Controls.Add(MakeButton("Проверить",CheckPlugin));actions.Controls.Add(MakeButton("Обновления",CheckUpdates));main.Controls.Add(actions,0,2);
  main.Controls.Add(log,0,3);status.Dock=DockStyle.Bottom;status.Padding=new Padding(0,8,0,0);side.Controls.Add(status);Home();
 }
 void Nav(Control p,string text,Action a){var b=MakeButton(text,a);b.Width=205;b.Height=40;b.Margin=new Padding(0,3,0,4);p.Controls.Add(b);}
 Button MakeButton(string text,Action a,bool primary=false){var b=new Button{Text=text,Width=205,Height=45,FlatStyle=FlatStyle.Flat,BackColor=primary?blue:Color.FromArgb(31,48,70),ForeColor=Color.White,Font=new Font("Segoe UI",10,FontStyle.Bold),Cursor=Cursors.Hand,Margin=new Padding(4)};b.FlatAppearance.BorderColor=Color.FromArgb(48,137,220);b.Click+=(_,_)=>a();return b;}
 void Write(string s){if(InvokeRequired){BeginInvoke(()=>Write(s));return;}log.AppendText($"[{DateTime.Now:HH:mm:ss}] {s}{Environment.NewLine}");}
 void LoadConfig(){try{if(File.Exists(cfg)){using var d=JsonDocument.Parse(File.ReadAllText(cfg));game=d.RootElement.GetProperty("gamePath").GetString();}}catch{}pathBox.Text=game??"";}
 void SaveConfig(){Directory.CreateDirectory(Path.GetDirectoryName(cfg)!);File.WriteAllText(cfg,JsonSerializer.Serialize(new{gamePath=game}));}
 bool Ready(){if(string.IsNullOrWhiteSpace(game)||!Directory.Exists(game)){MessageBox.Show("Сначала выбери корневую папку GTA V.");return false;}return true;}
 void ChooseGame(){using var d=new FolderBrowserDialog{Description="Выбери корневую папку GTA V Legacy"};if(d.ShowDialog()!=DialogResult.OK)return;if(!File.Exists(Path.Combine(d.SelectedPath,"GTA5.exe"))){MessageBox.Show("В папке не найден GTA5.exe.");return;}game=d.SelectedPath;pathBox.Text=game;SaveConfig();status.Text="Папка игры сохранена";Write("Выбрана папка: "+game);}
 void Home(){Write("Главная панель. Выбери GTA V и проверь установку.");Write("Поддержка: GTA V Legacy + LSPDFR + RAGE Plugin Hook.");}
 void CheckPlugin(){if(!Ready())return;Write("=== Проверка плагина ===");Write(File.Exists(Path.Combine(game!,"GTA5.exe"))?"GTA5.exe: OK":"GTA5.exe: отсутствует");Write(File.Exists(Path.Combine(game!,"RAGEPluginHook.exe"))?"RAGE Plugin Hook: OK":"RAGE Plugin Hook: отсутствует");Write(Directory.Exists(Path.Combine(game!,"LSPDFR"))?"LSPDFR: найдена":"LSPDFR: отсутствует");var dll=Path.Combine(game!,"Plugins","LSPDFR",Dll);if(!File.Exists(dll))dll=Path.Combine(game!,"plugins","LSPDFR",Dll);Write(File.Exists(dll)?"ProverkaPoliceMod.dll: найдена":"ProverkaPoliceMod.dll: НЕ НАЙДЕНА");status.Text=File.Exists(dll)?"DLL найдена":"Требуется установка DLL";}
 void Launch(){if(!Ready())return;var exe=Path.Combine(game!,"RAGEPluginHook.exe");if(!File.Exists(exe)){Write("ОШИБКА: RAGEPluginHook.exe не найден.");return;}CheckPlugin();try{Process.Start(new ProcessStartInfo(exe){WorkingDirectory=game!,UseShellExecute=true});Write("RAGE Plugin Hook запущен.");status.Text="Команда запуска отправлена";}catch(Exception e){Write("Ошибка запуска: "+e.Message);}}
 void Features(){Write("=== Возможности ===");foreach(var s in new[]{"Проверка установки и диагностика — реализуется в лаунчере","Создание резервных копий модификаций — реализуется","Запуск через RAGE Plugin Hook — реализуется","Напарники, патруль, подкрепление и диспетчер — план игрового мода","AI Police Assistant — локальная справка в первой версии"})Write("• "+s);}
 void CheckLogs(){if(!Ready())return;Write("=== Ошибки в логах ===");foreach(var f in new[]{Path.Combine(game!,"RagePluginHook.log"),Path.Combine(game!,"LSPDFR","lspdfr.log")}){if(!File.Exists(f)){Write("Не найден: "+f);continue;}Write("Файл: "+f);try{foreach(var line in File.ReadLines(f).TakeLast(300).Where(x=>x.Contains("error",StringComparison.OrdinalIgnoreCase)||x.Contains("exception",StringComparison.OrdinalIgnoreCase)||x.Contains("Proverka",StringComparison.OrdinalIgnoreCase)))Write(line);}catch(Exception e){Write(e.Message);}}}
 void Backup(){if(!Ready())return;using var d=new FolderBrowserDialog{Description="Папка для ZIP-копии модификаций"};if(d.ShowDialog()!=DialogResult.OK)return;try{var zip=Path.Combine(d.SelectedPath,"Proverka-GTA-Mods-"+DateTime.Now.ToString("yyyyMMdd-HHmmss")+".zip");using var a=ZipFile.Open(zip,ZipArchiveMode.Create);foreach(var rel in new[]{"LSPDFR","Plugins","plugins","RagePluginHook.ini","RagePluginHook.exe.config"}){var src=Path.Combine(game!,rel);if(File.Exists(src))a.CreateEntryFromFile(src,rel);else if(Directory.Exists(src))foreach(var f in Directory.EnumerateFiles(src,"*",SearchOption.AllDirectories))a.CreateEntryFromFile(f,Path.GetRelativePath(game!,f));}Write("Резервная копия создана: "+zip);MessageBox.Show("Копия создана:\n"+zip);}catch(Exception e){Write("Ошибка резервирования: "+e.Message);}}
 void Assistant(){var q=Microsoft.VisualBasic.Interaction.InputBox("Вставь ошибку или опиши проблему:","AI Police Assistant");if(string.IsNullOrWhiteSpace(q))return;var s=q.ToLowerInvariant();var answer=s.Contains("plugin attribute")?"Проверь атрибут RagePluginHook.Plugin и целевую версию RPH.":s.Contains("file not found")||s.Contains("не найден")?"Проверь путь игры и наличие требуемого файла.":s.Contains("exception")||s.Contains("ошибка")?"Найди первую ошибку в RagePluginHook.log и проверь совместимость версий.":"Приложи RagePluginHook.log и LSPDFR.log для диагностики. Это локальный справочник, не облачная ИИ-модель.";Write("Запрос: "+q);Write("Рекомендация: "+answer);}
 async void CheckUpdates(){Write("Проверка GitHub Releases...");try{using var h=new HttpClient();h.DefaultRequestHeaders.UserAgent.ParseAdd("ProverkaPoliceLauncher/1.0");var json=await h.GetStringAsync($"https://api.github.com/repos/{Owner}/{Repo}/releases/latest");using var d=JsonDocument.Parse(json);var tag=d.RootElement.GetProperty("tag_name").GetString();var url=d.RootElement.GetProperty("html_url").GetString();Write("Последний релиз: "+tag);Write(url??"");Write("Пока выполняется проверка релиза; автоматическая установка обновлений будет добавлена после настройки подписанных пакетов.");}catch(Exception e){Write("Проверка обновлений не удалась: "+e.Message);}}
}