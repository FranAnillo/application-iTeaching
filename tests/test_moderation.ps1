$base = 'http://localhost:8081/api'
$results = @()

# Login
$r = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body '{"username":"admin","password":"Admin1234"}'
$adminToken = $r.token
$r3 = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body '{"username":"estudiante","password":"Estud123!"}'
$estToken = $r3.token
$adminH = @{Authorization="Bearer $adminToken"; 'Content-Type'='application/json'}
$estH = @{Authorization="Bearer $estToken"; 'Content-Type'='application/json'}

$usuarios = Invoke-RestMethod -Uri "$base/usuarios" -Method GET -Headers $adminH
$profId = ($usuarios | Where-Object { $_.username -eq 'profesor' } | Select-Object -First 1).id
$estId = ($usuarios | Where-Object { $_.username -eq 'estudiante' } | Select-Object -First 1).id

# Create separate asignaturas for each moderation test
function New-TestAsig($nombre) {
    $a = Invoke-RestMethod -Uri "$base/asignaturas" -Method POST -Headers $adminH -Body "{`"nombre`":`"$nombre`",`"descripcion`":`"Test`",`"url`":`"$nombre`"}"
    Invoke-RestMethod -Uri "$base/asignaturas/$($a.id)/profesores/$profId" -Method POST -Headers $adminH
    Invoke-RestMethod -Uri "$base/asignaturas/$($a.id)/estudiantes/$estId" -Method POST -Headers $adminH
    return $a.id
}

# Test 1: Insult in comment
$aid1 = New-TestAsig("ModTest1")
try {
    Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $estH -Body "{`"puntuacion`":1,`"comentario`":`"Este profesor es un idiota`",`"profesorId`":$profId,`"asignaturaId`":$aid1}"
    $results += "1. Insult 'idiota': NOT BLOCKED (FAIL)"
} catch {
    $sr = $_.Exception.Response.GetResponseStream(); $rd = New-Object System.IO.StreamReader($sr); $b = $rd.ReadToEnd()
    $results += "1. Insult 'idiota': BLOCKED - $b"
}

# Test 2: Vulgar expression
$aid2 = New-TestAsig("ModTest2")
try {
    Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $estH -Body "{`"puntuacion`":1,`"comentario`":`"Vete a la mierda profesor de mierda`",`"profesorId`":$profId,`"asignaturaId`":$aid2}"
    $results += "2. Vulgar 'mierda': NOT BLOCKED (FAIL)"
} catch {
    $sr = $_.Exception.Response.GetResponseStream(); $rd = New-Object System.IO.StreamReader($sr); $b = $rd.ReadToEnd()
    $results += "2. Vulgar 'mierda': BLOCKED - $b"
}

# Test 3: Insult in puntos de mejora
$aid3 = New-TestAsig("ModTest3")
try {
    Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $estH -Body "{`"puntuacion`":2,`"comentario`":`"Regular`",`"puntosMejora`":`"Es un imbecil total`",`"profesorId`":$profId,`"asignaturaId`":$aid3}"
    $results += "3. PuntosMejora insult: NOT BLOCKED (FAIL)"
} catch {
    $sr = $_.Exception.Response.GetResponseStream(); $rd = New-Object System.IO.StreamReader($sr); $b = $rd.ReadToEnd()
    $results += "3. PuntosMejora insult: BLOCKED - $b"
}

# Test 4: ALL CAPS (shouting)
$aid4 = New-TestAsig("ModTest4")
try {
    Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $estH -Body "{`"puntuacion`":1,`"comentario`":`"ESTE PROFESOR ES LO PEOR QUE HE VISTO EN MI VIDA`",`"profesorId`":$profId,`"asignaturaId`":$aid4}"
    $results += "4. ALL CAPS: NOT BLOCKED (FAIL)"
} catch {
    $sr = $_.Exception.Response.GetResponseStream(); $rd = New-Object System.IO.StreamReader($sr); $b = $rd.ReadToEnd()
    $results += "4. ALL CAPS: BLOCKED - $b"
}

# Test 5: Excessive punctuation
$aid5 = New-TestAsig("ModTest5")
try {
    Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $estH -Body "{`"puntuacion`":1,`"comentario`":`"Muy mal!!!! Horrible!!!`",`"profesorId`":$profId,`"asignaturaId`":$aid5}"
    $results += "5. Excess punctuation: NOT BLOCKED (FAIL)"
} catch {
    $sr = $_.Exception.Response.GetResponseStream(); $rd = New-Object System.IO.StreamReader($sr); $b = $rd.ReadToEnd()
    $results += "5. Excess punctuation: BLOCKED - $b"
}

# Test 6: Constructive comment (should pass)
$aid6 = New-TestAsig("ModTest6")
try {
    $val = Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $estH -Body "{`"puntuacion`":4,`"comentario`":`"Buen profesor, se nota que domina la materia y las clases son interesantes`",`"puntosMejora`":`"Podria utilizar mas recursos visuales y dar mas tiempo en los examenes`",`"profesorId`":$profId,`"asignaturaId`":$aid6}"
    $results += "6. Constructive: ALLOWED (id=$($val.id)) - OK"
} catch {
    $sr = $_.Exception.Response.GetResponseStream(); $rd = New-Object System.IO.StreamReader($sr); $b = $rd.ReadToEnd()
    $results += "6. Constructive: BLOCKED (FAIL) - $b"
}

# Test 7: English insult
$aid7 = New-TestAsig("ModTest7")
try {
    Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $estH -Body "{`"puntuacion`":1,`"comentario`":`"This idiot professor doesn't know shit`",`"profesorId`":$profId,`"asignaturaId`":$aid7}"
    $results += "7. English insult: NOT BLOCKED (FAIL)"
} catch {
    $sr = $_.Exception.Response.GetResponseStream(); $rd = New-Object System.IO.StreamReader($sr); $b = $rd.ReadToEnd()
    $results += "7. English insult: BLOCKED - $b"
}

$results | Out-File 'C:\Users\fjanillo\Documents\application-iTeaching\test_moderation_results.txt' -Encoding utf8
Write-Host "Done"
