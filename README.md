# Melo 🎵

<div align="center">

  **A sleek, ultra-modern music player for Android. Beautiful UI, powered by Jetpack Compose, AGSL shaders, and zero telemetry.**

<br/>

<p align="center">
  <a href="https://github.com/techxsarwar/melo/releases">
    <img src="https://img.shields.io/github/v/release/techxsarwar/melo?style=for-the-badge&color=cba6f7&label=Release" alt="Latest Release">
  </a>
  <a href="https://github.com/techxsarwar/melo/stargazers">
    <img src="https://img.shields.io/github/stars/techxsarwar/melo?style=for-the-badge&color=f9e2af&label=Stars" alt="Stars">
  </a>
  <a href="https://github.com/techxsarwar/melo/network/members">
    <img src="https://img.shields.io/github/forks/techxsarwar/melo?style=for-the-badge&color=89b4fa&label=Forks" alt="Forks">
  </a>
  <a href="https://github.com/techxsarwar/melo/releases">
    <img src="https://img.shields.io/badge/Android-8.0%2B-94e2d5?style=for-the-badge&logo=android&logoColor=white" alt="Android 8.0+">
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/License-GPL--3.0-fab387?style=for-the-badge" alt="GPL-3.0 License">
  </a>
</p>
</div>

---

## ✨ Features

- 🎨 **Next-Gen Fluid UI**: Built with Jetpack Compose, AGSL custom runtime shaders, dynamic artwork palette extraction, and smooth 120Hz display refresh locking.
- 📱 **3D Gyroscope Card Tilt**: Uses device sensors to physically tilt album cards in 3D space as you angle your phone.
- 🎤 **Apple Music-Style Glowing Lyrics**: Real-time karaoke syllable sync, dynamic glows, and blur masks powered by [LRCLIB](https://lrclib.net).
- 🎛️ **Dual-Engine DJ Crossmix**: Runs two ExoPlayer audio engines simultaneously with transient analysis, kick-drum beat matching, vinyl brake, and tape-stop effects.
- 📻 **Full YouTube Music Library**: Direct streaming via InnerTube with zero API keys required and zero ads.
- 📺 **Android TV Mode**: Dedicated 10-foot television interface with D-pad navigation and a local Wi-Fi phone remote protocol.
- 🌙 **Standby Bedside Clock**: Ambient bedside display with glowing retro nixie clocks when charging in landscape.
- 🎧 **AutoEq Integration**: Over 2,500+ built-in headphone equalizer presets.
- 🔒 **Private & Decentralized**: No accounts needed, no central tracking server, and full offline caching support.

---

## 📥 How to Install

1. Go to the [Actions Tab](https://github.com/techxsarwar/melo/actions) or [Releases](https://github.com/techxsarwar/melo/releases).
2. Download the `app-universal-gms-debug.apk` file.
3. Open the APK on your Android device and tap **Install**.
4. Launch **Melo** and start listening!

---

## 🛠️ Building From Source

### Prerequisites:
- Android Studio (latest stable) with JDK 21
- Android SDK 36 and NDK `27.0.12077973`
- A connected device with USB debugging enabled or an emulator

### Build Steps:
```bash
git clone https://github.com/techxsarwar/melo.git
cd melo

# Build and install debug APK directly on your phone:
./build.sh --debug -i
```

Or build using Gradle directly:
```bash
./gradlew assembleUniversalGmsDebug
```

---

## 👥 Developers

- **Sarwar Altaf Dar** — Lead Developer  
  GitHub: [@techxsarwar](https://github.com/techxsarwar/) · Instagram: [@i.jubito](https://www.instagram.com/i.jubito)  
  *“yea i started ts and yea here we are”*

- **Burhan Hamid Dar** — Developer  
  GitHub: [@BurhanHamidDar](https://github.com/BurhanHamidDar) · Instagram: [@let_me_code__](https://www.instagram.com/let_me_code__/)  
  *“hell yea idc give a star”*

### ☕ Support Development
UPI ID: `sarwaraltafdar@fam` *(Works with Google Pay, PhonePe, Paytm, and BHIM)*

---

## 📜 Credits & Lineage

Melo is an open-source project licensed under **GNU General Public License v3.0 (GPL-3.0)**. It builds upon and draws inspiration from incredible open-source projects:

- **Mochy Music** by [realidkroo](https://github.com/realidkroo/Mochy-Music) — Upstream base UI & audio architecture.
- **ViviMusic** by [vivizzz007](https://github.com/vivizzz007/vivi-music) — Base player engine.
- **Metrolist** by [Mostafa Alagamy](https://github.com/mostafaalagamy/Metrolist) — Core streaming framework.
- **RiMusic** by [fast4x](https://github.com/fast4x/RiMusic) — InnerTube service wrapper.
- **LRCLIB** by [tranxuanthang](https://github.com/tranxuanthang/lrclib) — Synchronized lyrics provider.
- **AutoEq** by [Jaakko Pasanen](https://github.com/jaakkopasanen/AutoEq) — Headphone equalizer database.

---

## 📄 License

```
Melo - Open Source Android Music Player
Copyright (C) 2026 Sarwar Altaf Dar, Burhan Hamid Dar, and Contributors.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```
See the full [LICENSE](LICENSE) file for details.
