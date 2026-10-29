# Alien Launcher 0.3.0-alpha.4 — Trail Studio / Windows x64

**[Baixar ZIP — 67.45 MiB](https://github.com/AttackAlien/Alien-Client/raw/refs/heads/feature/trail-studio-20261029/downloads/launcher/v0.3.0-alpha.4/AlienLauncher-Windows-x64-0.3.0-alpha.4.zip)** · [Fotos reais atuais](../../../captures/launcher-atual/README.md) · [Validação](../../../docs/TRAIL-STUDIO-VALIDATION.md) · [SHA-256](SHA256SUMS.txt) · [Origem do client](client-source.json).

Inclui o **Unified 0.7.0-alpha.1 / Forge 1.8.9 / Java 8**, com o novo Trail Studio e as correções da auditoria. O layout e o emblema aprovados foram mantidos; a identificação do client acompanha a versão real incluída.

1. Extraia todo o ZIP no Windows 10/11 x64 e abra `AlienLauncher.exe`, mantendo `payload` ao lado do executável. O .NET está incluído.
2. Escolha Alien e use **Instalar / reparar** para baixar/verificar Minecraft, Java e Forge oficiais.
3. **Conectar conta** usa Microsoft com uma conta que possua Minecraft Java; requer WebView2 Runtime. Depois use **Jogar Alien**.
4. No jogo, abra **Cosmetic Studio → Motion → Trail settings**, selecione Ribbon e ative Trail e Designer. [Guia das trilhas](../../../docs/TRAIL-STUDIO.md).

Dados e instâncias em `%LOCALAPPDATA%\AlienLauncher`. Perfis Vanilla são independentes. Saves e outros mods são preservados; versões anteriores do mod Alien vão para backup.

[CI 40890062885](https://github.com/AttackAlien/Alien-Client/actions/runs/40890062885): **31 testes Linux, 31 Windows, 46 checks da interface real, seis capturas e instalação Forge/Java 8 aprovados**. Fonte do launcher `a2f11fef4675af5fcf9260992a01a98767a5ed9f`. O ZIP foi copiado sem alteração do CI e passou por CRC completo, EXE PE/AMD64 e verificação do JAR.

ZIP SHA-256: `9a54a20f6771cff56718c1f3be9f14407135201f3e89060a832630a04ea7d65a`. JAR incluído SHA-256: `69f5e881ab8f88e5a7bf36de44c3f217a83d4b343c94b369965f890a1cb0a240`.

**Alpha do PR #22, sem merge na main.** Autenticação Microsoft com conta licenciada, renovação/logout reais, gameplay em Windows físico, GPU física e OptiFine ainda dependem de validação. O EXE experimental não tem assinatura Authenticode; não inclui ferramentas de teste nem caches de conta.
