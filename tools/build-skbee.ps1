# Check PowerShell version
function Check-Powershell {
    $minimumVersion = [Version]"7.0.0"
    $currentVersion = $PSVersionTable.PSVersion
    
    if ($currentVersion -lt $minimumVersion) {
        Write-Host "This script requires PowerShell version 7.0 or newer. Please update PowerShell." -ForegroundColor Red
        exit 1
    } else {
        Write-Host "Powershell version 7.0 or newer found" -ForegroundColor Green
    }
}

# Function to write text with a specific color
function Write-ColorText {
    param(
        [string]$Text,
        [System.ConsoleColor]$FColor,
        [System.ConsoleColor]$BColor
    )
    if ($FColor -in [System.Enum]::GetNames([System.ConsoleColor])) {
        if ($BColor -eq $null) {
            Write-Host $Text -ForegroundColor $FColor -BackgroundColor Black
        } else {
            Write-Host $Text -ForegroundColor $FColor -BackgroundColor $BColor
        }
    } else {
        Write-Host $Text
    }
}

# Function to check Java version
function Get-JavaVersion {
    $javaVersionOutput = & java -version 2>&1 | Select-Object -First 1
    if ($javaVersionOutput -match 'version "(\d+)\.(\d+)\.(\d+)_\d+"') {
        return [Version]"$($matches[1]).$($matches[2]).$($matches[3])"
    } elseif ($javaVersionOutput -match 'version "(\d+)"') {
        return [Version]$matches[1]
    } elseif ($javaVersionOutput -match '(\d+)\.(\d+)\.(\d+)') {
        return [Version]"$($matches[1]).$($matches[2]).$($matches[3])"
    } else {
        return $null
    }
}

function Install-SkBee {

    $insideTools = $false
    
    # Get the current directory name
    $currentDirectoryName = (Get-Item -Path ".\").Name
    
    # Check if the current directory is named "tools"
    if ($currentDirectoryName -eq "tools") {
        Write-Host "Current directory is 'tools'." -ForegroundColor Green
        $insideTools = $true
    }

    if($insideTools -eq $true) { Set-Location .. }
    
    Write-ColorText "Installing SkBee" Cyan
    
    # Set local JAVA_HOME and update PATH for JDK 20
    $graalvmPath = "" #"C:\tools\.jdks\graalvm-ce-21.0.22"
    
    # Check if folder exists
    if (Test-Path $graalvmPath) {
        $env:JAVA_HOME = $graalvmPath
    } else {
        $env:JAVA_HOME = [System.Environment]::GetEnvironmentVariable("JAVA_HOME")
    }
    
    $javaVersion = Get-JavaVersion
    
    if ($javaVersion -ge [Version]"20.0.0") {
        Write-Host "Java version: $javaVersion" -ForegroundColor Green
    } else {
        Write-ColorText "Java version is older than 20. Please provide the path to a JDK 20 or newer." Red
        $jdkPath = Read-Host "Enter the path to JDK 20 or newer"
        if (Test-Path $jdkPath) {
            $env:JAVA_HOME = $jdkPath
            $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
        } else {
            Write-ColorText "Invalid JDK path provided. Exiting script." Red
            exit 1
        }
    }
    
    # Print Java version
    $javaVersion = & java --version | Select-Object -First 1
    Write-ColorText "Java version: $javaVersion" Green
    
    # Clone the repository from GitHub
    Write-ColorText "Cloning the repository..." Yellow
    git clone "https://github.com/ShaneBeee/SkBee.git" >$null 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-ColorText "Error cloning the repository." Red
        exit $LASTEXITCODE
    }
    Write-ColorText "Repository cloned successfully." Green
    
    # Change to the repository directory
    Set-Location SkBee
    
    # Build the project using Gradle Wrapper
    Write-ColorText "Building the project using Gradle Wrapper..." Yellow
    & ./gradlew clean build
    
    # Get the name of the JAR file from the libs directory
    $jarFileName = Get-ChildItem -Path .\build\libs\*.jar | Sort-Object LastWriteTime -Descending | Select-Object -First 1 -ExpandProperty Name
    
    # Extract the version from the JAR file name
    $delimiter = "SkBee-"
    $Dversion = ($jarFileName -split $delimiter)[1] -replace ".jar", ""
    
    # Install the JAR file into the local Maven repository
    Set-Location ..
    
    Write-ColorText "Installing the JAR file into the local Maven repository..." Yellow
    mvn install:install-file -Dfile="SkBee\build\libs\$jarFileName" -DgroupId="com.shanebeestudios.skbee" -DartifactId="skbee" -Dversion=$Dversion -Dpackaging=jar
    
    # Check if pom.xml already contains SkBee dependency
    $pomPath = "./pom.xml"
    $pomContent = Get-Content -Path $pomPath
    
    [xml]$pomXml = Get-Content -Path $pomPath
    $dependenciesNode = $pomXml.project.dependencies
    $hasSkBee = $false
    $nonVersion = $false
    
    foreach ($dependency in $dependenciesNode.dependency) {
        if ($dependency.groupId -eq "com.shanebeestudios.skbee") {
            if ($dependency.version -eq $Dversion) {
                $hasSkBee = $true
                $nonVersion = $true
            } else {
                $hasSkbee = $false
            }
        }
    }
    
    if (-not $hasSkBee) {
        if ($nonVersion) {
            $dependenciesNode.RemoveChild($dependency)
        }
        
        if (-not $dependenciesNode) {
            $dependenciesNode = $pomXml.CreateElement("dependencies")
            $pomXml.project.AppendChild($dependenciesNode)
        }
    
        $dependencyNode = $pomXml.CreateElement("dependency")
    
        $groupIdNode = $pomXml.CreateElement("groupId")
        $groupIdNode.InnerText = "com.shanebeestudios.skbee"
        $dependencyNode.AppendChild($groupIdNode)
    
        $artifactIdNode = $pomXml.CreateElement("artifactId")
        $artifactIdNode.InnerText = "skbee"
        $dependencyNode.AppendChild($artifactIdNode)
    
        $versionNode = $pomXml.CreateElement("version")
        $versionNode.InnerText = $Dversion
        $dependencyNode.AppendChild($versionNode)
    
        $dependenciesNode.AppendChild($dependencyNode)
    
        # Save the updated pom.xml
        $pomXml.Save($pomPath)
        Write-ColorText "SkBee dependency added to pom.xml." Green
    } else {
        Write-ColorText "SkBee dependency already exists in pom.xml." Yellow
    }
    
    # Remove all xmlns attributes from the pom.xml file
    $pomContent = Get-Content -Path $pomPath
    $pomContent = $pomContent -replace '\s+xmlns="[^"]*"', ''
    Set-Content -Path $pomPath -Value $pomContent
    Write-ColorText "All xmlns attributes removed from pom.xml." Green
    
    # Delete the SkBee directory
    Remove-Item -Recurse -Force SkBee
    if($insideTools -eq $true) { cd ./tools }

}

# Run the script
Check-Powershell

# Title and Introduction
Write-ColorText "SkBee Installation Script" Cyan
Write-Host "This script will install SkBee and its dependencies." -ForegroundColor Green

Install-SkBee