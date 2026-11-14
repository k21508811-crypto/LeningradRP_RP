import os
import sys
import ctypes
import subprocess
import threading
import winreg
import tkinter as tk
from tkinter import filedialog, messagebox, ttk
from pathlib import Path
from datetime import datetime, timedelta

APP_NAME = "Proverka Antivirus"
BG = "#101820"
PANEL = "#18232e"
CARD = "#202d39"
TEXT = "#edf3f8"
MUTED = "#9aabb9"
BLUE = "#3b82f6"
GREEN = "#22c55e"
RED = "#ef4444"
TRIAL_DAYS = 7


def is_admin():
    try:
        return bool(ctypes.windll.shell32.IsUserAnAdmin())
    except Exception:
        return False


def defender_cli():
    candidates = [
        Path(os.environ.get("ProgramFiles", r"C:\Program Files")) /
        "Windows Defender" / "MpCmdRun.exe",
        Path(os.environ.get("ProgramData", r"C:\ProgramData")) /
        "Microsoft" / "Windows Defender" / "Platform",
    ]
    direct = candidates[0]
    if direct.exists():
        return str(direct)
    platform = candidates[1]
    if platform.exists():
        versions = sorted(platform.glob("*/MpCmdRun.exe"), reverse=True)
        if versions:
            return str(versions[0])
    return None


def installed_apps():
    paths = [
        (winreg.HKEY_LOCAL_MACHINE, r"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall"),
        (winreg.HKEY_LOCAL_MACHINE, r"SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall"),
        (winreg.HKEY_CURRENT_USER, r"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall"),
    ]
    found = {}
    for hive, path in paths:
        try:
            root = winreg.OpenKey(hive, path)
        except OSError:
            continue
        with root:
            for i in range(winreg.QueryInfoKey(root)[0]):
                try:
                    sub = winreg.EnumKey(root, i)
                    key = winreg.OpenKey(root, sub)
                    with key:
                        name = winreg.QueryValueEx(key, "DisplayName")[0]
                        try:
                            version = winreg.QueryValueEx(key, "DisplayVersion")[0]
                        except OSError:
                            version = "—"
                        try:
                            publisher = winreg.QueryValueEx(key, "Publisher")[0]
                        except OSError:
                            publisher = "—"
                        if name and not name.startswith("KB"):
                            found[name] = (version, publisher)
                except OSError:
                    pass
    return sorted([(n, *v) for n, v in found.items()], key=lambda x: x[0].lower())


class ProverkaApp:
    def __init__(self, root):
        self.root = root
        self.root.title(APP_NAME)
        self.root.geometry("1120x720")
        self.root.minsize(900, 600)
        self.root.configure(bg=BG)
        self.trial_until = datetime.now() + timedelta(days=TRIAL_DAYS)
        self.current_page = "Обзор"
        self.build_ui()
        self.show_page("Обзор")
        if "--full-scan" in sys.argv:
            self.root.after(700, lambda: self.start_scan(str(Path.home()), full=True))

    def build_ui(self):
        style = ttk.Style()
        style.theme_use("clam")
        style.configure("Treeview", background=CARD, foreground=TEXT,
                        fieldbackground=CARD, rowheight=29, borderwidth=0)
        style.configure("Treeview.Heading", background=PANEL, foreground=TEXT,
                        relief="flat", font=("Segoe UI", 9, "bold"))
        style.map("Treeview", background=[("selected", BLUE)])
        sidebar = tk.Frame(self.root, bg="#0b1219", width=235)
        sidebar.pack(side="left", fill="y")
        sidebar.pack_propagate(False)
        brand = tk.Frame(sidebar, bg="#0b1219")
        brand.pack(fill="x", padx=20, pady=(25, 30))
        tk.Label(brand, text="⬡", fg="#f05252", bg="#0b1219",
                 font=("Segoe UI Symbol", 28, "bold")).pack(side="left")
        tk.Label(brand, text="PROVERKA\nANTIVIRUS", fg=TEXT, bg="#0b1219",
                 font=("Segoe UI", 12, "bold"), justify="left").pack(side="left", padx=10)
        self.nav_buttons = {}
        for label, glyph in [
            ("Обзор", "⌂"), ("Проверка", "⌕"), ("Приложения", "▦"),
            ("Карантин", "▣"), ("История", "◷"), ("Pro версия", "★"),
            ("Настройки", "⚙")
        ]:
            b = tk.Button(sidebar, text=f"  {glyph}    {label}", anchor="w",
                          command=lambda x=label: self.show_page(x),
                          bg="#0b1219", fg=TEXT, activebackground=CARD,
                          activeforeground=TEXT, relief="flat", bd=0,
                          font=("Segoe UI", 10), padx=18, pady=12, cursor="hand2")
            b.pack(fill="x", padx=10, pady=2)
            self.nav_buttons[label] = b
        tk.Label(sidebar, text="WINDOWS 10 / 11", bg="#0b1219", fg=MUTED,
                 font=("Segoe UI", 8)).pack(side="bottom", pady=18)

        self.main = tk.Frame(self.root, bg=BG)
        self.main.pack(side="left", fill="both", expand=True)
        top = tk.Frame(self.main, bg=BG)
        top.pack(fill="x", padx=34, pady=(24, 12))
        self.page_title = tk.Label(top, text="", bg=BG, fg=TEXT,
                                   font=("Segoe UI", 21, "bold"))
        self.page_title.pack(side="left")
        self.admin_label = tk.Label(top, text="● Администратор" if is_admin() else "● Обычный режим",
                                    bg=BG, fg=GREEN if is_admin() else MUTED,
                                    font=("Segoe UI", 9))
        self.admin_label.pack(side="right", pady=10)
        self.content = tk.Frame(self.main, bg=BG)
        self.content.pack(fill="both", expand=True, padx=34, pady=12)

    def clear(self):
        for child in self.content.winfo_children():
            child.destroy()

    def card(self, parent, title, body, accent=BLUE):
        frame = tk.Frame(parent, bg=CARD, padx=20, pady=18)
        tk.Frame(frame, bg=accent, width=4).pack(side="left", fill="y", padx=(0, 14))
        inner = tk.Frame(frame, bg=CARD)
        inner.pack(side="left", fill="both", expand=True)
        tk.Label(inner, text=title, bg=CARD, fg=TEXT,
                 font=("Segoe UI", 12, "bold")).pack(anchor="w")
        tk.Label(inner, text=body, bg=CARD, fg=MUTED,
                 font=("Segoe UI", 9), justify="left", wraplength=650).pack(anchor="w", pady=(7, 0))
        return frame

    def button(self, parent, label, command, primary=True):
        return tk.Button(parent, text=label, command=command,
                         bg=BLUE if primary else CARD, fg="white" if primary else TEXT,
                         activebackground="#2563eb" if primary else PANEL,
                         activeforeground="white", relief="flat", bd=0,
                         padx=18, pady=11, font=("Segoe UI", 9, "bold"),
                         cursor="hand2")

    def show_page(self, page):
        self.current_page = page
        self.page_title.config(text=page)
        for name, b in self.nav_buttons.items():
            b.config(bg=CARD if name == page else "#0b1219")
        self.clear()
        if page == "Обзор":
            self.overview()
        elif page == "Проверка":
            self.scan_page()
        elif page == "Приложения":
            self.apps_page()
        elif page == "Pro версия":
            self.pro_page()
        elif page == "Карантин":
            self.info_page("Карантин", "Обнаруженные угрозы обрабатываются Microsoft Defender. Этот прототип не перемещает файлы самостоятельно, чтобы не повредить систему.")
        elif page == "История":
            self.info_page("История проверок", "История появится здесь после выполнения проверок. Результаты Defender отображаются в приложении «Безопасность Windows».")
        else:
            self.info_page("Настройки", "Защита выполняется встроенным Microsoft Defender. Proverka не отключает системную защиту и не меняет настройки безопасности Windows.")

    def overview(self):
        hero = tk.Frame(self.content, bg="#172b3c", padx=25, pady=25)
        hero.pack(fill="x", pady=(0, 18))
        tk.Label(hero, text="◆", bg="#172b3c", fg=GREEN,
                 font=("Segoe UI", 36)).pack(side="left", padx=(0, 20))
        info = tk.Frame(hero, bg="#172b3c")
        info.pack(side="left", fill="x", expand=True)
        tk.Label(info, text="Безопасность Windows — под контролем",
                 bg="#172b3c", fg=TEXT, font=("Segoe UI", 17, "bold")).pack(anchor="w")
        tk.Label(info, text="Proverka использует Microsoft Defender для проверки файлов.",
                 bg="#172b3c", fg="#b8c8d6", font=("Segoe UI", 10)).pack(anchor="w", pady=5)
        self.button(hero, "Быстрая проверка", lambda: self.start_scan(str(Path.home()))).pack(side="right")
        self.card(self.content, "Защита от вирусов", "Microsoft Defender — основной антивирусный движок. Proverka не заменяет его и не отключает.", GREEN).pack(fill="x", pady=7)
        self.card(self.content, "Пробный период Pro", f"Все демонстрационные Pro-разделы открыты. До окончания теста: {TRIAL_DAYS} дней.", BLUE).pack(fill="x", pady=7)
        self.card(self.content, "Доступ к файлам", "Проверяются только выбранные вами папки. Для защищённых системных областей Windows может запросить подтверждение UAC.", "#eab308").pack(fill="x", pady=7)
        self.button(self.content, "Открыть Безопасность Windows", self.open_windows_security, False).pack(anchor="w", pady=15)

    def scan_page(self):
        self.card(self.content, "Проверка файлов", "Выберите папку или запустите полную проверку профиля пользователя через Microsoft Defender.").pack(fill="x", pady=(0, 16))
        row = tk.Frame(self.content, bg=BG)
        row.pack(anchor="w")
        self.button(row, "Выбрать папку", self.choose_folder).pack(side="left", padx=(0, 10))
        self.button(row, "Проверить папку пользователя", lambda: self.start_scan(str(Path.home())), False).pack(side="left")
        self.button(self.content, "Полная проверка (UAC)", self.request_full_scan, False).pack(anchor="w", pady=12)
        self.status = tk.Label(self.content, text="Ожидание проверки", bg=BG, fg=MUTED,
                               font=("Segoe UI", 10), wraplength=700, justify="left")
        self.status.pack(anchor="w", pady=18)
        self.progress = ttk.Progressbar(self.content, mode="indeterminate", length=500)
        self.progress.pack(anchor="w")

    def choose_folder(self):
        folder = filedialog.askdirectory(title="Выберите папку для проверки")
        if folder:
            self.start_scan(folder)

    def request_full_scan(self):
        if is_admin():
            self.start_scan(str(Path.home()), full=True)
            return
        answer = messagebox.askyesno("Подтверждение UAC",
            "Для полной проверки Windows потребуется запуск от имени администратора. Продолжить?")
        if answer:
            params = ' '.join(f'"{a}"' for a in sys.argv[1:] if a != "--full-scan")
            ctypes.windll.shell32.ShellExecuteW(None, "runas", sys.executable,
                                                f'"{os.path.abspath(sys.argv[0])}" --full-scan {params}',
                                                None, 1)

    def start_scan(self, target, full=False):
        if not hasattr(self, "status"):
            self.show_page("Проверка")
        self.status.config(text=f"Проверка: {target}\nЗапускается Microsoft Defender…", fg=TEXT)
        self.progress.start(12)
        threading.Thread(target=self.scan_worker, args=(target,), daemon=True).start()

    def scan_worker(self, target):
        cli = defender_cli()
        if not cli:
            result = "Microsoft Defender CLI не найден. Проверьте состояние «Безопасности Windows»."
        else:
            try:
                proc = subprocess.run([cli, "-Scan", "-ScanType", "3", "-File", target],
                                      capture_output=True, text=True, timeout=60 * 60)
                output = (proc.stdout + "\n" + proc.stderr).strip()
                result = output[-1800:] if output else f"Проверка завершена. Код Defender: {proc.returncode}"
            except subprocess.TimeoutExpired:
                result = "Проверка длится дольше часа и была остановлена приложением."
            except Exception as exc:
                result = f"Не удалось запустить проверку: {exc}"
        self.root.after(0, lambda: self.scan_done(result))

    def scan_done(self, result):
        if hasattr(self, "progress"):
            self.progress.stop()
        if hasattr(self, "status"):
            self.status.config(text=result, fg=TEXT)
        messagebox.showinfo("Результат проверки", result[-1200:])

    def apps_page(self):
        self.card(self.content, "Установленные приложения", "Список программ из реестра Windows. Это инвентаризация, а не доказательство безопасности программы.").pack(fill="x", pady=(0, 12))
        bar = tk.Frame(self.content, bg=BG)
        bar.pack(fill="x", pady=(0, 8))
        self.button(bar, "Обновить список", self.load_apps).pack(side="left")
        self.app_count = tk.Label(bar, text="", bg=BG, fg=MUTED, font=("Segoe UI", 9))
        self.app_count.pack(side="right")
        columns = ("name", "version", "publisher")
        self.table = ttk.Treeview(self.content, columns=columns, show="headings")
        for col, title, width in [("name", "Приложение", 330), ("version", "Версия", 130), ("publisher", "Издатель", 260)]:
            self.table.heading(col, text=title)
            self.table.column(col, width=width, anchor="w")
        self.table.pack(fill="both", expand=True)
        self.load_apps()

    def load_apps(self):
        if not hasattr(self, "table"):
            return
        self.table.delete(*self.table.get_children())
        rows = installed_apps()
        for name, version, publisher in rows:
            self.table.insert("", "end", values=(name, version, publisher))
        self.app_count.config(text=f"Найдено программ: {len(rows)}")

    def pro_page(self):
        self.card(self.content, "Pro — пробный период", f"Тестовый режим открыт на {TRIAL_DAYS} дней в этой демонстрационной сборке. Таймер пока локальный и не является серверной лицензией.", BLUE).pack(fill="x", pady=(0, 14))
        for title, body in [
            ("Расширенная проверка", "Доступ к запуску полной проверки через подтверждение UAC."),
            ("Список приложений", "Просмотр установленных программ Windows."),
            ("История и карантин", "Разделы интерфейса подготовлены; управление угрозами оставлено Microsoft Defender."),
        ]:
            self.card(self.content, title, body, GREEN).pack(fill="x", pady=6)

    def info_page(self, title, body):
        self.card(self.content, title, body).pack(fill="x", pady=8)

    def open_windows_security(self):
        try:
            os.startfile("windowsdefender:")
        except Exception:
            messagebox.showinfo("Безопасность Windows", "Откройте Пуск → Безопасность Windows.")


if __name__ == "__main__":
    if os.name != "nt":
        raise SystemExit("Proverka Antivirus предназначен для Windows 10/11.")
    root = tk.Tk()
    app = ProverkaApp(root)
    root.mainloop()
