# Contributing to MultiVPN

Thank you for your interest in contributing to MultiVPN! This document provides guidelines and instructions for contributing.

## Code of Conduct

- Be respectful and inclusive
- Focus on the code, not the person
- Help others learn and grow
- Report inappropriate behavior to maintainers

## Getting Started

### 1. Fork the Repository
```bash
git clone https://github.com/yourusername/MultiVPN.git
cd MultiVPN
```

### 2. Create a Feature Branch
```bash
git checkout -b feature/your-feature-name
```

### 3. Set Up Development Environment
Follow the [SETUP.md](SETUP.md) guide for detailed instructions.

## Development Guidelines

### Kotlin Style Guide
- Follow [Official Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Max line length: 100 characters
- Indentation: 4 spaces
- Use meaningful variable names

### Code Quality
- Write clean, readable code
- Add comments for complex logic
- Use IDE's code inspections
- Follow DRY (Don't Repeat Yourself) principle

### Naming Conventions
```kotlin
// Classes and interfaces
class MainActivity : AppCompatActivity()
interface VpnRepository

// Functions and variables
fun connectVpn()
private val vpnState: LiveData<Boolean>

// Constants
companion object {
    const val TAG = "VpnService"
    const val CONNECTION_TIMEOUT = 30000
}
```

### Architecture Standards
- Use MVVM pattern
- Repository pattern for data access
- Hilt for dependency injection
- Coroutines for async operations
- LiveData/Flow for reactive updates

## Commit Guidelines

### Commit Message Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation change
- `style`: Code style change (formatting)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process, dependencies

### Examples
```
feat(vpn): add kill switch feature
fix(connection): resolve reconnection timeout issue
docs: update installation guide
test(viewmodel): add unit tests for MainViewModel
```

### Best Practices
- One commit per feature/fix
- Write descriptive commit messages
- Reference issues: `Fixes #123`
- Test before committing

## Pull Request Process

### Before Submitting
1. **Update your branch**
   ```bash
   git fetch origin
   git rebase origin/main
   ```

2. **Run tests**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

3. **Check code quality**
   ```bash
   ./gradlew lint
   ./gradlew detekt  # if using detekt
   ```

4. **Build the app**
   ```bash
   ./gradlew build
   ```

### Create Pull Request
1. Push your branch to GitHub
   ```bash
   git push origin feature/your-feature-name
   ```

2. Create PR with:
   - Clear title
   - Detailed description
   - Link to related issues
   - Screenshots (if UI change)
   - Testing instructions

3. PR Template
   ```markdown
   ## Description
   Brief description of changes
   
   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   
   ## How to Test
   Steps to test the changes
   
   ## Screenshots (if applicable)
   Add screenshots of UI changes
   
   ## Checklist
   - [ ] Code follows style guidelines
   - [ ] New tests added
   - [ ] Tests pass locally
   - [ ] Documentation updated
   ```

## Testing Requirements

### Unit Tests
- Minimum 80% code coverage
- Test happy path and error cases
- Use meaningful test names

```kotlin
@Test
fun testConnectVpn_whenSuccessful_shouldUpdateState() {
    // Arrange
    whenever(vpnRepository.connectVpn()).thenReturn(true)
    
    // Act
    viewModel.toggleVpn()
    
    // Assert
    assertTrue(viewModel.vpnState.value)
}
```

### Instrumentation Tests
- Test critical user flows
- Use Espresso for UI tests
- Test on multiple API levels

### Test Coverage
```bash
./gradlew testDebugUnitTestCoverage
# View report: app/build/reports/coverage/debug/index.html
```

## Documentation

### Code Documentation
```kotlin
/**
 * Connects to VPN with specified configuration
 * 
 * @param config VPN configuration
 * @return true if connection successful, false otherwise
 * @throws VpnConnectionException if connection fails
 */
suspend fun connectVpn(config: VpnConfig): Boolean
```

### README Updates
- Update features list
- Update dependencies
- Add configuration examples

### API Documentation
- Document new APIs
- Add code examples
- Include error handling

## Issue Reporting

### Bug Reports
Include:
- Device and Android version
- Steps to reproduce
- Expected behavior
- Actual behavior
- Logs/screenshots

### Feature Requests
Include:
- Use case description
- Why it's needed
- Proposed solution
- Alternatives considered

## Code Review Process

### What Reviewers Look For
- ✅ Code quality and style
- ✅ Test coverage
- ✅ Documentation
- ✅ Performance
- ✅ Security
- ✅ Backward compatibility

### Responding to Review
- Address all comments
- Push follow-up commits
- Explain decisions when needed
- Thank reviewers

## Setup Code Analysis Tools

### Android Lint
```bash
./gradlew lint
# View report: app/build/reports/lint-results-debug.html
```

### Static Analysis
```bash
./gradlew detekt  # if configured
```

### Code Formatting
```bash
./gradlew spotlessApply  # if configured
```

## Common Tasks

### Adding a New Feature
1. Create feature branch
2. Add tests first (TDD)
3. Implement feature
4. Update documentation
5. Submit PR

### Fixing a Bug
1. Create issue
2. Create bugfix branch
3. Write failing test
4. Fix bug
5. Verify test passes
6. Submit PR

### Updating Dependencies
1. Create update branch
2. Update dependency version
3. Test thoroughly
4. Submit PR with testing results

## Git Workflow

### Useful Commands
```bash
# Update main branch
git fetch origin
git pull origin main

# List branches
git branch -a

# Delete branch
git branch -d feature/name
git push origin --delete feature/name

# View commit history
git log --oneline -10

# View changes
git diff
git diff --cached

# Stash changes
git stash
git stash pop
```

## Troubleshooting

### Merge Conflicts
```bash
git fetch origin
git rebase origin/main
# Resolve conflicts in IDE
git add .
git rebase --continue
git push origin feature/name --force
```

### Failed Tests
1. Run tests locally
2. Check test output
3. Debug and fix
4. Push updated code

### Build Errors
1. Clean project: `./gradlew clean`
2. Sync Gradle: `./gradlew sync`
3. Check dependencies
4. Review error messages

## Resources

- [Kotlin Docs](https://kotlinlang.org/docs/)
- [Android Docs](https://developer.android.com/docs)
- [Architecture Components](https://developer.android.com/topic/libraries/architecture)
- [Git Documentation](https://git-scm.com/doc)

## Questions?

- Open a discussion
- Comment on related issue
- Email maintainers
- Check existing documentation

## Recognition

Contributors will be:
- Listed in CONTRIBUTORS.md
- Thanked in release notes
- Recognized in README

Thank you for contributing to MultiVPN! 🎉

---

**Last Updated**: 2026-06-30
