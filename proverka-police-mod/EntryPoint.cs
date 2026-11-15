using System;
using System.Collections.Generic;
using System.Speech.Synthesis;
using System.Windows.Forms;
using Rage;

namespace ProverkaPoliceMod
{
    public static class EntryPoint
    {
        private static readonly List<string> Menu = new List<string>
        {
            "Нанять напарника",
            "Напарник: следовать / ждать",
            "Посадить напарника в машину",
            "Удалить напарника",
            "Транспорт: POLICE",
            "Транспорт: POLICE2",
            "Транспорт: POLICE3",
            "Диспетчер: запросить подкрепление",
            "Диспетчер: отменить подкрепление",
            "Озвучить произвольный текст",
            "Закрыть меню"
        };

        private static readonly List<Ped> Partners = new List<Ped>();
        private static readonly List<Ped> BackupPeds = new List<Ped>();
        private static readonly List<Vehicle> BackupVehicles = new List<Vehicle>();
        private static readonly List<Vehicle> SpawnedVehicles = new List<Vehicle>();
        private static int selected;
        private static bool menuOpen;
        private static bool partnersFollowing = true;
        private static DateTime nextMenuDraw = DateTime.MinValue;
        private static readonly SpeechSynthesizer Speaker = new SpeechSynthesizer();
        private static bool f12WasDown, escapeWasDown, upWasDown, downWasDown, enterWasDown;

        public static void Main()
        {
            Game.DisplayNotification("Proverka Police Mod загружен. F12 — меню.");
            try
            {
                while (true)
                {
                    try { Tick(); }
                    catch (Exception ex)
                    {
                        Game.LogTrivial("Proverka Police Mod: " + ex);
                        Game.DisplayNotification("Proverka Police Mod: " + ex.Message);
                    }
                    GameFiber.Sleep(50);
                }
            }
            finally { Cleanup(); }
        }

        private static void Tick()
        {
            bool f12 = Game.IsKeyDown(Keys.F12);
            if (f12 && !f12WasDown) { menuOpen = !menuOpen; nextMenuDraw = DateTime.MinValue; DrawMenu(); }
            f12WasDown = f12;
            if (!menuOpen) return;
            if (DateTime.UtcNow >= nextMenuDraw) DrawMenu();

            bool esc = Game.IsKeyDown(Keys.Escape);
            if (esc && !escapeWasDown) { menuOpen = false; Game.DisplaySubtitle("Меню закрыто", 1200); }
            escapeWasDown = esc;
            if (!menuOpen) return;

            bool up = Game.IsKeyDown(Keys.Up);
            if (up && !upWasDown) { selected = (selected + Menu.Count - 1) % Menu.Count; DrawMenu(); }
            upWasDown = up;

            bool down = Game.IsKeyDown(Keys.Down);
            if (down && !downWasDown) { selected = (selected + 1) % Menu.Count; DrawMenu(); }
            downWasDown = down;

            bool enter = Game.IsKeyDown(Keys.Enter);
            if (enter && !enterWasDown) ExecuteSelected();
            enterWasDown = enter;
        }

        private static void DrawMenu()
        {
            string text = "~r~PROVERKA POLICE MOD~s~\nF12/ESC — закрыть | ↑↓ — выбор | ENTER — выполнить\n";
            for (int i = 0; i < Menu.Count; i++)
                text += (i == selected ? "~y~> " : "~w~  ") + Menu[i] + (i == selected ? "~s~" : "") + "\n";
            Game.DisplaySubtitle(text, 2200);
            nextMenuDraw = DateTime.UtcNow.AddMilliseconds(1800);
        }

        private static void ExecuteSelected()
        {
            try
            {
                switch (selected)
                {
                    case 0: HirePartner(); break;
                    case 1: TogglePartnerFollow(); break;
                    case 2: PutPartnerInVehicle(); break;
                    case 3: RemovePartners(); break;
                    case 4: SpawnPoliceVehicle("POLICE"); break;
                    case 5: SpawnPoliceVehicle("POLICE2"); break;
                    case 6: SpawnPoliceVehicle("POLICE3"); break;
                    case 7: RequestBackup(); break;
                    case 8: CancelBackup(); break;
                    case 9: SpeakCustomText(); break;
                    case 10: menuOpen = false; break;
                }
            }
            catch (Exception ex)
            {
                Game.LogTrivial("Proverka Police Mod action failed: " + ex);
                Game.DisplayNotification("Ошибка: " + ex.Message);
            }
            if (menuOpen) DrawMenu();
        }

        private static Ped PlayerPed { get { return Game.LocalPlayer.Character; } }

        private static Model LoadModel(string name)
        {
            Model model = new Model(name);
            if (!model.IsInCdImage) throw new InvalidOperationException("Модель не найдена: " + name);
            model.LoadAndWait();
            return model;
        }

        private static void HirePartner()
        {
            if (Partners.Count >= 3) { Game.DisplayNotification("Максимум 3 напарника."); return; }
            Model model = LoadModel("s_m_y_cop_01");
            Ped ped = new Ped(model, PlayerPed.GetOffsetPosition(new Vector3(2f + Partners.Count, 1f, 0f)), PlayerPed.Heading);
            ped.IsPersistent = true;
            ped.Tasks.FollowToOffsetFromEntity(PlayerPed, new Vector3(1.5f, -1.5f - Partners.Count, 0f));
            Partners.Add(ped);
            model.Dismiss();
            Game.DisplayNotification("Напарник присоединился. Всего: " + Partners.Count);
        }

        private static void TogglePartnerFollow()
        {
            if (Partners.Count == 0) { Game.DisplayNotification("Сначала наймите напарника."); return; }
            foreach (Ped ped in Partners)
            {
                if (ped == null || !ped.Exists()) continue;
                if (partnersFollowing) ped.Tasks.Clear();
                else ped.Tasks.FollowToOffsetFromEntity(PlayerPed, new Vector3(1.5f, -1.5f, 0f));
            }
            partnersFollowing = !partnersFollowing;
            Game.DisplayNotification(partnersFollowing ? "Напарники следуют за вами." : "Напарники ждут на месте.");
        }

        private static void PutPartnerInVehicle()
        {
            Vehicle vehicle = PlayerPed.CurrentVehicle;
            if (vehicle == null || !vehicle.Exists()) { Game.DisplayNotification("Сначала сядьте в автомобиль."); return; }
            int seat = -2;
            foreach (Ped ped in Partners)
            {
                if (ped == null || !ped.Exists()) continue;
                ped.Tasks.EnterVehicle(vehicle, seat);
                seat--;
            }
            Game.DisplayNotification("Напарникам приказано сесть в машину.");
        }

        private static void RemovePartners()
        {
            foreach (Ped ped in Partners) if (ped != null && ped.Exists()) ped.Delete();
            Partners.Clear();
            Game.DisplayNotification("Напарники удалены.");
        }

        private static void SpawnPoliceVehicle(string modelName)
        {
            Model model = LoadModel(modelName);
            Vehicle vehicle = new Vehicle(model, PlayerPed.GetOffsetPosition(new Vector3(0f, 8f, 0f)), PlayerPed.Heading);
            vehicle.IsPersistent = true;
            SpawnedVehicles.Add(vehicle);
            model.Dismiss();
            Game.DisplayNotification("Создан полицейский автомобиль: " + modelName);
        }

        private static void RequestBackup()
        {
            if (BackupVehicles.Count > 0) { Game.DisplayNotification("Подкрепление уже вызвано."); return; }
            string[] cars = { "POLICE2", "POLICE3" };
            for (int i = 0; i < cars.Length; i++)
            {
                Model vehicleModel = LoadModel(cars[i]);
                Vector3 pos = PlayerPed.GetOffsetPosition(new Vector3((i == 0 ? -1 : 1) * 9f, 18f, 0f));
                Vehicle vehicle = new Vehicle(vehicleModel, pos, PlayerPed.Heading);
                vehicle.IsPersistent = true;
                BackupVehicles.Add(vehicle);
                vehicleModel.Dismiss();

                Model copModel = LoadModel("s_m_y_cop_01");
                Ped driver = new Ped(copModel, pos, PlayerPed.Heading);
                driver.IsPersistent = true;
                driver.Tasks.EnterVehicle(vehicle, -1);
                BackupPeds.Add(driver);
                copModel.Dismiss();
            }
            Game.DisplayNotification("Подкрепление создано рядом с вами. Это локальная имитация, не вызов LSPDFR/Ultimate Backup.");
        }

        private static void CancelBackup()
        {
            foreach (Ped ped in BackupPeds) if (ped != null && ped.Exists()) ped.Delete();
            foreach (Vehicle vehicle in BackupVehicles) if (vehicle != null && vehicle.Exists()) vehicle.Delete();
            BackupPeds.Clear();
            BackupVehicles.Clear();
            Game.DisplayNotification("Подкрепление удалено.");
        }

        private static void SpeakCustomText()
        {
            using (TextEntryForm form = new TextEntryForm())
            {
                if (form.ShowDialog() != DialogResult.OK || String.IsNullOrWhiteSpace(form.Value)) return;
                string value = form.Value.Trim();
                Game.DisplaySubtitle(value, Math.Min(10000, Math.Max(2500, value.Length * 70)));
                try
                {
                    Speaker.SetOutputToDefaultAudioDevice();
                    Speaker.SpeakAsyncCancelAll();
                    Speaker.SpeakAsync(value);
                }
                catch (Exception ex) { Game.DisplayNotification("Системная озвучка недоступна: " + ex.Message); }
            }
        }

        private static void Cleanup()
        {
            try { Speaker.SpeakAsyncCancelAll(); } catch { }
            RemovePartners();
            CancelBackup();
            foreach (Vehicle vehicle in SpawnedVehicles) if (vehicle != null && vehicle.Exists()) vehicle.Delete();
            SpawnedVehicles.Clear();
        }

        private sealed class TextEntryForm : Form
        {
            private readonly TextBox input;
            public string Value { get { return input.Text; } }

            public TextEntryForm()
            {
                Text = "Proverka Police Mod — озвучка";
                Width = 520; Height = 165; StartPosition = FormStartPosition.CenterScreen;
                FormBorderStyle = FormBorderStyle.FixedDialog;
                MaximizeBox = false; MinimizeBox = false;
                Label label = new Label { Left = 12, Top = 12, Width = 480, Text = "Введите текст на русском или английском:" };
                input = new TextBox { Left = 12, Top = 38, Width = 480 };
                Button ok = new Button { Text = "Озвучить", Left = 320, Top = 75, Width = 80, DialogResult = DialogResult.OK };
                Button cancel = new Button { Text = "Отмена", Left = 412, Top = 75, Width = 80, DialogResult = DialogResult.Cancel };
                Controls.Add(label); Controls.Add(input); Controls.Add(ok); Controls.Add(cancel);
                AcceptButton = ok; CancelButton = cancel;
            }
        }
    }
}
