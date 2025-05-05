import json
import os
import shutil
import subprocess
import platform
import requests
import zipfile
import tempfile
import argparse
from pathlib import Path
from typing import TypedDict, List, Optional, Dict, Any, Tuple

# ANSI color codes
class Colors:
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'

class SystemInfo:
    def __init__(self, system_type: str, shell: str, gradle_wrapper: str):
        self.system_type = system_type
        self.shell = shell
        self.gradle_wrapper = gradle_wrapper

def detect_system() -> SystemInfo:
    """Detects current system and returns relevant information."""
    system = platform.system().lower()
    
    # WSL detection
    is_wsl = False
    if system == "linux":
        try:
            with open("/proc/version", "r") as f:
                if "microsoft" in f.read().lower():
                    is_wsl = True
        except:
            pass
    
    if is_wsl:
        return SystemInfo("wsl", "bash", "./gradlew")
    elif system == "windows":
        return SystemInfo("windows", "cmd", "gradlew.bat")
    elif system == "linux":
        return SystemInfo("linux", "bash", "./gradlew")
    elif system == "darwin":  # macOS
        return SystemInfo("darwin", "bash", "./gradlew")
    else:
        raise RuntimeError(f"Unsupported system: {system}")

def get_system_info(config_system: str) -> SystemInfo:
    """Gets system information based on configuration or detects automatically."""
    if config_system == "auto":
        return detect_system()
    elif config_system == "wsl":
        return SystemInfo("wsl", "bash", "./gradlew")
    elif config_system == "windows":
        return SystemInfo("windows", "cmd", "gradlew.bat")
    else:
        raise ValueError(f"Unsupported system value in configuration: {config_system}")

def run_command(command: Tuple[str, ...], system_info: SystemInfo) -> subprocess.CompletedProcess:
    """Runs command with respect to current system."""
    if system_info.system_type == "windows":
        return subprocess.run(("cmd", "/c", *command))
    else:
        return subprocess.run(command)

def print_colored(text: str, color: str) -> None:
    print(f"{color}{text}{Colors.ENDC}")

def print_step(text: str) -> None:
    print_colored(f"\n{Colors.BOLD}>>> {text}{Colors.ENDC}", Colors.BLUE)

def parse_arguments() -> argparse.Namespace:
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(description='Run Skript tests with custom configuration.')
    parser.add_argument('--configuration', type=int, help='Configuration index to use (0-based)')
    parser.add_argument('--jdk', type=str, choices=['auto', 'download'], default='auto', 
                        help='JDK selection mode: auto (use existing) or download (download JDK 17)')
    parser.add_argument('--system', type=str, choices=['auto', 'windows', 'wsl', 'linux', 'darwin'], default='auto',
                        help='System type to use')
    parser.add_argument('--no-interactive', action='store_true', help='Run in non-interactive mode')
    return parser.parse_args()

def select_configuration(configurations: List[Dict[str, Any]], default_index: int, args: argparse.Namespace) -> Dict[str, Any]:
    """Allows user to select a configuration."""
    # If configuration is specified via command line, use it
    if args.configuration is not None:
        if 0 <= args.configuration < len(configurations):
            return configurations[args.configuration]
        else:
            print_colored(f"Invalid configuration index: {args.configuration}", Colors.RED)
            print_colored(f"Available configurations: 0-{len(configurations)-1}", Colors.RED)
            exit(1)
    
    # If in non-interactive mode, use default
    if args.no_interactive:
        return configurations[default_index]
    
    # Otherwise, interactive selection
    print_step("Available configurations:")
    for i, config in enumerate(configurations):
        print_colored(f"{i + 1}. {config['name']}", Colors.YELLOW)
        print_colored(f"   Skript version: {config['skript_repo_ref']}", Colors.GREEN)
    
    while True:
        try:
            choice = input(f"\nSelect configuration number (default is {default_index + 1}, or 'q' to quit): ")
            if choice.lower() == 'q':
                exit(0)
            
            if not choice:  # Empty input uses default value
                return configurations[default_index]
            
            index = int(choice) - 1
            if 0 <= index < len(configurations):
                return configurations[index]
            else:
                print_colored("Invalid choice, try again.", Colors.RED)
        except ValueError:
            print_colored("Please enter a valid number.", Colors.RED)

def find_jdk_installations() -> List[str]:
    """Finds all possible JDK installations in the system."""
    possible_paths = []
    
    # Program Files
    program_files = [
        os.environ.get('ProgramFiles', 'C:\\Program Files'),
        os.environ.get('ProgramFiles(x86)', 'C:\\Program Files (x86)')
    ]
    
    for program_files_path in program_files:
        if os.path.exists(program_files_path):
            # Looking for folders starting with "Java" or "jdk"
            for item in os.listdir(program_files_path):
                if item.lower().startswith(('java', 'jdk')):
                    full_path = os.path.join(program_files_path, item)
                    if os.path.isdir(full_path):
                        possible_paths.append(full_path)
    
    # Check JAVA_HOME
    java_home = os.environ.get('JAVA_HOME')
    if java_home and os.path.exists(java_home) and java_home not in possible_paths:
        possible_paths.append(java_home)
    
    return possible_paths

def select_jdk_installation() -> Optional[str]:
    """Allows user to select a JDK installation."""
    installations = find_jdk_installations()
    
    if not installations:
        print_colored("No JDK installations found!", Colors.RED)
        return None
    
    print_step("Found JDK installations:")
    for i, path in enumerate(installations, 1):
        try:
            # Try to get Java version
            java_exe = os.path.join(path, 'bin', 'java.exe')
            if os.path.exists(java_exe):
                version = subprocess.run([java_exe, '-version'], capture_output=True, text=True, stderr=subprocess.STDOUT)
                print_colored(f"{i}. {path}", Colors.YELLOW)
                print_colored(f"   Version: {version.stderr.strip()}", Colors.GREEN)
            else:
                print_colored(f"{i}. {path} (java.exe not found)", Colors.RED)
        except Exception:
            print_colored(f"{i}. {path} (error getting version)", Colors.RED)
    
    while True:
        try:
            choice = input("\nSelect JDK installation number (or 'q' to quit): ")
            if choice.lower() == 'q':
                return None
            
            index = int(choice) - 1
            if 0 <= index < len(installations):
                return installations[index]
            else:
                print_colored("Invalid choice, try again.", Colors.RED)
        except ValueError:
            print_colored("Please enter a valid number.", Colors.RED)

def check_java_version_compatibility(java_path: str) -> bool:
    """Checks if Java version is compatible with Gradle."""
    try:
        java_exe = os.path.join(java_path, "bin", "java.exe" if os.name == "nt" else "java")
        if not os.path.exists(java_exe):
            print_colored(f"Java executable not found at: {java_exe}", Colors.RED)
            return False

        version_output = subprocess.run([java_exe, "-version"], 
                                     stdout=subprocess.PIPE,
                                     stderr=subprocess.PIPE,
                                     text=True)
        version_str = version_output.stderr
        
        # Extract Java major version
        if "version" in version_str:
            version = version_str.split("version")[1].split()[0].strip('"')
            major_version = int(version.split('.')[0])
            
            # Gradle 8.x requires Java 17
            if major_version == 17:
                print_colored(f"Java version {version} is ideal for Gradle", Colors.GREEN)
                return True
            elif 17 <= major_version <= 21:
                print_colored(f"Java version {version} is compatible with Gradle, but Java 17 is recommended", Colors.YELLOW)
                return True
            else:
                print_colored(f"Java version {version} is not compatible with Gradle (Java 17 required)", Colors.RED)
                return False
    except Exception as e:
        print_colored(f"Error checking Java version at {java_path}: {str(e)}", Colors.RED)
        return False

def download_jdk(version: int = 17) -> Optional[str]:
    """Downloads and installs JDK."""
    print_step("Downloading JDK")
    
    # Detect operating system
    system = platform.system().lower()
    if system == "windows":
        os_name = "windows"
        arch = "x64"
    elif system == "linux":
        os_name = "linux"
        arch = "x64"
    elif system == "darwin":
        os_name = "macos"
        arch = "x64"
    else:
        print_colored(f"Unsupported operating system: {system}", Colors.RED)
        return None
    
    # Create temporary directory for download
    with tempfile.TemporaryDirectory() as temp_dir:
        temp_dir_path = Path(temp_dir)
        
        # Distribution selection
        print_colored("\nSelect JDK distribution:", Colors.YELLOW)
        print_colored("1. Amazon Corretto (recommended)", Colors.GREEN)
        print_colored("2. Azul Zulu", Colors.GREEN)
        
        while True:
            choice = input("\nYour choice (1/2): ")
            if choice in ["1", "2"]:
                break
            print_colored("Invalid choice, try again.", Colors.RED)
        
        # Download URL based on selection
        if choice == "1":  # Corretto
            url = f"https://corretto.aws/downloads/latest/amazon-corretto-{version}-{arch}-{os_name}-jdk.zip"
            jdk_name = f"amazon-corretto-{version}"
        else:  # Zulu
            url = f"https://cdn.azul.com/zulu/bin/zulu{version}.32.17-ca-jdk{version}.0.2-{os_name}_{arch}.zip"
            jdk_name = f"zulu{version}"
        
        print_colored(f"Downloading JDK from: {url}", Colors.YELLOW)
        
        try:
            # Download file
            response = requests.get(url, stream=True)
            response.raise_for_status()
            
            # Save to temporary file
            zip_path = temp_dir_path / f"{jdk_name}.zip"
            total_size = int(response.headers.get('content-length', 0))
            block_size = 8192
            downloaded = 0
            
            with open(zip_path, "wb") as f:
                for chunk in response.iter_content(chunk_size=block_size):
                    f.write(chunk)
                    downloaded += len(chunk)
                    # Calculate download percentage
                    if total_size > 0:
                        percent = int((downloaded / total_size) * 100)
                        print(f"\rDownloading: {percent}%", end="", flush=True)
            print("\n")  # New line after download
            
            print_colored("Extracting JDK...", Colors.YELLOW)
            
            # Extract ZIP file
            with zipfile.ZipFile(zip_path, "r") as zip_ref:
                zip_ref.extractall(temp_dir_path)
            
            # Find extracted JDK directory
            jdk_dir = None
            for item in temp_dir_path.iterdir():
                if item.is_dir() and (jdk_name.lower() in item.name.lower() or "jdk" in item.name.lower()):
                    jdk_dir = item
                    break
            
            if not jdk_dir:
                print_colored("Could not find extracted JDK directory", Colors.RED)
                return None
            
            # Create target directory
            target_dir = Path.home() / ".jdks" / jdk_name
            target_dir.parent.mkdir(parents=True, exist_ok=True)
            
            # Move JDK to target directory
            if target_dir.exists():
                shutil.rmtree(target_dir)
            shutil.move(str(jdk_dir), str(target_dir))
            
            print_colored(f"JDK successfully installed to: {target_dir}", Colors.GREEN)
            return str(target_dir)
            
        except Exception as e:
            print_colored(f"Error downloading JDK: {e}", Colors.RED)
            return None

def find_compatible_jdk(args: argparse.Namespace) -> Optional[str]:
    """Finds compatible JDK in the following order:
    1. Check JDK_PATH from configuration
    2. Check installations in .jdks
    3. Offer to download new version
    """
    print_step("Looking for compatible JDK")
    
    # If JDK mode is 'download', directly download JDK 17
    if args.jdk == 'download':
        print_colored("Downloading JDK 17 as requested", Colors.YELLOW)
        return download_jdk(17)
    
    # 1. First check JDK from configuration
    if jdk_path and os.path.exists(jdk_path):
        print_colored(f"Checking JDK from configuration: {jdk_path}", Colors.YELLOW)
        if check_java_version_compatibility(jdk_path):
            return jdk_path
        else:
            print_colored("JDK from configuration is not compatible", Colors.RED)
    
    # 2. Check installations in .jdks
    jdks_dir = Path.home() / ".jdks"
    if jdks_dir.exists():
        print_colored(f"Looking for compatible JDK in: {jdks_dir}", Colors.YELLOW)
        for jdk_dir in jdks_dir.iterdir():
            if jdk_dir.is_dir():
                print_colored(f"Checking: {jdk_dir}", Colors.YELLOW)
                if check_java_version_compatibility(str(jdk_dir)):
                    return str(jdk_dir)
    
    # 3. Check system installations
    installations = find_jdk_installations()
    for path in installations:
        print_colored(f"Checking system installation: {path}", Colors.YELLOW)
        if check_java_version_compatibility(path):
            return path
    
    # 4. If nothing found, offer to download
    print_colored("\nNo compatible JDK found", Colors.RED)
    
    # In non-interactive mode, exit with error
    if args.no_interactive:
        print_colored("No compatible JDK found and running in non-interactive mode", Colors.RED)
        exit(1)
    
    choice = input("Do you want to download Java 17? (yes/no): ")
    if choice.lower() in ["yes", "y"]:
        return download_jdk(17)
    
    return None

class EnvironmentResource(TypedDict):
    source: str
    target: str


def delete_contents_of_directory(directory: Path) -> None:
    for path in directory.iterdir():
        if path.is_file():
            path.unlink()
        elif path.is_dir():
            shutil.rmtree(path)


# Main execution
if __name__ == "__main__":
    # Parse command line arguments
    args = parse_arguments()
    
    # Load configuration from config.json
    print_step("Loading configuration from config.json")
    with open("./config.json") as f:
        config_data = json.load(f)

    # Get system information
    system_info = get_system_info(args.system if args.system != 'auto' else config_data.get("system", "auto"))
    print_step(f"Detected system: {system_info.system_type}")
    print_colored(f"  Shell: {system_info.shell}", Colors.YELLOW)
    print_colored(f"  Gradle wrapper: {system_info.gradle_wrapper}", Colors.YELLOW)

    configurations = config_data["configurations"]
    default_index = config_data.get("default_configuration", 0)

    # Let user select configuration
    config = select_configuration(configurations, default_index, args)

    workspace_directory = Path(config["workspace_directory"]).resolve()
    test_script_directory = workspace_directory / config["test_script_directory"]
    skript_repo_ref = config.get("skript_repo_ref", None)
    run_vanilla_tests = config.get("run_vanilla_tests", False)
    skript_repo_git_url = config.get("skript_repo_git_url", "https://github.com/SkriptLang/Skript.git")
    skript_repo_path = Path("skript").resolve()  # Always use "skript" folder
    jdk_path = config.get("jdk_path", None)

    extra_plugins_directory = None
    extra_plugins_directory_string = config.get("extra_plugins_directory", None)
    if extra_plugins_directory_string:
        extra_plugins_directory = workspace_directory / extra_plugins_directory_string

    skript_test_directory = skript_repo_path / "src" / "test" / "skript" / "tests"
    custom_test_directory = skript_test_directory / "custom"

    print_step("Selected configuration:")
    print_colored(f"  Name: {config['name']}", Colors.YELLOW)
    print_colored(f"  Skript version: {skript_repo_ref}", Colors.YELLOW)
    print_colored(f"  Test script directory: {test_script_directory}", Colors.YELLOW)
    print_colored(f"  Run vanilla tests: {run_vanilla_tests}", Colors.YELLOW)
    print_colored(f"  Extra plugins directory: {extra_plugins_directory}", Colors.YELLOW)
    print_colored(f"  Skript repo path: {skript_repo_path}", Colors.YELLOW)
    if jdk_path:
        print_colored(f"  JDK path: {jdk_path}", Colors.YELLOW)

    # Delete repo path if it already exists
    if skript_repo_path.exists():
        print_step(f"Updating existing repository: {skript_repo_path}")
        os.chdir(skript_repo_path)
        # Update main repository
        subprocess.run(("git", "fetch", "--all"), check=True)
        subprocess.run(("git", "reset", "--hard", "origin/master"), check=True)
        # Update submodules
        subprocess.run(("git", "submodule", "update", "--init", "--recursive"), check=True)
    else:
        print_step("Cloning Skript repository")
        subprocess.run(("git", "clone", "--recurse-submodules", skript_repo_git_url, str(skript_repo_path)), check=True)
        os.chdir(skript_repo_path)

    # Checkout specific ref if provided
    if skript_repo_ref and not skript_repo_ref.isspace():
        print_step(f"Switching to specific version: {skript_repo_ref}")
        subprocess.run(("git", "checkout", "-f", skript_repo_ref), check=True)
        subprocess.run(("git", "submodule", "update", "--recursive"), check=True)

    # Remove vanilla tests if disabled
    if not run_vanilla_tests:
        print_step("Removing vanilla tests")
        delete_contents_of_directory(skript_test_directory)

    # Copy custom test scripts
    print_step("Copying custom test scripts")
    shutil.rmtree(custom_test_directory, ignore_errors=True)
    shutil.copytree(test_script_directory, custom_test_directory)

    # Add extra plugins to environments
    if extra_plugins_directory and extra_plugins_directory.exists():
        print_step("Adding extra plugins to environments")
        environments_dir = skript_repo_path / "src" / "test" / "skript" / "environments"
        for environment_file_path in environments_dir.glob("**/*.json"):
            print_colored(f"  Processing file: {environment_file_path}", Colors.YELLOW)
            with open(environment_file_path, "r") as environment_file:
                environment = json.load(environment_file)
                if "resources" not in environment:
                    environment["resources"] = []
                resources = environment["resources"]
                for plugin_path in extra_plugins_directory.iterdir():
                    print_colored(f"    Adding plugin: {plugin_path.name}", Colors.GREEN)
                    resources.append(EnvironmentResource(
                        source=str(plugin_path.absolute().resolve()),
                        target=f"plugins/{plugin_path.name}"
                    ))
            with open(environment_file_path, "w") as environment_file:
                json.dump(environment, environment_file, indent=2)

    # Check Java installation before running tests
    print_step("Checking Java installation")
    compatible_jdk = find_compatible_jdk(args)
    if compatible_jdk:
        print_colored(f"Using JDK: {compatible_jdk}", Colors.GREEN)
        os.environ['JAVA_HOME'] = compatible_jdk
        # Add to PATH
        path = os.environ.get('PATH', '')
        java_bin = os.path.join(compatible_jdk, 'bin')
        if java_bin not in path:
            os.environ['PATH'] = f"{java_bin};{path}"
    else:
        print_colored("No compatible JDK found (Java 17 required)", Colors.RED)
        exit(1)

    # Run tests
    print_step("Running tests")
    gradle_test_process = run_command((system_info.gradle_wrapper, "quickTest"), system_info)
    if gradle_test_process.returncode == 0:
        print_colored("\nTests completed successfully!", Colors.GREEN)
    else:
        print_colored("\nTests failed!", Colors.RED)
        print_colored(f"Exit code: {gradle_test_process.returncode}", Colors.RED)

    # Clean up test_runners directory
    test_runners_dir = skript_repo_path / "build" / "test_runners"
    if test_runners_dir.exists():
        print_step("Cleaning up test_runners directory")
        shutil.rmtree(test_runners_dir)
        print_colored("Test runners directory cleaned up", Colors.GREEN)

    exit(gradle_test_process.returncode)
