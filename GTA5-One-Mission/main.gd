extends Node3D

const WALK_SPEED := 7.0
const CAR_SPEED := 19.0
const GRAVITY := 22.0

var player: CharacterBody3D
var car: CharacterBody3D
var camera: Camera3D
var hud: Label
var objective: Label
var marker: MeshInstance3D
var in_car := false
var mission_stage := 0
var velocity_y := 0.0
var yaw := 0.0
var pitch := -0.18
var message_time := 0.0

func _ready() -> void:
    Input.set_mouse_mode(Input.MOUSE_MODE_CAPTURED)
    _build_city()
    _build_player()
    _build_car()
    _build_ui()
    _set_objective("МИССИЯ 1: ДОБЕРИСЬ ДО ВСТРЕЧИ", "Иди к светящемуся маркеру на площади.")

func _unhandled_input(event: InputEvent) -> void:
    if event is InputEventMouseMotion and Input.mouse_mode == Input.MOUSE_MODE_CAPTURED:
        yaw -= event.relative.x * 0.0025
        pitch = clampf(pitch - event.relative.y * 0.002, -0.85, 0.35)
    if event is InputEventKey and event.pressed and not event.echo:
        if event.keycode == KEY_ESCAPE:
            Input.set_mouse_mode(Input.MOUSE_MODE_VISIBLE if Input.mouse_mode == Input.MOUSE_MODE_CAPTURED else Input.MOUSE_MODE_CAPTURED)
        if event.keycode == KEY_E:
            _toggle_car()

func _physics_process(delta: float) -> void:
    if message_time > 0.0:
        message_time -= delta
        if message_time <= 0.0:
            hud.text = "WASD — движение   E — сесть/выйти   Мышь — обзор   ESC — курсор"
    if in_car:
        _drive_car(delta)
    else:
        _walk_player(delta)
    _update_camera(delta)
    _check_mission()

func _build_city() -> void:
    var world := WorldEnvironment.new()
    var env := Environment.new()
    env.background_mode = Environment.BG_COLOR
    env.background_color = Color(0.035, 0.05, 0.075)
    env.ambient_light_source = Environment.AMBIENT_SOURCE_COLOR
    env.ambient_light_color = Color(0.65, 0.72, 0.85)
    env.ambient_light_energy = 0.7
    world.environment = env
    add_child(world)
    _box("Ground", Vector3(0, -0.6, 0), Vector3(100, 1, 100), Color(0.16, 0.19, 0.2))
    _box("Main road", Vector3(0, -0.08, 0), Vector3(12, 0.12, 100), Color(0.055, 0.06, 0.07))
    _box("Cross street", Vector3(0, -0.07, 0), Vector3(100, 0.13, 10), Color(0.055, 0.06, 0.07))
    for z in range(-42, 44, 8):
        _box("Road dash", Vector3(0, 0.01, z), Vector3(0.22, 0.025, 3.2), Color(0.92, 0.82, 0.48))
    for i in range(14):
        var x := -38.0 if i % 2 == 0 else 38.0
        var z := -38.0 + float(i) * 6.0
        var h := 7.0 + float((i * 7) % 5) * 2.3
        var color := Color(0.19 + float(i % 3) * 0.035, 0.22, 0.28 + float(i % 4) * 0.025)
        _box("City block", Vector3(x, h * 0.5 - 0.1, z), Vector3(11, h, 8), color)
        for floor_i in range(int(h / 2.2)):
            _box("Window", Vector3(x + (5.53 if x < 0 else -5.53), 1.0 + floor_i * 2.2, z), Vector3(0.05, 0.85, 1.2), Color(0.65, 0.78, 0.86))
    _box("Meetup plaza", Vector3(16, -0.25, -10), Vector3(22, 0.35, 20), Color(0.24, 0.27, 0.27))
    _box("Destination lot", Vector3(-20, -0.25, 28), Vector3(24, 0.35, 20), Color(0.2, 0.23, 0.24))
    _make_marker(Vector3(16, 0.25, -10))

func _build_player() -> void:
    player = CharacterBody3D.new()
    player.name = "Player"
    player.position = Vector3(0, 1, 0)
    var shape := CapsuleShape3D.new()
    shape.radius = 0.42
    shape.height = 1.8
    var collision := CollisionShape3D.new()
    collision.shape = shape
    player.add_child(collision)
    var mesh := MeshInstance3D.new()
    var capsule := CapsuleMesh.new()
    capsule.radius = 0.42
    capsule.height = 1.8
    mesh.mesh = capsule
    var mat := StandardMaterial3D.new()
    mat.albedo_color = Color(0.72, 0.19, 0.12)
    mesh.material_override = mat
    player.add_child(mesh)
    add_child(player)
    camera = Camera3D.new()
    camera.current = true
    add_child(camera)

func _build_car() -> void:
    car = CharacterBody3D.new()
    car.name = "Car"
    car.position = Vector3(3.0, 0.6, 2.0)
    var body := MeshInstance3D.new()
    var box := BoxMesh.new()
    box.size = Vector3(2.0, 0.9, 3.8)
    body.mesh = box
    var mat := StandardMaterial3D.new()
    mat.albedo_color = Color(0.12, 0.22, 0.34)
    body.material_override = mat
    car.add_child(body)
    var roof := MeshInstance3D.new()
    var roof_mesh := BoxMesh.new()
    roof_mesh.size = Vector3(1.55, 0.65, 1.8)
    roof.mesh = roof_mesh
    roof.position.y = 0.72
    roof.material_override = mat
    car.add_child(roof)
    var collision := CollisionShape3D.new()
    var shape := BoxShape3D.new()
    shape.size = Vector3(2, 1.4, 3.8)
    collision.shape = shape
    car.add_child(collision)
    add_child(car)

func _build_ui() -> void:
    var layer := CanvasLayer.new()
    add_child(layer)
    hud = Label.new()
    hud.position = Vector2(24, 20)
    hud.add_theme_font_size_override("font_size", 17)
    hud.add_theme_color_override("font_color", Color(1, 1, 1))
    layer.add_child(hud)
    objective = Label.new()
    objective.position = Vector2(24, 58)
    objective.add_theme_font_size_override("font_size", 23)
    objective.add_theme_color_override("font_color", Color(1.0, 0.78, 0.28))
    layer.add_child(objective)
    var title := Label.new()
    title.text = "CITY STORIES  /  CHAPTER 01"
    title.position = Vector2(24, 670)
    title.add_theme_font_size_override("font_size", 14)
    title.add_theme_color_override("font_color", Color(0.7, 0.76, 0.82))
    layer.add_child(title)
    hud.text = "WASD — движение   E — сесть/выйти   Мышь — обзор   ESC — курсор"

func _walk_player(delta: float) -> void:
    var input := Vector2.ZERO
    input.x = float(Input.is_key_pressed(KEY_D) or Input.is_key_pressed(KEY_RIGHT)) - float(Input.is_key_pressed(KEY_A) or Input.is_key_pressed(KEY_LEFT))
    input.y = float(Input.is_key_pressed(KEY_S) or Input.is_key_pressed(KEY_DOWN)) - float(Input.is_key_pressed(KEY_W) or Input.is_key_pressed(KEY_UP))
    var forward := Vector3(sin(yaw), 0, cos(yaw))
    var right := Vector3(forward.z, 0, -forward.x)
    var direction := (right * input.x + forward * input.y).normalized()
    player.velocity.x = direction.x * WALK_SPEED
    player.velocity.z = direction.z * WALK_SPEED
    if not player.is_on_floor():
        player.velocity.y -= GRAVITY * delta
    else:
        player.velocity.y = 0
    player.move_and_slide()
    if direction.length() > 0.1:
        player.rotation.y = atan2(direction.x, direction.z)

func _drive_car(_delta: float) -> void:
    var throttle := float(Input.is_key_pressed(KEY_W) or Input.is_key_pressed(KEY_UP)) - float(Input.is_key_pressed(KEY_S) or Input.is_key_pressed(KEY_DOWN))
    var steering := float(Input.is_key_pressed(KEY_D) or Input.is_key_pressed(KEY_RIGHT)) - float(Input.is_key_pressed(KEY_A) or Input.is_key_pressed(KEY_LEFT))
    car.rotation.y -= steering * 1.65 * get_physics_process_delta_time() * maxf(absf(throttle), 0.25)
    var forward := Vector3(sin(car.rotation.y), 0, cos(car.rotation.y))
    car.velocity = forward * throttle * CAR_SPEED
    car.move_and_slide()
    player.global_position = car.global_position + Vector3(0, 1.2, 0)

func _toggle_car() -> void:
    if not in_car and player.global_position.distance_to(car.global_position) < 4.0:
        in_car = true
        hud.text = "Ты за рулём. Езжай к следующему маркеру!"
    elif in_car:
        in_car = false
        player.global_position = car.global_position + Vector3(2.5, 1, 0)
        hud.text = "Ты вышел из машины."

func _update_camera(delta: float) -> void:
    var target := car.global_position if in_car else player.global_position
    var distance := 8.0 if in_car else 6.0
    var offset := Vector3(sin(yaw) * distance, 3.0 - pitch * 2.0, cos(yaw) * distance)
    camera.global_position = camera.global_position.lerp(target + offset, minf(delta * 7.0, 1.0))
    camera.look_at(target + Vector3(0, 1.0, 0), Vector3.UP)

func _check_mission() -> void:
    var actor := car if in_car else player
    if mission_stage == 0 and actor.global_position.distance_to(Vector3(16, 0, -10)) < 5.5:
        mission_stage = 1
        _set_objective("МИССИЯ 1: ЗАБЕРИ МАШИНУ", "Подойди к синей машине и нажми E.")
        _make_marker(Vector3(3, 0.25, 2))
    elif mission_stage == 1 and player.global_position.distance_to(car.global_position) < 4.0 and not in_car:
        _set_objective("МИССИЯ 1: ДОСТАВЬ МАШИНУ", "Садись в машину и езжай на парковку на юге.")
        mission_stage = 2
        _make_marker(Vector3(-20, 0.25, 28))
    elif mission_stage == 2 and in_car and car.global_position.distance_to(Vector3(-20, 0, 28)) < 7.0:
        mission_stage = 3
        _set_objective("МИССИЯ ПРОЙДЕНА!", "Глава 1 завершена. Спасибо за игру!")
        hud.text = "MISSION COMPLETE"
        message_time = 99999.0

func _set_objective(title: String, hint: String) -> void:
    objective.text = title + "\n" + hint

func _make_marker(pos: Vector3) -> void:
    if is_instance_valid(marker):
        marker.queue_free()
    marker = MeshInstance3D.new()
    var mesh := CylinderMesh.new()
    mesh.top_radius = 1.7
    mesh.bottom_radius = 1.7
    mesh.height = 0.12
    marker.mesh = mesh
    var mat := StandardMaterial3D.new()
    mat.albedo_color = Color(1.0, 0.68, 0.12, 0.75)
    mat.emission_enabled = true
    mat.emission = Color(1.0, 0.4, 0.05)
    marker.material_override = mat
    marker.position = pos
    add_child(marker)

func _box(label: String, pos: Vector3, size: Vector3, color: Color) -> void:
    var body := StaticBody3D.new()
    body.name = label
    body.position = pos
    var mesh := MeshInstance3D.new()
    var box := BoxMesh.new()
    box.size = size
    mesh.mesh = box
    var mat := StandardMaterial3D.new()
    mat.albedo_color = color
    mesh.material_override = mat
    body.add_child(mesh)
    var collision := CollisionShape3D.new()
    var shape := BoxShape3D.new()
    shape.size = size
    collision.shape = shape
    body.add_child(collision)
    add_child(body)
