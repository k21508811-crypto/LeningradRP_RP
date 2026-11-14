extends Node3D

var player: CharacterBody3D
var camera_pivot: Node3D
var camera: Camera3D
var car: CharacterBody3D
var in_car := false
var mission_stage := 0
var checkpoint := Vector3(0, 0.5, -42)
var hud: Label
var prompt: Label
var speed := 0.0
var parcel: Node3D
var traffic: Array[Node3D] = []
var traffic_speeds: Array[float] = []
var traffic_lanes: Array[float] = [-4.2, 4.2]

func _ready() -> void:
	_build_city()
	_build_player()
	_build_car()
	_build_parcel()
	_spawn_traffic()
	_build_ui()
	_add_checkpoint(checkpoint)
	Input.mouse_mode = Input.MOUSE_MODE_CAPTURED

func _box(label: String, pos: Vector3, size: Vector3, color: Color, solid := true) -> void:
	var body := StaticBody3D.new() if solid else Node3D.new()
	body.name = label
	body.position = pos
	var mesh_node := MeshInstance3D.new()
	var mesh := BoxMesh.new()
	mesh.size = size
	mesh_node.mesh = mesh
	var mat := StandardMaterial3D.new()
	mat.albedo_color = color
	mesh_node.material_override = mat
	body.add_child(mesh_node)
	if solid:
		var collision := CollisionShape3D.new()
		var shape := BoxShape3D.new()
		shape.size = size
		collision.shape = shape
		body.add_child(collision)
	add_child(body)

func _build_city() -> void:
	var sun := DirectionalLight3D.new()
	sun.rotation_degrees = Vector3(-48, -25, 0)
	sun.light_energy = 1.35
	sun.shadow_enabled = true
	sun.directional_shadow_max_distance = 120.0
	add_child(sun)
	var world := WorldEnvironment.new()
	var env := Environment.new()
	env.background_mode = Environment.BG_SKY
	env.sky = Sky.new()
	env.ambient_light_source = Environment.AMBIENT_SOURCE_COLOR
	env.ambient_light_color = Color(0.55, 0.62, 0.72)
	env.ambient_light_energy = 0.8
	env.tonemap_mode = Environment.TONE_MAPPER_FILMIC
	env.tonemap_exposure = 1.05
	env.glow_enabled = true
	env.glow_intensity = 0.35
	env.fog_enabled = true
	env.fog_light_color = Color(0.48, 0.56, 0.66)
	env.fog_density = 0.0018
	world.environment = env
	add_child(world)
	_box("Ground", Vector3(0,-0.6,-65), Vector3(100,1,210), Color(0.12,0.16,0.14))
	_box("Road", Vector3(0,-0.05,-65), Vector3(15,0.15,210), Color(0.09,0.095,0.11), false)
	for side in [-1,1]:
		_box("Sidewalk", Vector3(side*9,0.02,-65), Vector3(3,0.3,210), Color(0.34,0.34,0.32))
		for i in range(9):
			var z := -i * 22.0 - 5.0
			var w := 8.0 + float((i*7)%5)
			var h := 9.0 + float((i*11)%22)
			_box("Building", Vector3(side*(17+w*0.5),h*0.5-0.1,z), Vector3(w,h,13), Color(0.24+float(i%3)*0.04,0.27,0.31))
			_add_building_windows(side, side * (17.0 + w * 0.5), z, w, h, i)
	for z in range(-10,-190,-10):
		_box("Lane marking", Vector3(0,0.04,z), Vector3(0.18,0.025,4), Color(0.85,0.78,0.48), false)
	_box("Garage plaza", Vector3(0,0.02,-166), Vector3(34,0.2,20), Color(0.23,0.27,0.25))
	for z in range(-18, -175, -24):
		_add_streetlight(-10.8, z)
		_add_streetlight(10.8, z)
	for i in range(12):
		var tree_z := -12.0 - float(i) * 13.0
		_add_tree(-12.5, tree_z)
		_add_tree(12.5, tree_z - 5.0)


func _add_building_windows(side: int, x: float, z: float, width: float, height: float, seed: int) -> void:
	var columns := maxi(2, int(width / 2.3))
	var rows := maxi(2, int(height / 2.4))
	for row in range(rows):
		for col in range(columns):
			var wx := float(col) * (width / float(columns)) - width * 0.5 + width / float(columns) * 0.5
			var wy := 1.8 + float(row) * 2.2
			if wy > height - 1.0:
				continue
			var tint := Color(0.16, 0.24, 0.31)
			if (row * 7 + col * 3 + seed) % 5 == 0:
				tint = Color(0.95, 0.66, 0.28)
			var pane := MeshInstance3D.new()
			var pane_mesh := BoxMesh.new()
			pane_mesh.size = Vector3(0.72, 0.95, 0.07)
			pane.mesh = pane_mesh
			var pane_mat := StandardMaterial3D.new()
			pane_mat.albedo_color = tint
			if tint.r > 0.8:
				pane_mat.emission_enabled = true
				pane_mat.emission = Color(0.55, 0.25, 0.07)
			pane.material_override = pane_mat
			pane.position = Vector3(x + wx, wy, z + (6.56 * float(side)))
			add_child(pane)

func _add_streetlight(x: float, z: float) -> void:
	_box("Lamp post", Vector3(x, 3.0, z), Vector3(0.12, 6.0, 0.12), Color(0.12, 0.13, 0.15))
	var head := MeshInstance3D.new()
	var mesh := SphereMesh.new()
	mesh.radius = 0.28
	mesh.height = 0.5
	head.mesh = mesh
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(1.0, 0.78, 0.48)
	mat.emission_enabled = true
	mat.emission = Color(1.0, 0.52, 0.2)
	head.material_override = mat
	head.position = Vector3(x, 6.1, z)
	add_child(head)
	var light := OmniLight3D.new()
	light.light_color = Color(1.0, 0.68, 0.36)
	light.light_energy = 0.45
	light.omni_range = 9.0
	light.shadow_enabled = false
	light.position = Vector3(x, 5.8, z)
	add_child(light)

func _add_tree(x: float, z: float) -> void:
	_box("Tree trunk", Vector3(x, 1.0, z), Vector3(0.45, 2.0, 0.45), Color(0.22, 0.13, 0.08))
	var crown := MeshInstance3D.new()
	var mesh := SphereMesh.new()
	mesh.radius = 1.65
	mesh.height = 3.0
	crown.mesh = mesh
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(0.10, 0.28, 0.16)
	crown.material_override = mat
	crown.position = Vector3(x, 3.0, z)
	add_child(crown)

func _build_player() -> void:
	player = CharacterBody3D.new()
	var visual := MeshInstance3D.new()
	var capsule := CapsuleMesh.new()
	capsule.radius = 0.42
	capsule.height = 1.8
	visual.mesh = capsule
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(0.72,0.24,0.12)
	visual.material_override = mat
	visual.position.y = 0.9
	player.add_child(visual)
	var collision := CollisionShape3D.new()
	var shape := CapsuleShape3D.new()
	shape.radius = 0.42
	shape.height = 1.8
	collision.shape = shape
	collision.position.y = 0.9
	player.add_child(collision)
	camera_pivot = Node3D.new()
	camera_pivot.position.y = 1.5
	player.add_child(camera_pivot)
	camera = Camera3D.new()
	camera.position = Vector3(0,1.2,6.2)
	camera.fov = 76
	camera.rotation_degrees.x = -10
	camera.current = true
	camera_pivot.add_child(camera)
	player.position = Vector3(2,0,4)
	add_child(player)

func _build_car() -> void:
	car = CharacterBody3D.new()
	var chassis := MeshInstance3D.new()
	var mesh := BoxMesh.new()
	mesh.size = Vector3(2,0.75,4)
	chassis.mesh = mesh
	var paint := StandardMaterial3D.new()
	paint.albedo_color = Color(0.68,0.08,0.06)
	paint.roughness = 0.24
	paint.clearcoat_enabled = true
	paint.clearcoat = 0.7
	paint.metallic = 0.35
	chassis.material_override = paint
	chassis.position.y = 0.7
	car.add_child(chassis)
	var wheel_mat := StandardMaterial3D.new()
	wheel_mat.albedo_color = Color(0.025, 0.03, 0.035)
	wheel_mat.roughness = 0.92
	for wheel_x in [-0.92, 0.92]:
		for wheel_z in [-1.25, 1.25]:
			var wheel := MeshInstance3D.new()
			var wheel_mesh := CylinderMesh.new()
			wheel_mesh.top_radius = 0.38
			wheel_mesh.bottom_radius = 0.38
			wheel_mesh.height = 0.24
			wheel.mesh = wheel_mesh
			wheel.material_override = wheel_mat
			wheel.rotation_degrees.z = 90
			wheel.position = Vector3(wheel_x, 0.4, wheel_z)
			car.add_child(wheel)
	var lamp_mat := StandardMaterial3D.new()
	lamp_mat.albedo_color = Color(1.0, 0.9, 0.62)
	lamp_mat.emission_enabled = true
	lamp_mat.emission = Color(1.0, 0.72, 0.35)
	lamp_mat.emission_energy_multiplier = 1.2
	for lamp_x in [-0.62, 0.62]:
		var headlamp := MeshInstance3D.new()
		var lamp_mesh := BoxMesh.new()
		lamp_mesh.size = Vector3(0.42, 0.2, 0.08)
		headlamp.mesh = lamp_mesh
		headlamp.material_override = lamp_mat
		headlamp.position = Vector3(lamp_x, 0.82, -2.02)
		car.add_child(headlamp)
	var tail_mat := StandardMaterial3D.new()
	tail_mat.albedo_color = Color(0.85, 0.025, 0.015)
	tail_mat.emission_enabled = true
	tail_mat.emission = Color(0.65, 0.01, 0.005)
	for lamp_x in [-0.62, 0.62]:
		var taillamp := MeshInstance3D.new()
		var tail_mesh := BoxMesh.new()
		tail_mesh.size = Vector3(0.42, 0.2, 0.08)
		taillamp.mesh = tail_mesh
		taillamp.material_override = tail_mat
		taillamp.position = Vector3(lamp_x, 0.82, 2.02)
		car.add_child(taillamp)
	var roof := MeshInstance3D.new()
	var roof_mesh := BoxMesh.new()
	roof_mesh.size = Vector3(1.55,0.65,1.9)
	roof.mesh = roof_mesh
	var glass := StandardMaterial3D.new()
	glass.albedo_color = Color(0.08,0.14,0.19)
	roof.material_override = glass
	roof.position = Vector3(0,1.35,-0.15)
	car.add_child(roof)
	var collision := CollisionShape3D.new()
	var shape := BoxShape3D.new()
	shape.size = Vector3(2,1.4,4)
	collision.shape = shape
	collision.position.y = 0.7
	car.add_child(collision)
	car.position = Vector3(0,0,1)
	add_child(car)

func _build_parcel() -> void:
	parcel = Node3D.new()
	parcel.position = Vector3(0, 0, -95)
	var crate := MeshInstance3D.new()
	var crate_mesh := BoxMesh.new()
	crate_mesh.size = Vector3(0.85, 0.65, 0.85)
	crate.mesh = crate_mesh
	var crate_mat := StandardMaterial3D.new()
	crate_mat.albedo_color = Color(0.62, 0.36, 0.14)
	crate_mat.roughness = 0.88
	crate.material_override = crate_mat
	crate.position.y = 0.42
	parcel.add_child(crate)
	var glow := OmniLight3D.new()
	glow.light_color = Color(1.0, 0.62, 0.2)
	glow.light_energy = 0.8
	glow.omni_range = 3.5
	glow.position.y = 1.2
	parcel.add_child(glow)
	add_child(parcel)

func _spawn_traffic() -> void:
	var paints := [Color(0.12,0.32,0.68), Color(0.72,0.72,0.68), Color(0.12,0.42,0.28), Color(0.72,0.38,0.08)]
	for i in range(8):
		var vehicle := Node3D.new()
		var body := MeshInstance3D.new()
		var body_mesh := BoxMesh.new()
		body_mesh.size = Vector3(1.8, 0.65, 3.5)
		body.mesh = body_mesh
		var paint := StandardMaterial3D.new()
		paint.albedo_color = paints[i % paints.size()]
		paint.roughness = 0.32
		body.material_override = paint
		body.position.y = 0.62
		vehicle.add_child(body)
		var cabin := MeshInstance3D.new()
		var cabin_mesh := BoxMesh.new()
		cabin_mesh.size = Vector3(1.45, 0.55, 1.55)
		cabin.mesh = cabin_mesh
		var glass := StandardMaterial3D.new()
		glass.albedo_color = Color(0.06,0.12,0.17)
		cabin.material_override = glass
		cabin.position = Vector3(0, 1.17, -0.15)
		vehicle.add_child(cabin)
		vehicle.position = Vector3(traffic_lanes[i % 2], 0, 10.0 - float(i) * 24.0)
		add_child(vehicle)
		traffic.append(vehicle)
		traffic_speeds.append(7.0 + float(i % 4) * 1.8)

func _build_ui() -> void:
	var layer := CanvasLayer.new()
	add_child(layer)
	hud = Label.new()
	hud.position = Vector2(24,22)
	hud.add_theme_font_size_override("font_size",22)
	layer.add_child(hud)
	prompt = Label.new()
	prompt.anchor_left = 0.5
	prompt.anchor_top = 1.0
	prompt.anchor_right = 0.5
	prompt.anchor_bottom = 1.0
	prompt.position = Vector2(-240,-74)
	prompt.add_theme_font_size_override("font_size",19)
	prompt.add_theme_color_override("font_color",Color(1,0.82,0.32))
	layer.add_child(prompt)
	var help := Label.new()
	help.text = "WASD — движение / вождение    E — сесть или выйти    Esc — курсор"
	help.position = Vector2(24,680)
	help.add_theme_font_size_override("font_size",15)
	layer.add_child(help)

func _add_checkpoint(pos: Vector3) -> void:
	var marker := MeshInstance3D.new()
	var ring := CylinderMesh.new()
	ring.top_radius = 2.0
	ring.bottom_radius = 2.0
	ring.height = 0.12
	marker.mesh = ring
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(1,0.65,0.05)
	mat.emission_enabled = true
	mat.emission = Color(1,0.35,0.02)
	marker.material_override = mat
	marker.position = Vector3(pos.x,0.18,pos.z)
	add_child(marker)

func _input(event: InputEvent) -> void:
	if event is InputEventMouseMotion and Input.mouse_mode == Input.MOUSE_MODE_CAPTURED:
		if in_car:
			car.rotate_y(-event.relative.x*0.0025)
		else:
			player.rotate_y(-event.relative.x*0.0025)
			camera_pivot.rotate_x(-event.relative.y*0.0025)
			camera_pivot.rotation.x = clamp(camera_pivot.rotation.x,-0.65,0.35)
	if event is InputEventKey and event.pressed and event.keycode == KEY_ESCAPE:
		Input.mouse_mode = Input.MOUSE_MODE_VISIBLE
	if event is InputEventMouseButton and event.pressed:
		Input.mouse_mode = Input.MOUSE_MODE_CAPTURED

func _physics_process(delta: float) -> void:
	if Input.is_action_just_pressed("interact"):
		if in_car:
			_exit_car()
		elif mission_stage == 1 and player.global_position.distance_to(parcel.global_position) < 3.0:
			parcel.visible = false
			mission_stage = 2
			checkpoint = Vector3(0, 0.5, -166)
			_add_checkpoint(checkpoint)
		elif player.global_position.distance_to(car.global_position) < 4.0:
			_enter_car()
	if in_car:
		_drive_car(delta)
	else:
		_walk_player(delta)
	_update_mission()

func _process(delta: float) -> void:
	for i in range(traffic.size()):
		var vehicle := traffic[i]
		vehicle.position.z -= traffic_speeds[i] * delta
		if vehicle.position.z < -190.0:
			vehicle.position.z = 12.0 + float(i % 3) * 18.0

func _walk_player(delta: float) -> void:
	var input_dir := Input.get_vector("move_left","move_right","move_forward","move_back")
	var direction := (player.transform.basis * Vector3(input_dir.x,0,input_dir.y)).normalized()
	if direction:
		player.velocity.x = direction.x*6.0
		player.velocity.z = direction.z*6.0
	else:
		player.velocity.x = move_toward(player.velocity.x,0,22*delta)
		player.velocity.z = move_toward(player.velocity.z,0,22*delta)
	player.velocity.y -= 20.0*delta
	player.move_and_slide()

func _enter_car() -> void:
	in_car = true
	player.visible = false
	camera_pivot.top_level = true
	camera_pivot.global_position = car.global_position + Vector3(0,2.2,0)
	camera_pivot.rotation = Vector3(-0.12,car.rotation.y,0)
	camera.current = true

func _exit_car() -> void:
	in_car = false
	player.global_position = car.global_position + car.global_transform.basis.x*2.2
	player.visible = true
	camera_pivot.top_level = false
	camera_pivot.position = Vector3(0, 1.5, 0)
	camera_pivot.rotation = Vector3.ZERO
	player.rotate_y(car.rotation.y)
	camera.current = true

func _drive_car(delta: float) -> void:
	var throttle := Input.get_action_strength("move_forward") - Input.get_action_strength("move_back")
	var steering := Input.get_action_strength("move_right") - Input.get_action_strength("move_left")
	speed = move_toward(speed,throttle*23.0,18.0*delta)
	car.rotate_y(-steering*delta*clamp(abs(speed)/10.0,0.0,1.0)*sign(speed if speed != 0 else 1))
	car.velocity = -car.global_transform.basis.z*speed
	car.velocity.y -= 20.0*delta
	car.move_and_slide()
	camera_pivot.global_position = car.global_position + Vector3(0,2.5,0)
	camera_pivot.rotation = Vector3(-0.12,car.rotation.y,0)

func _update_mission() -> void:
	var current := car.global_position if in_car else player.global_position
	if mission_stage == 0 and current.distance_to(checkpoint) < 7.0:
		mission_stage = 1
		checkpoint = Vector3(0, 0.5, -95)
		_add_checkpoint(checkpoint)
	elif mission_stage == 2 and in_car and current.distance_to(checkpoint) < 8.0:
		mission_stage = 3
		hud.text = "CITY RUN  |  МИССИЯ ВЫПОЛНЕНА!  Награда: $5 000"
		prompt.text = "Посылка доставлена. Отличная работа!"
		return
	var titles := ["ЭТАП 1/3 — Найди машину и доберись до встречи", "ЭТАП 2/3 — Выйди из машины и забери посылку (E)", "ЭТАП 3/3 — Доставь посылку в гараж"]
	hud.text = "CITY RUN  |  " + titles[min(mission_stage, 2)]
	if mission_stage == 1 and not in_car and player.global_position.distance_to(parcel.global_position) < 3.0:
		prompt.text = "Нажми E, чтобы забрать посылку"
	elif not in_car and player.global_position.distance_to(car.global_position) < 4.0:
		prompt.text = "Нажми E, чтобы сесть в машину"
	elif in_car:
		prompt.text = "Нажми E, чтобы выйти из машины"
	else:
		prompt.text = "Следуй к жёлтому маркеру"
