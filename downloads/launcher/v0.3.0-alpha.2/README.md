# Alien Launcher 0.3 — interface nova / Windows x64

**[Baixar ZIP — 67.44 MiB](https://github.com/AttackAlien/Alien-Client/raw/refs/heads/feature/launcher-unified-alpha2-20261026/downloads/launcher/v0.3.0-alpha.2/AlienLauncher-Windows-x64-0.3.0-alpha.2.zip)** · [Fotos reais atuais](../../../captures/launcher-atual/README.md) · [Testes e limites](../../../docs/LAUNCHER-REDESIGN-0.3.md) · [SHA-256](SHA256SUMS.txt).

Novo emblema Alien roxo e facetado, com fundo transparente. O layout, os controles e os recursos da interface 0.3 foram preservados. Mantém o **Unified 0.6.0-alpha.2 / Forge 1.8.9**, o isolamento dos perfis e os backups da instalação anterior.

1. Extraia **todo o ZIP** no Windows 10/11 x64 e abra `AlienLauncher.exe`. Mantenha `payload` ao lado do EXE; o .NET já está incluído.
2. Na tela **Jogar**, escolha o perfil e use **Instalar / reparar** para baixar/verificar Minecraft, Java e Forge oficiais.
3. Na lateral, **Conectar conta** abre o fluxo Microsoft para uma conta que possua Minecraft Java; requer WebView2 Runtime.
4. Use **Jogar Alien**. RAM, seleção/restauração de Java 8 e logout ficam em **Configurações**. **Meus arquivos** abre a instância e **Diagnóstico** mostra os registros reais.

Dados em `%LOCALAPPDATA%\AlienLauncher`, preservando saves, configurações e mods de terceiros. O Alien permanece no Forge 1.8.9; perfis modernos são Vanilla independente.

**Alpha, sem merge na main.** O login Microsoft interativo e gameplay em PC Windows físico ainda não foram validados. O [CI 40343053042](https://github.com/AttackAlien/Alien-Client/actions/runs/40343053042) passou **22 testes Linux, 22 Windows, 36 verificações da interface real e instalação Forge real**.

Código do launcher: `69ac3f43f73e2511cfbf19dc04e9c05ac6f293ef`. ZIP SHA-256: `3f8d5fe57af6514bbb6afd95ce050de8a60c62f627449c524fc64de335b2c008`. O pacote foi copiado exatamente do CI aprovado; não inclui probes, caches de conta ou imagens de teste.
