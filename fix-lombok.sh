#!/bin/bash

echo "Fixing Lombok annotations in Java files..."

# Find all Java files that use 'log' variable but don't have @Slf4j annotation
find . -name "*.java" -type f -exec grep -l "log\." {} \; | xargs -I {} grep -L "@Slf4j" {} | while read -r file; do
    echo "Adding @Slf4j to $file"
    # Add import statement if not present
    grep -q "import lombok.extern.slf4j.Slf4j;" "$file" || sed -i".bak" '1,/package/s/package.*/&\n\nimport lombok.extern.slf4j.Slf4j;/' "$file"
    # Add @Slf4j annotation before class declaration
    sed -i".bak" 's/\(public class\|public interface\|public enum\|private class\|protected class\|class \)/@Slf4j\n\1/' "$file"
    # Remove backup files
    rm -f "$file.bak"
done

# Fix the Logger declaration in files that were handled manually
find . -name "*.java" -type f -exec grep -l "private static final org.slf4j.Logger" {} \; | while read -r file; do
    echo "Removing manual Logger definition in $file"
    # Add @Slf4j if not present
    grep -q "@Slf4j" "$file" || sed -i".bak" 's/\(public class\|public interface\|public enum\|private class\|protected class\|class \)/@Slf4j\n\1/' "$file"
    # Add import statement if not present
    grep -q "import lombok.extern.slf4j.Slf4j;" "$file" || sed -i".bak" '1,/package/s/package.*/&\n\nimport lombok.extern.slf4j.Slf4j;/' "$file"
    # Remove manual logger declaration
    sed -i".bak" '/private static final org.slf4j.Logger log/d' "$file"
    # Remove backup files
    rm -f "$file.bak"
done

echo "Fix completed. Please rebuild the project now."