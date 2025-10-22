#!/bin/bash

set -o errexit
set -o pipefail
set -o nounset

# Global variables
PLUGIN_XML="src/main/resources/META-INF/plugin.xml"
GRADLE_PROPERTIES="gradle.properties"

# Function to display usage information
usage() {
    printf "Usage: %s [-h|--help]\n" "$(basename "$0")"
    printf "Description: This script increments the version number of an IntelliJ plugin, builds the project, and optionally pushes changes to the origin.\n\n"
    printf "Options:\n"
    printf "  -h, --help  Show this help message and exit\n"
}

# Function to read current version from plugin.xml
get_current_version_plugin_xml() {
    local version
    version=$(sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p' "$PLUGIN_XML")
    printf "%s" "$version"
}

# Function to read current version from gradle.properties
get_current_version_gradle_properties() {
    local version=""
    while IFS= read -r line; do
        # printf "DEBUG: Read line: '%s'\n" "$line" >&2
        if [[ "$line" =~ ^pluginVersion[[:space:]]*=[[:space:]]*(.+)$ ]]; then
            version="${BASH_REMATCH[1]}"
            break
        fi
    done < "$GRADLE_PROPERTIES"
    printf "%s" "$version"
}

# Function to increment the minor version
increment_version() {
    local version="$1"
    local major minor patch
    IFS='.' read -r major minor patch <<< "$version"
    minor=$((minor + 1))
    printf "%d.%d.%d" "$major" "$minor" "$patch"
}

# Function to validate the new version
is_new_version_valid() {
    local current_version="$1"
    local new_version="$2"
    [ "$(printf '%s\n' "$current_version" "$new_version" | sort -V | head -n1)" != "$new_version" ]
}

# Function to check for uncommitted changes and handle git commit
handle_git_commit() {
    if ! git diff-index --quiet HEAD --; then
        printf "\n# Uncommitted changes detected. Files:\n"
        git status --short
        printf "\nEnter commit message (default: changes for version %s): " "$1"
        read -r commit_message
        commit_message=${commit_message:-"changes for version $1"}

        printf "\n# Adding and committing changes...\n"
        git add -A
        git commit -m "$commit_message"
    else
        printf "\n# No uncommitted changes detected.\n"
    fi
}

# Function to update version in plugin.xml
update_version_plugin_xml() {
    local new_version="$1"
    printf "\n# Changing the version in the plugin.xml file"
    sed -i '' "s/<version>.*<\/version>/<version>$new_version<\/version>/" "$PLUGIN_XML"
    printf "Done."
}

# Function to update version in gradle.properties
update_version_gradle_properties() {
    local new_version="$1"
    printf "\n# Changing the version in the gradle.properties file"
    sed -i '' "s/^pluginVersion[[:space:]]*=.*/pluginVersion=$new_version/" "$GRADLE_PROPERTIES"
    printf "Done."
}

# Function to build the project
run_gradle_build() {
    printf "\n# Running gradle build\n"
    if ! ./gradlew build; then
        printf "Gradle build failed.\n" >&2
        return 1
    fi
}

# Function to display the distribution file path
display_distribution_file() {
    local new_version="$1"
    printf "\nThe final distribution file is at: build/distributions/Copy_File_Content-%s.zip\n" "$new_version"
}

# Function to commit the version change
commit_version_change() {
    local new_version="$1"
    printf "\n# Committing version change\n"
    git add "$PLUGIN_XML" "$GRADLE_PROPERTIES"
    git commit -m "chore: version changed to $new_version"
}

# Function to create a git tag
create_git_tag() {
    local new_version="$1"
    printf "\n# Creating git tag %s\n" "$new_version"
    git tag -a "$new_version" -m "Release version $new_version" --force
}

# Function to push changes to origin
push_changes_to_origin() {
    printf "\n# Pushing changes to origin\n"
    git push origin
    git push origin --tags
}

# Main function
main() {
    if [[ $# -gt 0 ]]; then
        case $1 in
            -h|--help)
                usage
                exit 0
                ;;
            *)
                printf "Invalid option: %s\n\n" "$1" >&2
                usage
                exit 1
                ;;
        esac
    fi

    printf "Have you updated the change-notes tag in the plugin.xml file? [Y/n]: "
    read -r updated_change_notes
    updated_change_notes=${updated_change_notes:-Y}

    if [[ ! "$updated_change_notes" =~ ^[Yy]$ ]]; then
        printf "\nPlease update the change-notes tag in the plugin.xml file before proceeding.\n"
        exit 1
    fi

    local current_version_plugin_xml current_version_gradle_properties new_version

    current_version_plugin_xml=$(get_current_version_plugin_xml)
    current_version_gradle_properties=$(get_current_version_gradle_properties)

    # printf "DEBUG: plugin.xml version is '%s'\n" "$current_version_plugin_xml" >&2
    # printf "DEBUG: gradle.properties version is '%s'\n" "$current_version_gradle_properties" >&2

    if [ -z "$current_version_gradle_properties" ]; then
        printf "Failed to extract version from gradle.properties\n" >&2
        return 1
    fi

    if [ "$current_version_plugin_xml" != "$current_version_gradle_properties" ]; then
        printf "Version mismatch: plugin.xml version is %s, gradle.properties version is %s\n" "$current_version_plugin_xml" "$current_version_gradle_properties" >&2
        return 1
    fi

    printf "Current version is %s. Enter new version (default: %s): " "$current_version_plugin_xml" "$(increment_version "$current_version_plugin_xml")"
    read -r new_version
    new_version=${new_version:-$(increment_version "$current_version_plugin_xml")}

    until is_new_version_valid "$current_version_plugin_xml" "$new_version"; do
        printf "New version must be higher than the current version %s. Try again: " "$current_version_plugin_xml"
        read -r new_version
    done

    handle_git_commit "$new_version"
    update_version_plugin_xml "$new_version"
    update_version_gradle_properties "$new_version"
    commit_version_change "$new_version"
    run_gradle_build
    display_distribution_file "$new_version"
    create_git_tag "$new_version"

    printf "\nDo you want to push the changes and the tag to origin? [Y/n]: "
    read -r push_to_origin
    push_to_origin=${push_to_origin:-Y}

    if [[ "$push_to_origin" =~ ^[Yy]$ ]]; then
        push_changes_to_origin && printf "\nChanges and tag pushed to origin.\n" || printf "\nFailed to push changes and tag to origin.\n"
    else
        printf "\nChanges and tag not sent to origin.\n"
    fi
}

main "$@"
