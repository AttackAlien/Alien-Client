# Launcher 0.3.0-alpha.6 — Windows x64

**[Baixar ZIP — 67.50 MiB](https://github.com/AttackAlien/Alien-Client/raw/refs/heads/feature/reactive-auras-20261104/downloads/launcher/v0.3.0-alpha.6/AlienLauncher-Windows-x64-0.3.0-alpha.6.zip)** · [Fotos reais](https://github.com/AttackAlien/Alien-Client/blob/4ea1b8bb2a66fb4250fb7aaa98310aa9f9f5e8aa/captures/launcher-atual/README.md) · [Validação](../../../docs/REACTIVE-AURAS-VALIDATION.md) · [SHA-256](SHA256SUMS.txt).

Inclui **Unified 0.9.0-alpha.1**, Aura Studio e todos os recursos anteriores. O layout e o emblema aprovados foram mantidos.

1. Extraia o ZIP inteiro em Windows 10/11 x64 e abra `AlienLauncher.exe`, mantendo `payload` ao lado.
2. Escolha Alien e use **Instalar / reparar** para baixar/verificar Java 8, Forge e arquivos oficiais.
3. Conecte uma conta Microsoft que possua Minecraft Java; o login requer WebView2 Runtime. Use **Jogar Alien**.
4. Abra **Cosmetic Studio → Auras**, escolha Reactive e um preset. [Guia](../../../docs/REACTIVE-AURAS.md).

Dados em `%LOCALAPPDATA%\AlienLauncher`. Saves, configurações e outros mods são preservados; JARs Alien anteriores vão para backup. Instâncias Vanilla são independentes.

[CI 41845407649](https://github.com/AttackAlien/Alien-Client/actions/runs/41845407649): 31 testes Linux, 31 Windows, 46 checks da janela real, seis capturas e instalação real Forge/Java 8 aprovados. Fonte `331ee2bbf7aa12e18503e4c3e7bc793b489020e8`. ZIP original do CI, CRC completo e EXE PE/AMD64 verificados.

ZIP SHA-256: `fb5d9e3c25bfac64f3c91a3f6e6e296afbe108ab0306d4e067b20ebe83d05147`. JAR incluído: `2ea2c0d4a786006863600cfd4f569660288014e47b6045692f7e50fc9084a036`. Alpha do PR #24, sem merge. Login/licença Microsoft, renovação/logout reais, servidores externos, GPU física, gameplay em Windows pessoal e OptiFine seguem sem validação. EXE sem assinatura Authenticode; ferramentas de teste e caches de conta não são distribuídos.
