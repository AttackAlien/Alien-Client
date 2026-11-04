# Alien Launcher 0.3.0-alpha.5 — Emote Studio / Windows x64

**[Baixar ZIP — 67.48 MiB](https://github.com/AttackAlien/Alien-Client/raw/refs/heads/feature/emote-studio-20261104/downloads/launcher/v0.3.0-alpha.5/AlienLauncher-Windows-x64-0.3.0-alpha.5.zip)** · [Fotos reais atuais](https://github.com/AttackAlien/Alien-Client/blob/8c11c2fecdd864a458d46e7e2ce950edde8a9140/captures/launcher-atual/README.md) · [Validação](../../../docs/EMOTE-STUDIO-VALIDATION.md) · [SHA-256](SHA256SUMS.txt) · [Origem do client](client-source.json).

Inclui **Unified 0.8.0-alpha.1 / Forge 1.8.9 / Java 8**, com Emote Studio e todos os estúdios anteriores. Layout e emblema aprovados foram preservados.

1. Extraia o ZIP inteiro em Windows 10/11 x64 e abra `AlienLauncher.exe`, mantendo `payload` ao lado. O .NET está incluído.
2. Escolha Alien e use **Instalar / reparar** para baixar/verificar Minecraft, Java e Forge oficiais.
3. Conecte uma conta Microsoft que possua Minecraft Java; o login requer WebView2 Runtime. Depois use **Jogar Alien**.
4. No mundo, segure **B**, selecione o emote e solte. Personalize em **Cosmetic Studio → Emotes**. [Guia](../../../docs/EMOTE-STUDIO.md).

Dados em `%LOCALAPPDATA%\AlienLauncher`; saves, configurações e mods são preservados. Instâncias Vanilla continuam independentes. JARs Alien anteriores vão para backup.

[CI 41830735294](https://github.com/AttackAlien/Alien-Client/actions/runs/41830735294): **31 testes Linux, 31 Windows, 46 checks da interface real, seis capturas e instalação Forge/Java 8 aprovados**. Fonte `e080fbf144df1e1f1b40adbd854b795b7cde9ca9`. ZIP original do CI, com CRC completo e EXE PE/AMD64 verificados.

ZIP SHA-256: `d3c13c13f9b4149d3c84c370af4eb1ae68b30395bf90382d20b110baf382d254`. JAR incluído SHA-256: `9d5a7caefc731aa69a3cbc2536e3b0ae827a84b789b9db00fecd7d29491d8a0c`.

**Alpha do PR #23, sem merge na main.** Login Microsoft/licença/renovação/logout reais, multiplayer externo, GPU física, gameplay em PC Windows pessoal e OptiFine seguem sem validação. O EXE experimental não possui assinatura Authenticode nem inclui ferramentas de teste ou caches de conta.
