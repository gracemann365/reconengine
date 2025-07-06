#!/bin/bash

echo "Starting Reconciliation Engine Scaffolding..."

# Define the root directory
ROOT_DIR=""

# Define the common Java package structure
JAVA_PKG="com/grace/recon"

# Define modules and their base paths
declare -a MODULES=("common" "naas" "orchestrator" "matcher" "reporter" "escalator" "monitor")
echo "Modules: ${MODULES[@]}"

# --- Function to create directories and files ---
create_structure() {
    local base_dir=$1
    local module_name=$2

    # Create module directory and its pom.xml
    mkdir -p "$base_dir/$module_name"
    touch "$base_dir/$module_name/pom.xml"

    # Create src/main/java and src/main/resources
    mkdir -p "$base_dir/$module_name/src/main/java/$JAVA_PKG/$module_name"
    touch "$base_dir/$module_name/src/main/java/$JAVA_PKG/$module_name/${module_name^}Application.java" # Example Java file
    mkdir -p "$base_dir/$module_name/src/main/resources"

    # Create src/test/java and src/test/resources
    mkdir -p "$base_dir/$module_name/src/test/java/$JAVA_PKG/$module_name"
    touch "$base_dir/$module_name/src/test/java/$JAVA_PKG/$module_name/${module_name^}ApplicationTest.java" # Example Test Java file
    mkdir -p "$base_dir/$module_name/src/test/resources"
}

# --- Main scaffolding logic ---

echo "Scaffolding project structure for '$ROOT_DIR'..."

# Create the root directory
mkdir -p "$ROOT_DIR"
cd "$ROOT_DIR" || { echo "Failed to change to $ROOT_DIR directory. Exiting."; exit 1; }
echo "Created root directory: $ROOT_DIR"

# Create the root pom.xml
cat << EOF > pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.grace.recon</groupId>
    <artifactId>reconengine</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>Reconciliation Engine POC</name>
    <description>Proof of Concept for a Reconciliation Engine</description>

    <properties>
        <java.version>17</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <modules>
        <module>common</module>
        <module>naas</module>
        <module>orchestrator</module>
        <module>matcher</module>
        <module>reporter</module>
        <module>escalator</module>
        <module>monitor</module>
    </modules>

    </project>
EOF

# Loop through modules and create their structures
for module in "${MODULES[@]}"; do
    echo "Creating module: $module"
    create_structure "." "$module"
done

echo "Scaffolding complete!"
echo "You are now in the '$ROOT_DIR' directory."
ls -R