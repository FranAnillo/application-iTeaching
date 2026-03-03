$base = 'http://localhost:8081/api'
$results = @()

# Login all 3
$r = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body '{"username":"admin","password":"Admin1234"}'
$adminToken = $r.token
$r2 = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body '{"username":"profesor","password":"Profe123!"}'
$profToken = $r2.token
$r3 = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body '{"username":"estudiante","password":"Estud123!"}'
$estToken = $r3.token
$results += "1. Logins: Admin=$($r.role) Prof=$($r2.role) Est=$($r3.role)"

$adminH = @{Authorization="Bearer $adminToken"; 'Content-Type'='application/json'}
$profH = @{Authorization="Bearer $profToken"; 'Content-Type'='application/json'}
$estH = @{Authorization="Bearer $estToken"; 'Content-Type'='application/json'}

# 2. Admin creates asignatura
$asig = Invoke-RestMethod -Uri "$base/asignaturas" -Method POST -Headers $adminH -Body '{"nombre":"Fisica I","descripcion":"Curso de fisica","url":"fis1"}'
$asigId = $asig.id
$results += "2. Admin create asig: OK (id=$asigId)"

# 3. Get user IDs
$usuarios = Invoke-RestMethod -Uri "$base/usuarios" -Method GET -Headers $adminH
$profPersona = $usuarios | Where-Object { $_.username -eq 'profesor' } | Select-Object -First 1
$estPersona = $usuarios | Where-Object { $_.username -eq 'estudiante' } | Select-Object -First 1
$results += "3. Users: prof=$($profPersona.id) est=$($estPersona.id)"

# 4. Admin assigns profesor and estudiante
Invoke-RestMethod -Uri "$base/asignaturas/$asigId/profesores/$($profPersona.id)" -Method POST -Headers $adminH
Invoke-RestMethod -Uri "$base/asignaturas/$asigId/estudiantes/$($estPersona.id)" -Method POST -Headers $adminH
$results += "4. Prof+Est assigned to asig"

# 5. Estudiante creates valoracion (should succeed)
try {
    $val = Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $estH -Body "{`"puntuacion`":4,`"comentario`":`"Buen profesor, explica bien`",`"puntosMejora`":`"Podria usar mas ejemplos practicos`",`"profesorId`":$($profPersona.id),`"asignaturaId`":$asigId}"
    $results += "5. Est create valoracion: OK (id=$($val.id))"
} catch {
    $sr = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($sr)
    $body = $reader.ReadToEnd()
    $results += "5. Est create valoracion: FAIL - $body"
}

# 6. Verify valoracion is anonymous (no alumno data)
try {
    $vals = Invoke-RestMethod -Uri "$base/valoraciones/asignatura/$asigId" -Method GET -Headers $estH
    $v = $vals | Select-Object -First 1
    $hasAlumno = $v.PSObject.Properties.Name -contains 'alumnoId'
    $results += "6. Anon check: has alumnoId=$hasAlumno, profesorNombre=$($v.profesorNombre), puntosMejora=$($v.puntosMejora)"
} catch {
    $results += "6. Anon check: FAIL"
}

# 7. Estudiante tries duplicate (should fail)
try {
    Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $estH -Body "{`"puntuacion`":3,`"comentario`":`"Otra valoracion`",`"profesorId`":$($profPersona.id),`"asignaturaId`":$asigId}"
    $results += "7. Duplicate check: FAIL (allowed duplicate)"
} catch {
    $sr = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($sr)
    $body = $reader.ReadToEnd()
    $results += "7. Duplicate blocked: OK - $body"
}

# 8. Profesor tries to create valoracion (should fail 403)
try {
    Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $profH -Body "{`"puntuacion`":5,`"comentario`":`"Test`",`"profesorId`":$($profPersona.id),`"asignaturaId`":$asigId}"
    $results += "8. Prof create val: FAIL (should be denied)"
} catch {
    $status = [int]$_.Exception.Response.StatusCode
    $results += "8. Prof create val: DENIED ($status) - OK"
}

# 9. Test AI moderation - insult
try {
    Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $estH -Body "{`"puntuacion`":1,`"comentario`":`"Este profesor es un idiota total`",`"profesorId`":$($profPersona.id),`"asignaturaId`":$asigId}"
    $results += "9. Insult moderation: FAIL (allowed insult)"
} catch {
    $sr = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($sr)
    $body = $reader.ReadToEnd()
    $results += "9. Insult blocked: OK - $body"
}

# 10. Test AI moderation - vulgarities
try {
    Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $estH -Body "{`"puntuacion`":1,`"comentario`":`"Vete a la mierda profesor`",`"profesorId`":$($profPersona.id),`"asignaturaId`":$asigId}"
    $results += "10. Vulgar moderation: FAIL (allowed vulgarity)"
} catch {
    $sr = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($sr)
    $body = $reader.ReadToEnd()
    $results += "10. Vulgar blocked: OK - $body"
}

# 11. Test AI moderation - puntos de mejora with insult
try {
    Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $estH -Body "{`"puntuacion`":2,`"comentario`":`"Regular`",`"puntosMejora`":`"Es un imbecil, no sabe nada`",`"profesorId`":$($profPersona.id),`"asignaturaId`":$asigId}"
    $results += "11. PuntosMejora moderation: FAIL (allowed insult in puntosMejora)"
} catch {
    $sr = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($sr)
    $body = $reader.ReadToEnd()
    $results += "11. PuntosMejora insult blocked: OK - $body"
}

# 12. Non-enrolled student test - admin creates another asig without student enrollment
$asig2 = Invoke-RestMethod -Uri "$base/asignaturas" -Method POST -Headers $adminH -Body '{"nombre":"Quimica","descripcion":"Quimica basica","url":"quim1"}'
$asig2Id = $asig2.id
Invoke-RestMethod -Uri "$base/asignaturas/$asig2Id/profesores/$($profPersona.id)" -Method POST -Headers $adminH
# Student NOT enrolled in asig2
try {
    Invoke-RestMethod -Uri "$base/valoraciones" -Method POST -Headers $estH -Body "{`"puntuacion`":4,`"comentario`":`"Buen curso`",`"profesorId`":$($profPersona.id),`"asignaturaId`":$asig2Id}"
    $results += "12. Non-enrolled check: FAIL (allowed non-enrolled)"
} catch {
    $sr = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($sr)
    $body = $reader.ReadToEnd()
    $results += "12. Non-enrolled blocked: OK - $body"
}

# 13. Get promedio
try {
    $prom = Invoke-RestMethod -Uri "$base/valoraciones/profesor/$($profPersona.id)/promedio" -Method GET -Headers $estH
    $results += "13. Promedio profesor: $($prom.promedio)"
} catch {
    $results += "13. Promedio: FAIL"
}

# 14. Admin delete valoracion
try {
    $allVals = Invoke-RestMethod -Uri "$base/valoraciones" -Method GET -Headers $adminH
    if ($allVals.Count -gt 0) {
        $valId = $allVals[0].id
        Invoke-RestMethod -Uri "$base/valoraciones/$valId" -Method DELETE -Headers $adminH
        $results += "14. Admin delete val: OK"
    } else {
        $results += "14. Admin delete: No vals to delete"
    }
} catch {
    $results += "14. Admin delete: FAIL"
}

# Output results
$results | Out-File 'C:\Users\fjanillo\Documents\application-iTeaching\test_val_results.txt' -Encoding utf8
Write-Host "Tests complete"
