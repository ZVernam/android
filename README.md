# Vernam Android Password Generator
Android App for password generation. See [details](https://github.com/ZVernam/vernam-cipher)

### Description:
This is password generator, which allows you to generate unique passwords on the fly.

How does it work?
1. You enter some key which you'd like to generate unique password for.
2. Enter you base secret password
3. Copy generated unique password, which is not encryptable Vernam Cipher combination of previous 2 strings.

Features:
- Store password encrypted with you biometrics
- Accepts any url from any application
- Strips domain name from url
- Optionally can add any suffix to your input string as a salt

### Release Notes
- [v1.0.0](https://github.com/ZVernam/android/releases/tag/v1.0.0)
  - initial release
- v1.0.0 - [v1.0.4](https://github.com/ZVernam/android/releases/tag/v1.0.4)
  - fix day theme
  - update lib versions
  - update SDK version
- v1.0.4 - [v1.0.5](https://github.com/ZVernam/android/releases/tag/v1.0.5)
  - update icons
  - some minor code cleanup

### Links:
- [Awesome asset studio](https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html#foreground.type=image&foreground.space.trim=1&foreground.space.pad=0&foreColor=rgba(96%2C%20125%2C%20139%2C%200)&backColor=rgb(30%2C%2080%2C%2032)&crop=0&backgroundShape=circle&effects=elevate&name=ic_launcher_round)
- [Overlap Activities](https://stackoverflow.com/questions/7878235/overlay-an-activity-on-another-activity-or-overlay-a-view-over-another)
- [Biometric Auth](https://developer.android.com/training/sign-in/biometric-auth). Related:  
  - https://medium.com/androiddevelopers/migrating-from-fingerprintmanager-to-biometricprompt-4bc5f570dccd
  - https://gist.github.com/Tanapruk/1154011542739c223c0cf59997867b15
  - https://developer.android.com/reference/android/app/KeyguardManager#createConfirmDeviceCredentialIntent(java.lang.CharSequence,%20java.lang.CharSequence)
  - https://developer.android.com/training/sign-in/biometric-auth
- [Android Material Components](https://material.io/develop/android/components/text-fields/)
- [Android DayNight Theme Control](https://medium.com/androiddevelopers/appcompat-v23-2-daynight-d10f90c83e94)
- [Android View Binding](https://developer.android.com/topic/libraries/view-binding)
- [Android View Binding Caveats](https://betterprogramming.pub/exploring-viewbinding-in-depth-598925821e41)
