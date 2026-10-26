# Alien Launcher 0.3.0-alpha.1 — versão anterior

> Use a [0.3.0-alpha.2 com o novo emblema](../v0.3.0-alpha.2/README.md). O layout foi preservado.

**[Baixar ZIP — 66.78 MiB](https://github.com/AttackAlien/Alien-Client/raw/refs/heads/feature/launcher-unified-alpha2-20261026/downloads/launcher/v0.3.0-alpha.1/AlienLauncher-Windows-x64-0.3.0-alpha.1.zip)** · [Fotos reais atuais](https://github.com/AttackAlien/Alien-Client/blob/0225a88d4fc1c49d85c84893a693cd1a6401ec73/captures/launcher-atual/README.md) · [Testes e limites](../../../docs/LAUNCHER-REDESIGN-0.3.md) · [SHA-256](SHA256SUMS.txt).

Interface refeita: navegação lateral, cenário do client, controles escuros, configurações separadas, slider de RAM e botão de jogar em destaque. Mantém o **Unified 0.6.0-alpha.2 / Forge 1.8.9**, o isolamento dos perfis e os backups da instalação anterior.

1. Extraia **todo o ZIP** no Windows 10/11 x64 e abra `AlienLauncher.exe`. Mantenha `payload` ao lado do EXE; o .NET já está incluído.
2. Na tela **Jogar**, escolha o perfil e use **Instalar / reparar** para baixar/verificar Minecraft, Java e Forge oficiais.
3. Na lateral, **Conectar conta** abre o fluxo Microsoft para uma conta que possua Minecraft Java; requer WebView2 Runtime.
4. Use **Jogar Alien**. RAM, seleção/restauração de Java 8 e logout ficam em **Configurações**. **Meus arquivos** abre a instância e **Diagnóstico** mostra os registros reais.

Dados em `%LOCALAPPDATA%\AlienLauncher`, preservando saves, configurações e mods de terceiros. O Alien permanece no Forge 1.8.9; perfis modernos são Vanilla independente.

**Alpha, sem merge na main.** O login Microsoft interativo e gameplay em PC Windows físico ainda não foram validados. O [CI 40340826688](https://github.com/AttackAlien/Alien-Client/actions/runs/40340826688) passou **22 testes Linux, 22 Windows, 36 verificações da interface real e instalação Forge real**.

Código do launcher: `232ce580e8246766a04c24a75d5bb3c32e00b249`. ZIP SHA-256: `6aad995f2dac180012647d331c4c235cc786e3945d2f94f8b638082a85a72167`. O pacote foi copiado exatamente do CI aprovado; não inclui probes, caches de conta ou imagens de teste.
