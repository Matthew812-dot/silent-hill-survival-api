param()
$ErrorActionPreference = 'Stop'
mvn test-compile
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
mvn exec:java '-Dexec.mainClass=app.ApiChecks' '-Dexec.classpathScope=test'
if ($LASTEXITCODE -ne 0) { throw 'HTTP checks failed' }

