extends Control

const GAME_RELATIVE_PATH := "../Game/CityRun.exe"
var status_label: Label
var play_button: Button

func _ready() -> void:
	_build_ui()
	_check_game()

func _style_panel(panel: PanelContainer, color: Color, border: Color) -> void:
	var style := StyleBoxFlat.new()
	style.bg_color = color
	style.border_color = border
	style.set_border_width_all(1)
	style.set_corner_radius_all(16)
	style.content_margin_left = 24
	style.content_margin_right = 24
	style.content_margin_top = 20
	style.content_margin_bottom = 20
	panel.add_theme_stylebox_override("panel", style)

func _build_ui() -> void:
	var bg := ColorRect.new()
	bg.color = Color("#090d16")
	bg.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	add_child(bg)
	var glow := ColorRect.new()
	glow.color = Color(0.55, 0.035, 0.06, 0.18)
	glow.position = Vector2(700, -150)
	glow.size = Vector2(550, 550)
	bg.add_child(glow)

	var root := HBoxContainer.new()
	root.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	root.add_theme_constant_override("separation", 24)
	root.add_theme_constant_override("margin_left", 38)
	root.add_theme_constant_override("margin_right", 38)
	root.add_theme_constant_override("margin_top", 30)
	root.add_theme_constant_override("margin_bottom", 30)
	add_child(root)

	var left := VBoxContainer.new()
	left.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	left.add_theme_constant_override("separation", 18)
	root.add_child(left)

	var brand := Label.new()
	brand.text = "CITY RUN"
	brand.add_theme_font_size_override("font_size", 42)
	brand.add_theme_color_override("font_color", Color("#f4f6fb"))
	left.add_child(brand)
	var edition := Label.new()
	edition.text = "PC DEMO  /  BUILD 0.1"
	edition.add_theme_font_size_override("font_size", 13)
	edition.add_theme_color_override("font_color", Color("#e34a55"))
	left.add_child(edition)

	var hero := PanelContainer.new()
	hero.size_flags_vertical = Control.SIZE_EXPAND_FILL
	_style_panel(hero, Color("#121a29"), Color("#29374b"))
	left.add_child(hero)
	var hero_col := VBoxContainer.new()
	hero_col.add_theme_constant_override("separation", 14)
	hero.add_child(hero_col)
	var tag := Label.new()
	tag.text = "OPEN-WORLD CITY DEMO"
	tag.add_theme_color_override("font_color", Color("#ff626b"))
	tag.add_theme_font_size_override("font_size", 12)
	hero_col.add_child(tag)
	var title := Label.new()
	title.text = "ТВОЙ ГОРОД.\nТВОИ ПРАВИЛА."
	title.add_theme_font_size_override("font_size", 34)
	title.add_theme_color_override("font_color", Color("#ffffff"))
	hero_col.add_child(title)
	var desc := Label.new()
	desc.text = "Исследуй улицы, садись за руль и выполни первую доставку. Это ранняя демо-версия City Run."
	desc.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	desc.add_theme_font_size_override("font_size", 16)
	desc.add_theme_color_override("font_color", Color("#b4c0d2"))
	hero_col.add_child(desc)
	var spacer := Control.new()
	spacer.size_flags_vertical = Control.SIZE_EXPAND_FILL
	hero_col.add_child(spacer)
	status_label = Label.new()
	status_label.text = "ПРОВЕРКА ФАЙЛОВ ИГРЫ…"
	status_label.add_theme_font_size_override("font_size", 13)
	status_label.add_theme_color_override("font_color", Color("#aab7c9"))
	hero_col.add_child(status_label)
	play_button = _button("▶   ИГРАТЬ", true)
	play_button.custom_minimum_size = Vector2(0, 58)
	play_button.pressed.connect(_launch_game)
	hero_col.add_child(play_button)

	var right := VBoxContainer.new()
	right.custom_minimum_size.x = 300
	right.add_theme_constant_override("separation", 16)
	root.add_child(right)
	var info := PanelContainer.new()
	_style_panel(info, Color("#101725"), Color("#263449"))
	right.add_child(info)
	var info_col := VBoxContainer.new()
	info_col.add_theme_constant_override("separation", 12)
	info.add_child(info_col)
	_add_heading(info_col, "СОСТАВ ДЕМО")
	_add_line(info_col, "01", "3D-город и улицы")
	_add_line(info_col, "02", "Управляемый автомобиль")
	_add_line(info_col, "03", "Дорожный трафик")
	_add_line(info_col, "04", "Миссия с посылкой")
	_add_line(info_col, "05", "Свободное исследование")

	var controls := PanelContainer.new()
	_style_panel(controls, Color("#101725"), Color("#263449"))
	right.add_child(controls)
	var controls_col := VBoxContainer.new()
	controls_col.add_theme_constant_override("separation", 10)
	controls.add_child(controls_col)
	_add_heading(controls_col, "УПРАВЛЕНИЕ")
	_add_control(controls_col, "W A S D", "Движение")
	_add_control(controls_col, "МЫШЬ", "Обзор / управление")
	_add_control(controls_col, "E", "Войти / выйти / взять")
	_add_control(controls_col, "ESC", "Освободить курсор")
	var note := Label.new()
	note.text = "Ранняя демо-сборка. Состав и графика будут расширяться."
	note.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	note.add_theme_font_size_override("font_size", 12)
	note.add_theme_color_override("font_color", Color("#77869b"))
	right.add_child(note)

func _button(caption: String, primary: bool) -> Button:
	var b := Button.new()
	b.text = caption
	b.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	b.add_theme_font_size_override("font_size", 17)
	var normal := StyleBoxFlat.new()
	normal.bg_color = Color("#c62838") if primary else Color("#1a2638")
	normal.set_corner_radius_all(10)
	normal.content_margin_top = 14
	normal.content_margin_bottom = 14
	b.add_theme_stylebox_override("normal", normal)
	var hover := normal.duplicate()
	hover.bg_color = Color("#ed3c4e") if primary else Color("#263850")
	b.add_theme_stylebox_override("hover", hover)
	return b

func _add_heading(parent: VBoxContainer, text_value: String) -> void:
	var label := Label.new()
	label.text = text_value
	label.add_theme_font_size_override("font_size", 12)
	label.add_theme_color_override("font_color", Color("#f05a65"))
	parent.add_child(label)

func _add_line(parent: VBoxContainer, number: String, text_value: String) -> void:
	var row := HBoxContainer.new()
	var n := Label.new()
	n.text = number
	n.custom_minimum_size.x = 28
	n.add_theme_color_override("font_color", Color("#ed5965"))
	row.add_child(n)
	var t := Label.new()
	t.text = text_value
	t.add_theme_color_override("font_color", Color("#d2d9e4"))
	row.add_child(t)
	parent.add_child(row)

func _add_control(parent: VBoxContainer, key: String, action: String) -> void:
	var row := HBoxContainer.new()
	var k := Label.new()
	k.text = key
	k.custom_minimum_size.x = 92
	k.add_theme_color_override("font_color", Color("#ffffff"))
	row.add_child(k)
	var a := Label.new()
	a.text = action
	a.add_theme_color_override("font_color", Color("#9eacc0"))
	row.add_child(a)
	parent.add_child(row)

func _game_path() -> String:
	return ProjectSettings.globalize_path("res://") + GAME_RELATIVE_PATH

func _check_game() -> void:
	var path := _game_path()
	var exists := FileAccess.file_exists(path)
	play_button.disabled = not exists
	status_label.text = "ИГРА НАЙДЕНА · ГОТОВА К ЗАПУСКУ" if exists else "ФАЙЛ ИГРЫ НЕ НАЙДЕН · ПРОВЕРЬТЕ ПАПКУ Game"
	status_label.add_theme_color_override("font_color", Color("#62d99b") if exists else Color("#ffb45b"))

func _launch_game() -> void:
	var path := _game_path()
	if not FileAccess.file_exists(path):
		_check_game()
		return
	var result := OS.create_process(path, [])
	if result == -1:
		status_label.text = "НЕ УДАЛОСЬ ЗАПУСТИТЬ ИГРУ"
	else:
		status_label.text = "CITY RUN ЗАПУЩЕНА"

