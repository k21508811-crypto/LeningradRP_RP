using System;
using System.Collections.Generic;
using System.Threading;
using System.Diagnostics;
using System.Text;
using System.Text.RegularExpressions;
using System.Windows.Forms;

namespace ProverkaDiagnostics;

internal static class Program
{
    [STAThread]
    static void Main()
    {
        ApplicationConfiguration.Initialize();
        Application.Run(new MainForm());
    }
}

sealed class MainForm : Form
{
    readonly TextBox pathBox = new() { Dock = DockStyle.Fill, PlaceholderText = "Выберите папку GTA V Legacy" };
    readonly TextBox output = new() { Dock = DockStyle.Fill, Multiline = true, ReadOnly = true, ScrollBars = ScrollBars.Vertical, Font = new("Consolas", 9) };
    readonly Button scan = new() { Text = "Проверить GTA и плагины", AutoSize = true };
    string report = "";

    public MainForm()
    {
        Text = "Proverka Diagnostics";
        Width = 900; Height = 650; MinimumSize = new(700, 450);
        var top = new TableLayoutPanel { Dock = DockStyle.Top, Height = 90, Padding = new(12), ColumnCount = 3, RowCount = 2 };
        top.ColumnStyles.Add(new(SizeType.Percent, 100)); top.ColumnStyles.Add(new(SizeType.AutoSize)); top.ColumnStyles.Add(new(SizeType.AutoSize));
        top.Controls.Add(pathBox, 0, 0); top.SetColumnSpan(pathBox, 1);
        var browse = new Button { Text = "Обзор…", AutoSize = true };
        browse.Click += (_, _) => { using var d = new FolderBrowserDialog(); if (d.ShowDialog() == DialogResult.OK) pathBox.Text = d.SelectedPath; };
        top.Controls.Add(browse, 1, 0);
        top.Controls.Add(scan, 2, 0);
        var save = new Button { Text = "Сохранить отчёт", AutoSize = true };
        save.Click += (_, _) => SaveReport();
        top.Controls.Add(save, 2, 1);
        Controls.Add(output); Controls.Add(top);
        scan.Click += (_, _) => RunScan();
        output.Text = "Выберите корневую папку GTA V Legacy и нажмите «Проверить».\\r\\nПрограмма только читает файлы и журналы — ничего не удаляет и не изменяет.";
    }

    void RunScan()
    {
        var root = pathBox.Text.Trim().Trim('"');
        if (!Directory.Exists(root)) { MessageBox.Show("Выберите существующую папку игры."); return; }
        scan.Enabled = false;
        try { report = Diagnose(root); output.Text = report; }
        catch (Exception ex) { output.Text = "Ошибка диагностики: " + ex; }
        finally { scan.Enabled = true; }
    }

    string Diagnose(string root)
    {
        var sb = new StringBuilder();
        void Add(string s = "") => sb.AppendLine(s);
        string plugins = Path.Combine(root, "Plugins");
        string lspdfr = Path.Combine(plugins, "LSPDFR");
        Add("PROVERKA DIAGNOSTICS — ОТЧЁТ"); Add("Дата: " + DateTime.Now); Add("Папка игры: " + root); Add();
        CheckFile("GTA5.exe"); CheckFile("RagePluginHook.exe"); CheckFile(Path.Combine("Plugins", "LSPD First Response.dll"));
        CheckFile(Path.Combine("Plugins", "LSPDFR", "ProverkaPoliceMod.dll"));
        CheckFile(Path.Combine("Plugins", "ProverkaPoliceMod.dll"));
        Add("=== ВЕРСИИ ФАЙЛОВ ===");
        foreach (var f in new[] { "GTA5.exe", "RagePluginHook.exe", Path.Combine("Plugins","LSPD First Response.dll"), Path.Combine("Plugins","LSPDFR","ProverkaPoliceMod.dll"), Path.Combine("Plugins","ProverkaPoliceMod.dll") })
        {
            var p = Path.Combine(root, f);
            if (File.Exists(p)) { var v = FileVersionInfo.GetVersionInfo(p); Add($"{f}: {v.FileVersion ?? "версия не указана"}"); }
        }
        Add(); Add("=== ПЛАГИНЫ ===");
        if (Directory.Exists(plugins))
        {
            foreach (var f in Directory.GetFiles(plugins, "*.dll")) Add("Plugins: " + Path.GetFileName(f));
            if (Directory.Exists(lspdfr)) foreach (var f in Directory.GetFiles(lspdfr, "*.dll")) Add("Plugins/LSPDFR: " + Path.GetFileName(f));
            else Add("ПАПКА Plugins/LSPDFR НЕ НАЙДЕНА");
        }
        else Add("ПАПКА Plugins НЕ НАЙДЕНА");

        Add(); Add("=== ЖУРНАЛЫ И ПОСЛЕДНИЕ ОШИБКИ ===");
        var logs = Directory.Exists(root) ? Directory.GetFiles(root, "*.log").Concat(Directory.Exists(plugins) ? Directory.GetFiles(plugins, "*.log", SearchOption.AllDirectories) : Array.Empty<string>()).Distinct().ToList() : new();
        if (logs.Count == 0) Add("Логи .log в корне и Plugins не найдены.");
        foreach (var log in logs.OrderByDescending(File.GetLastWriteTime).Take(12))
        {
            Add(); Add("--- " + Path.GetRelativePath(root, log) + " (" + File.GetLastWriteTime(log) + ") ---");
            try
            {
                var lines = File.ReadLines(log).ToList();
                var hits = lines.Where(x => Regex.IsMatch(x, "error|exception|failed|crash|does not have the required plugin attribute|could not load|unable to load", RegexOptions.IgnoreCase)).TakeLast(35).ToList();
                foreach (var line in (hits.Count > 0 ? hits : lines.TakeLast(8))) Add(line.Length > 500 ? line[..500] : line);
                AnalyzeHits(hits, Add);
            }
            catch (Exception ex) { Add("Не удалось прочитать: " + ex.Message); }
        }
        Add(); Add("=== ВЫВОД ===");
        Add("Отчёт показывает найденные признаки, но не гарантирует определение любой причины вылета.");
        Add("Перед отправкой отчёта проверьте его на личные данные и при необходимости скройте имя пользователя Windows.");
        Add("Никакие файлы игры не изменялись.");
        return sb.ToString();

        void CheckFile(string rel) { Add((File.Exists(Path.Combine(root, rel)) ? "[OK] " : "[НЕТ] ") + rel); }
    }

    static void AnalyzeHits(List<string> hits, Action<string> add)
    {
        var text = string.Join("\n", hits);
        if (text.Contains("required Plugin attribute", StringComparison.OrdinalIgnoreCase))
            add("ДИАГНОЗ: DLL не содержит обязательный атрибут RAGE Plugin Hook. Нужна пересборка плагина с Rage.Attributes.Plugin.");
        if (text.Contains("Could not load file or assembly", StringComparison.OrdinalIgnoreCase) || text.Contains("Unable to load", StringComparison.OrdinalIgnoreCase))
            add("ВОЗМОЖНО: отсутствует зависимость или несовместимая версия библиотеки. Смотрите полное сообщение выше.");
        if (text.Contains("Failed to find outfit", StringComparison.OrdinalIgnoreCase))
            add("ПРЕДУПРЕЖДЕНИЕ: LSPDFR не нашёл указанную форму. Это сообщение само по себе не доказывает причину вылета.");
        if (text.Contains("Exception", StringComparison.OrdinalIgnoreCase) || text.Contains("Fatal", StringComparison.OrdinalIgnoreCase))
            add("НАЙДЕНЫ СТРОКИ ИСКЛЮЧЕНИЙ/ОШИБОК — передайте этот отчёт для разбора.");
    }

    void SaveReport()
    {
        if (string.IsNullOrWhiteSpace(report)) { MessageBox.Show("Сначала выполните проверку."); return; }
        using var d = new SaveFileDialog { Filter = "Текстовый отчёт (*.txt)|*.txt", FileName = "ProverkaDiagnostics-report.txt" };
        if (d.ShowDialog() == DialogResult.OK) { File.WriteAllText(d.FileName, report, Encoding.UTF8); MessageBox.Show("Отчёт сохранён."); }
    }
}