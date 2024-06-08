#!/bin/bash

# Function to write text with a specific color
function write_color_text {
    text="$1"
    color="$2"

    case $color in
        "green")
            echo -e "\033[32m$text\033[0m"
            ;;
        "yellow")
            echo -e "\033[33m$text\033[0m"
            ;;
        "red")
            echo -e "\033[31m$text\033[0m"
            ;;
        "cyan")
            echo -e "\033[36m$text\033[0m"
            ;;
        *)
            echo "$text"
            ;;
    esac
}

write_color_text "Installing SkBee" "cyan"

# Clone the repository from GitHub
write_color_text "Cloning the repository..." "yellow"
git clone "https://github.com/ShaneBeee/SkBee.git" >/dev/null 2>&1
if [ $? -ne 0 ]; then
    write_color_text "Error cloning the repository." "red"
    exit $?
fi
write_color_text "Repository cloned successfully." "green"

# Change to the repository directory
cd SkBee || exit

# Run Gradle Wrapper to build the project
write_color_text "Building the project using Gradle Wrapper..." "yellow"
./gradlew clean build

# Get the name of the JAR file from the libs directory
jarFileName=$(ls -t build/libs/*.jar | head -n1)
Dversion=$(echo "$jarFileName" | cut -d'-' -f2 | cut -d'.' -f1)

# Install the JAR file into the local Maven repository
# Change to the directory where the JAR file is located
cd ..

write_color_text "Installing the JAR file into the local Maven repository..." "yellow"
mvn install:install-file -Dfile="$jarFileName" -DgroupId="com.shanebeestudios.skbee" -DartifactId="skbee" -Dversion="$Dversion" -Dpackaging=jar

# Check if pom.xml already contains SkBee dependency
pomPath="./pom.xml"

# Load dependencies from pom.xml
dependenciesNode=$(xmlstarlet sel -t -m '//project/dependencies' -c . "$pomPath")

hasSkBee=false
nonVersion=false

for dependency in $(xmlstarlet sel -t -m '//project/dependencies/dependency' -c . "$pomPath"); do
    groupId=$(xmlstarlet sel -t -v 'groupId' <<< "$dependency")
    version=$(xmlstarlet sel -t -v 'version' <<< "$dependency")

    if [ "$groupId" == "com.shanebeestudios.skbee" ]; then
        if [ "$version" == "$Dversion" ]; then
            hasSkBee=true
            nonVersion=true
        else
            hasSkBee=false
        fi
    fi
done

if [ "$hasSkBee" = false ]; then
    if [ "$nonVersion" = true ]; then
        xmlstarlet ed -L -d '//project/dependencies/dependency[groupId="com.shanebeestudios.skbee"]' "$pomPath"
    fi

    xmlstarlet ed -L -s '//project' -t elem -n "dependenciesNode" \
    -i "//dependenciesNode" -t attr -n "groupId" -v "com.shanebeestudios.skbee" \
    -i "//dependenciesNode" -t attr -n "artifactId" -v "skbee" \
    -i "//dependenciesNode" -t attr -n "version" -v "$Dversion" \
    -r "//project" -v "//dependenciesNode" "$pomPath"

    write_color_text "SkBee dependency added to pom.xml." "green"
else
    write_color_text "SkBee dependency already exists in pom.xml." "yellow"
fi

# Remove all xmlns attributes from the pom.xml file
xmlstarlet ed -L -d '//@xmlns' "$pomPath"
write_color_text "All xmlns attributes removed from pom.xml." "green"

# Delete the SkBee directory
rm -rf SkBee
