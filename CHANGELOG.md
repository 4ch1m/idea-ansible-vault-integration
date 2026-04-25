# Changelog

## Unreleased

## 4.2.2

### Changed

- dependency updates
- Gradle-wrapper update

## 4.2.1

### Changed

- dependency updates
- Gradle-wrapper update

## 4.2.0

### Changed

- dependency updates

### Fixed

- vault (multiple) files using vault-ids  
(the process is no longer blocked due to the popup-list not being shown properly)

## 4.1.0

### Changed

- improved input validation in settings dialog
- improved temp-file handling  
(safer file permissions; unique name; delete-on-exit)
- other minor code improvements
- Gradle-wrapper update

### Fixed

- vaulting of unsynchronized files  
(cached editor content now gets saved before running encryption)
- properly filtering out `WARNING`/`DEPRECATION` (`stderr`) lines from `ansible-vault` response
- don't trim content when (un)vaulting files  
(retain new lines at the end)

## 4.0.0

### Removed

- **IMPORTANT**  
  Due to the [repository/ownership transition](https://github.com/timo-reymann/idea-ansible-vault-integration), all settings of this plugin will be reset to defaults upon upgrading to version `4.0.0`!

### Changed

- refactored code (package change, etc.)
- updated test-resources
- updated plugin icon
- required IDE version >= `2025.1`
- license change (Apache -> MIT)
- dependency updates
- updated documentation

### Fixed

- Ansible config detection now includes the file referenced via environment variable (`ANSIBLE_CONFIG`)
- various other minor code fixes/improvements

## 3.3.0

### Changed

- replace deprecated WSL Path API call

## 3.2.1

### Changed

- migrate align for text fields to Kotlin DSL v2

## 3.2.0

### Added

- support for binary files ([issue +51](https://github.com/timo-reymann/idea-ansible-vault-integration/issues/51))

### Changed

- notifications are now grouped together and provide even better output of errors and what has been done
- use v2 Kotlin DSL for settings UI to make sure it works smooth in newer versions of IntelliJ
- plugin now requires IntelliJ platform `2022.3+`
- improve performance for vault file check by only reading first bytes instead of traversing PSI

## 3.1.0

### Added

- support for multiple files to be encrypted at once by [4ch1m](https://github.com/4ch1m) ([pull #50](https://github.com/timo-reymann/idea-ansible-vault-integration/pull/50))

## 3.0.0

### Fixed

- don't omit empty lines of decrypted content by [4ch1m](https://github.com/4ch1m) ([pull #49](https://github.com/timo-reymann/idea-ansible-vault-integration/pull/49))  
  (this might break current assumptions or formats so please check before updating and report if this change affects you)

## 2.4.0

### Added

- environment variables to custom vault script execution for project information by [4ch1m](https://github.com/4ch1m) ([issue #48](https://github.com/timo-reymann/idea-ansible-vault-integration/pull/48))

## 2.3.0

### Changed

- improved error message for invalid executable ([issue #44](https://github.com/timo-reymann/idea-ansible-vault-integration/issues/44))

## 2.2.1

### Fixed

- invalid padding in some cases of Ansible Vault context action

## 2.2.0

### Added

- support for Ansible Vault identity-list ([issue #15](https://github.com/timo-reymann/idea-ansible-vault-integration/issues/15))

## 2.1.2

### Fixed

- missing intention description errors

## 2.1.0

### Added

- vault/unvault action for entire files ([issue #8](https://github.com/timo-reymann/idea-ansible-vault-integration/issues/8))

## 2.0.1

### Fixed

- don't provide context action for decryption for full encrypted files ([issue #8](https://github.com/timo-reymann/idea-ansible-vault-integration/issues/8))

## 2.0.0

### Added

- more context for configuration and better pre-validation

### Changed

- plugin completely rewritten in Kotlin

## 1.4.0

### Added

- make timeout for commands configurable

## 1.3.0

### Added

- WSL support

## 1.2.0

### Added

- environment variables to allow custom scripts

## 1.1.0

### Added

- plugin icon

## 1.0.0

### Changed

- make Ansible Vault execution relative to project root

## 0.0.1

### Added

- initial plugin creation;  
this is where it all starts
